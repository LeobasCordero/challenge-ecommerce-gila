package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemDto;
import com.gila.ecommerce.dto.OrderDto;
import com.gila.ecommerce.dto.OrderItemDto;
import com.gila.ecommerce.model.Order;
import com.gila.ecommerce.model.OrderItem;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.model.User;
import com.gila.ecommerce.repository.OrderRepository;
import com.gila.ecommerce.repository.ProductRepository;
import com.gila.ecommerce.repository.UserRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service implementation processing transactional checkouts and order history resets.
 */
@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final AuditLogService auditLogService;

    /**
     * Constructor injecting dependencies.
     * @param userRepository user database interface
     * @param productRepository product catalog database interface
     * @param orderRepository order database interface
     * @param cartService shopping cart service interface
     * @param auditLogService audit logging service interface
     */
    public CheckoutServiceImpl(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            CartService cartService,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.auditLogService = auditLogService;
    }

    /**
     * Complete order checkout for the active user's cart.
     * @param username user checking out
     * @return created order transaction details
     */
    @Override
    @Transactional
    public OrderDto checkout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        CartDto cart = cartService.getCart(username);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
            auditLogService.log(username, "CHECKOUT", "FAILURE", Map.of("reason", "Cart is empty"));
            throw ex;
        }

        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus("PAID");
        order.setCreatedAt(OffsetDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (CartItemDto cartItem : cart.getItems()) {
            UUID productId = cartItem.getProduct().getId();
            Product product = productRepository.findWithLockById(productId)
                    .orElseThrow(() -> {
                        ResponseStatusException ex = new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Product not found: " + productId
                        );
                        auditLogService.log(username, "CHECKOUT", "FAILURE", Map.of("reason", "Product not found: " + productId));
                        return ex;
                    });

            if (product.getStock() < cartItem.getQuantity()) {
                ResponseStatusException ex = new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Insufficient stock for product: " + product.getName()
                );
                auditLogService.log(username, "CHECKOUT", "FAILURE", Map.of("reason", "Insufficient stock for product: " + product.getName()));
                throw ex;
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setId(UUID.randomUUID());
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            items.add(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalPrice(total);
        order.setItems(items);
        Order savedOrder = orderRepository.save(order);

        cartService.clearCart(username);

        auditLogService.log(username, "CHECKOUT", "SUCCESS", Map.of(
                "orderId", orderId.toString(),
                "totalPrice", total.doubleValue()
        ));

        return toOrderDto(savedOrder);
    }

    /**
     * Reset order transaction history and restore product stocks.
     */
    @Override
    @Transactional
    public void clearOrders() {
        orderRepository.deleteAll();
        productRepository.findAll().forEach(product -> {
            product.setStock(product.getInitialStock());
            productRepository.save(product);
        });
    }

    /**
     * Maps an Order entity to an OrderDto model.
     * @param order database order entity
     * @return order DTO model
     */
    private OrderDto toOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice().doubleValue());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDto> itemDtos = order.getItems().stream().map(item -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setProduct(ProductMapper.toDto(item.getProduct()));
            itemDto.setQuantity(item.getQuantity());
            itemDto.setPriceAtPurchase(item.getPriceAtPurchase().doubleValue());
            return itemDto;
        }).collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }
}
