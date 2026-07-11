package com.gila.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemRequestDto;
import com.gila.ecommerce.exception.ErrorMessages;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
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
public class InMemoryCartServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InMemoryCartService cartService;

    private String username = "testuser";
    private UUID productId;
    private Product product;

    @BeforeEach
    public void setUp() {
        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setName("Test Book");
        product.setPrice(BigDecimal.valueOf(9.99));
        product.setStock(10);
        product.setCategory("Books");
    }

    @Test
    public void testGetCart_Empty() {
        CartDto cart = cartService.getCart(username);
        assertNotNull(cart);
        assertTrue(cart.getItems().isEmpty());
        assertEquals(0.0, cart.getTotalPrice());
    }

    @Test
    public void testUpdateCartItem_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        CartItemRequestDto request = new CartItemRequestDto();
        request.setProductId(productId);
        request.setQuantity(3);

        CartDto cart = cartService.updateCartItem(username, request);

        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
        assertEquals(29.97, cart.getTotalPrice());
    }

    @Test
    public void testUpdateCartItem_ProductNotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        CartItemRequestDto request = new CartItemRequestDto();
        request.setProductId(productId);
        request.setQuantity(3);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            cartService.updateCartItem(username, request);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ErrorMessages.PRODUCT_NOT_FOUND, exception.getReason());
    }

    @Test
    public void testUpdateCartItem_InsufficientStock() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        CartItemRequestDto request = new CartItemRequestDto();
        request.setProductId(productId);
        request.setQuantity(15); // stock is 10

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            cartService.updateCartItem(username, request);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessages.INSUFFICIENT_STOCK, exception.getReason());
    }

    @Test
    public void testUpdateCartItem_RemoveItemWhenQuantityZeroOrLess() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // First, add item
        CartItemRequestDto requestAdd = new CartItemRequestDto();
        requestAdd.setProductId(productId);
        requestAdd.setQuantity(3);
        cartService.updateCartItem(username, requestAdd);

        // Update to quantity 0
        CartItemRequestDto requestRemove = new CartItemRequestDto();
        requestRemove.setProductId(productId);
        requestRemove.setQuantity(0);

        CartDto cart = cartService.updateCartItem(username, requestRemove);

        assertNotNull(cart);
        assertTrue(cart.getItems().isEmpty());
        assertEquals(0.0, cart.getTotalPrice());
    }

    @Test
    public void testRemoveCartItem() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Add item
        CartItemRequestDto requestAdd = new CartItemRequestDto();
        requestAdd.setProductId(productId);
        requestAdd.setQuantity(3);
        cartService.updateCartItem(username, requestAdd);

        // Remove item
        CartDto cart = cartService.removeCartItem(username, productId);

        assertNotNull(cart);
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    public void testClearCart() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Add item
        CartItemRequestDto requestAdd = new CartItemRequestDto();
        requestAdd.setProductId(productId);
        requestAdd.setQuantity(3);
        cartService.updateCartItem(username, requestAdd);

        // Clear cart
        cartService.clearCart(username);

        // Get cart again should be empty
        CartDto cart = cartService.getCart(username);
        assertNotNull(cart);
        assertTrue(cart.getItems().isEmpty());
    }
}
