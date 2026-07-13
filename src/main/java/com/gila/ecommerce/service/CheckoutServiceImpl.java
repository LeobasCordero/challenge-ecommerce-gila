package com.gila.ecommerce.service;

import com.gila.ecommerce.aspect.Auditable;
import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemDto;
import com.gila.ecommerce.dto.OrderDto;
import com.gila.ecommerce.dto.OrderItemDto;
import com.gila.ecommerce.exception.ErrorMessages;
import com.gila.ecommerce.model.Order;
import com.gila.ecommerce.model.OrderItem;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.model.User;
import com.gila.ecommerce.repository.OrderRepository;
import com.gila.ecommerce.repository.ProductRepository;
import com.gila.ecommerce.repository.UserRepository;
import com.gila.ecommerce.util.AuditAction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    /**
     * Constructor injecting dependencies.
     * @param orderRepository order log database interface
     * @param productRepository product catalog database interface
     * @param userRepository user profile database interface
     * @param cartService shopping cart service session manager
     */
    public CheckoutServiceImpl(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CartService cartService
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    /**
     * Complete order checkout for the active user's cart.
     * @param username user checking out
     * @return created order transaction details
     */
    @Override
    @Transactional
    @Auditable(action = AuditAction.CHECKOUT)
    public OrderDto checkout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorMessages.USER_NOT_FOUND));

        CartDto cart = cartService.getCart(username);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.CART_EMPTY);
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
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, ErrorMessages.PRODUCT_NOT_FOUND_PREFIX + productId
                    ));

            if (product.getStock() < cartItem.getQuantity()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, ErrorMessages.INSUFFICIENT_STOCK_PREFIX + product.getName()
                );
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

        return toOrderDto(savedOrder);
    }

    /**
     * Reset database transaction tables and restore original product baseline stock settings.
     */
    @Override
    @Transactional
    public void clearOrders() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            if (product.getInitialStock() != null) {
                product.setStock(product.getInitialStock());
                productRepository.save(product);
            }
        }
        orderRepository.deleteAll();
    }

    /**
     * Map order data model fields into response DTO wrappers.
     * @param order entity model instance
     * @return populated DTO container
     */
    private OrderDto toOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setTotalPrice(order.getTotalPrice().doubleValue());
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(this::toOrderItemDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setItems(new ArrayList<>());
        }
        return dto;
    }

    /**
     * Map order item data model fields into response DTO wrappers.
     * @param item entity model item instance
     * @return populated item DTO container
     */
    private OrderItemDto toOrderItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setProduct(ProductMapper.toDto(item.getProduct()));
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtPurchase(item.getPriceAtPurchase().doubleValue());
        return dto;
    }
}
