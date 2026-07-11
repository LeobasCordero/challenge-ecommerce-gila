package com.gila.ecommerce.controller;

import com.gila.ecommerce.api.CartApi;
import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemRequestDto;
import com.gila.ecommerce.service.CartService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementing shopping cart REST endpoints.
 */
@RestController
public class CartController implements CartApi {

    private final CartService cartService;

    /**
     * Constructor injecting CartService.
     * @param cartService shopping cart service interface
     */
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Clear all items from active shopping cart context.
     * @return 204 No Content
     */
    @Override
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(getAuthenticatedUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve details of customer shopping cart.
     * @return active cart DTO
     */
    @Override
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(cartService.getCart(getAuthenticatedUsername()));
    }

    /**
     * Remove a single product item reference from the cart.
     * @param productId target product identifier
     * @return updated cart DTO
     */
    @Override
    public ResponseEntity<CartDto> removeCartItem(UUID productId) {
        return ResponseEntity.ok(cartService.removeCartItem(getAuthenticatedUsername(), productId));
    }

    /**
     * Add or update specific product stock units in shopping cart.
     * @param cartItemRequestDto request detailing product and quantity
     * @return updated cart DTO
     */
    @Override
    public ResponseEntity<CartDto> updateCartItem(CartItemRequestDto cartItemRequestDto) {
        return ResponseEntity.ok(cartService.updateCartItem(getAuthenticatedUsername(), cartItemRequestDto));
    }

    /**
     * Retrieve authenticated username context.
     * @return username key from security principal
     */
    private String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
