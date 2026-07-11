package com.gila.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemDto;
import com.gila.ecommerce.dto.OrderDto;
import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.exception.ErrorMessages;
import com.gila.ecommerce.model.Order;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.model.User;
import com.gila.ecommerce.repository.OrderRepository;
import com.gila.ecommerce.repository.ProductRepository;
import com.gila.ecommerce.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private String username = "customer";
    private User user;
    private Product product;
    private CartDto cartDto;
    private CartItemDto cartItemDto;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setStock(10);
        product.setInitialStock(10);

        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setPrice(100.0);
        productDto.setStock(10);

        cartItemDto = new CartItemDto();
        cartItemDto.setProduct(productDto);
        cartItemDto.setQuantity(2);

        cartDto = new CartDto();
        cartDto.setItems(Collections.singletonList(cartItemDto));
        cartDto.setTotalPrice(200.0);
    }

    @Test
    public void testCheckout_Success() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cartService.getCart(username)).thenReturn(cartDto);
        when(productRepository.findWithLockById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto orderDto = checkoutService.checkout(username);

        assertNotNull(orderDto);
        assertEquals("PAID", orderDto.getStatus());
        assertEquals(200.0, orderDto.getTotalPrice());
        assertEquals(8, product.getStock()); // stock decremented from 10 to 8
        verify(productRepository, times(1)).save(product);
        verify(cartService, times(1)).clearCart(username);
    }

    @Test
    public void testCheckout_UserNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            checkoutService.checkout(username);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals(ErrorMessages.USER_NOT_FOUND, exception.getReason());
    }

    @Test
    public void testCheckout_CartEmpty() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        CartDto emptyCart = new CartDto();
        emptyCart.setItems(Collections.emptyList());
        when(cartService.getCart(username)).thenReturn(emptyCart);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            checkoutService.checkout(username);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessages.CART_EMPTY, exception.getReason());
    }

    @Test
    public void testCheckout_ProductNotFound() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cartService.getCart(username)).thenReturn(cartDto);
        when(productRepository.findWithLockById(product.getId())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            checkoutService.checkout(username);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains(ErrorMessages.PRODUCT_NOT_FOUND_PREFIX));
    }

    @Test
    public void testCheckout_InsufficientStock() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(cartService.getCart(username)).thenReturn(cartDto);
        product.setStock(1); // quantity in cart is 2
        when(productRepository.findWithLockById(product.getId())).thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            checkoutService.checkout(username);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains(ErrorMessages.INSUFFICIENT_STOCK_PREFIX));
    }

    @Test
    public void testClearOrders() {
        product.setStock(5); // original is 10, current is 5
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));

        checkoutService.clearOrders();

        assertEquals(10, product.getStock()); // reset to initial stock
        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).deleteAll();
    }
}
