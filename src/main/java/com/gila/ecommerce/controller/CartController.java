package com.gila.ecommerce.controller;

import com.gila.ecommerce.api.CartApi;
import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Controller stub implementing shopping cart cache operations.
 */
@RestController
public class CartController implements CartApi {

    /**
     * Clear all items from active shopping cart context.
     * @return 204 No Content
     */
    @Override
    public ResponseEntity<Void> clearCart() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve details of customer shopping cart.
     * @return active cart DTO
     */
    @Override
    public ResponseEntity<CartDto> getCart() {
        CartDto cart = new CartDto();
        cart.setItems(new ArrayList<>());
        cart.setTotalPrice(0.0);
        return ResponseEntity.ok(cart);
    }

    /**
     * Remove a single product item reference from the cart.
     * @param productId target product identifier
     * @return updated cart DTO
     */
    @Override
    public ResponseEntity<CartDto> removeCartItem(UUID productId) {
        CartDto cart = new CartDto();
        cart.setItems(new ArrayList<>());
        cart.setTotalPrice(0.0);
        return ResponseEntity.ok(cart);
    }

    /**
     * Add or update specific product stock units in shopping cart.
     * @param cartItemRequestDto request detailing product and quantity
     * @return updated cart DTO
     */
    @Override
    public ResponseEntity<CartDto> updateCartItem(CartItemRequestDto cartItemRequestDto) {
        CartDto cart = new CartDto();
        cart.setItems(new ArrayList<>());
        cart.setTotalPrice(0.0);
        return ResponseEntity.ok(cart);
    }
}
