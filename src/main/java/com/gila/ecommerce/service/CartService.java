package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemRequestDto;
import java.util.UUID;

/**
 * Service interface defining shopping cart management operations.
 */
public interface CartService {

    /**
     * Retrieve the active shopping cart for a given username.
     * @param username user lookup key
     * @return active cart DTO
     */
    CartDto getCart(String username);

    /**
     * Add or update an item quantity in a user's cart.
     * @param username user lookup key
     * @param request cart item modification request
     * @return updated cart DTO
     */
    CartDto updateCartItem(String username, CartItemRequestDto request);

    /**
     * Remove an item from a user's cart.
     * @param username user lookup key
     * @param productId target product to remove
     * @return updated cart DTO
     */
    CartDto removeCartItem(String username, UUID productId);

    /**
     * Clear all items from a user's cart.
     * @param username user lookup key
     */
    void clearCart(String username);
}
