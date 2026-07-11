package com.gila.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemRequestDto;
import com.gila.ecommerce.exception.ErrorMessages;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class RedisCartServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private RedisCartServiceImpl cartService;

    private String username;
    private UUID productId;
    private Product product;

    @BeforeEach
    public void setUp() {
        username = "testuser";
        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setName("Test Book");
        product.setPrice(BigDecimal.valueOf(9.99));
        product.setStock(10);
        product.setCategory("Books");

        // Lenient because not all tests exercise Redis operations
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    public void testGetCart_Empty() {
        when(hashOperations.entries("cart:" + username)).thenReturn(new HashMap<>());

        CartDto cart = cartService.getCart(username);

        assertNotNull(cart);
        assertTrue(cart.getItems().isEmpty());
        assertEquals(0.0, cart.getTotalPrice());
    }

    @Test
    public void testGetCart_WithItems() {
        Map<Object, Object> entries = new HashMap<>();
        entries.put(productId.toString(), "2");
        when(hashOperations.entries("cart:" + username)).thenReturn(entries);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        CartDto cart = cartService.getCart(username);

        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
        assertEquals(19.98, cart.getTotalPrice(), 0.01);
    }

    @Test
    public void testUpdateCartItem_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(hashOperations.entries("cart:" + username)).thenReturn(new HashMap<>());

        CartItemRequestDto request = new CartItemRequestDto();
        request.setProductId(productId);
        request.setQuantity(3);

        CartDto cart = cartService.updateCartItem(username, request);

        verify(hashOperations).put("cart:" + username, productId.toString(), "3");
        assertNotNull(cart);
    }

    @Test
    public void testUpdateCartItem_ProductNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        CartItemRequestDto request = new CartItemRequestDto();
        request.setProductId(productId);
        request.setQuantity(3);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cartService.updateCartItem(username, request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals(ErrorMessages.PRODUCT_NOT_FOUND, ex.getReason());
    }

    @Test
    public void testUpdateCartItem_InsufficientStock() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        CartItemRequestDto request = new CartItemRequestDto();
        request.setProductId(productId);
        request.setQuantity(15);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cartService.updateCartItem(username, request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(ErrorMessages.INSUFFICIENT_STOCK, ex.getReason());
    }

    @Test
    public void testUpdateCartItem_RemoveWhenQuantityZero() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(hashOperations.entries("cart:" + username)).thenReturn(new HashMap<>());

        CartItemRequestDto request = new CartItemRequestDto();
        request.setProductId(productId);
        request.setQuantity(0);

        cartService.updateCartItem(username, request);

        verify(hashOperations).delete("cart:" + username, productId.toString());
    }

    @Test
    public void testRemoveCartItem() {
        when(hashOperations.entries("cart:" + username)).thenReturn(new HashMap<>());

        CartDto cart = cartService.removeCartItem(username, productId);

        verify(hashOperations).delete("cart:" + username, productId.toString());
        assertNotNull(cart);
    }

    @Test
    public void testClearCart() {
        cartService.clearCart(username);
        verify(redisTemplate).delete("cart:" + username);
    }
}
