package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemDto;
import com.gila.ecommerce.dto.CartItemRequestDto;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Temporary in-memory CartService implementation caching carts in thread-safe collections.
 */
@Service
public class InMemoryCartService implements CartService {

    private final ProductRepository productRepository;
    private final Map<String, Map<UUID, Integer>> carts = new ConcurrentHashMap<>();

    /**
     * Constructor injecting ProductRepository.
     * @param productRepository product catalog database interface
     */
    public InMemoryCartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieve the active shopping cart for a given username.
     * @param username user lookup key
     * @return active cart DTO
     */
    @Override
    public CartDto getCart(String username) {
        Map<UUID, Integer> userItems = carts.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        CartDto cart = new CartDto();
        List<CartItemDto> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> entry : userItems.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            if (product != null) {
                CartItemDto item = new CartItemDto();
                item.setProduct(ProductMapper.toDto(product));
                item.setQuantity(entry.getValue());
                items.add(item);
                total = total.add(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }
        cart.setItems(items);
        cart.setTotalPrice(total.doubleValue());
        return cart;
    }

    /**
     * Add or update an item quantity in a user's cart.
     * @param username user lookup key
     * @param request cart item modification request
     * @return updated cart DTO
     */
    @Override
    public CartDto updateCartItem(String username, CartItemRequestDto request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (product.getStock() < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock");
        }

        Map<UUID, Integer> userItems = carts.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        userItems.put(request.getProductId(), request.getQuantity());
        return getCart(username);
    }

    /**
     * Remove an item from a user's cart.
     * @param username user lookup key
     * @param productId target product to remove
     * @return updated cart DTO
     */
    @Override
    public CartDto removeCartItem(String username, UUID productId) {
        Map<UUID, Integer> userItems = carts.get(username);
        if (userItems != null) {
            userItems.remove(productId);
        }
        return getCart(username);
    }

    /**
     * Clear all items from a user's cart.
     * @param username user lookup key
     */
    @Override
    public void clearCart(String username) {
        Map<UUID, Integer> userItems = carts.get(username);
        if (userItems != null) {
            userItems.clear();
        }
    }
}
