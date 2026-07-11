package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemDto;
import com.gila.ecommerce.dto.CartItemRequestDto;
import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.exception.ErrorMessages;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.repository.ProductRepository;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thread-safe temporary shopping cart service in-memory store before Redis migration.
 */
public class InMemoryCartService implements CartService {

    private final Map<String, Map<UUID, Integer>> carts = new ConcurrentHashMap<>();
    private final ProductRepository productRepository;

    /**
     * Constructor injecting ProductRepository.
     * @param productRepository product catalog database interface
     */
    public InMemoryCartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieve cart contents and sum total price for the active user session.
     * @param username user key
     * @return current shopping cart session contents DTO
     */
    @Override
    public CartDto getCart(String username) {
        Map<UUID, Integer> items = carts.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        CartDto dto = new CartDto();
        dto.setItems(new ArrayList<>());
        double total = 0.0;

        for (Map.Entry<UUID, Integer> entry : items.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            if (product != null) {
                CartItemDto itemDto = new CartItemDto();
                ProductDto prodDto = ProductMapper.toDto(product);
                itemDto.setProduct(prodDto);
                itemDto.setQuantity(entry.getValue());
                dto.getItems().add(itemDto);
                total += product.getPrice().doubleValue() * entry.getValue();
            }
        }
        dto.setTotalPrice(total);
        return dto;
    }

    /**
     * Set specific product quantity level in user cart.
     * @param username user key
     * @param request item request parameters
     * @return updated shopping cart session contents DTO
     */
    @Override
    public CartDto updateCartItem(String username, CartItemRequestDto request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.PRODUCT_NOT_FOUND));

        if (product.getStock() < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INSUFFICIENT_STOCK);
        }

        Map<UUID, Integer> items = carts.computeIfAbsent(username, k -> new ConcurrentHashMap<>());
        if (request.getQuantity() <= 0) {
            items.remove(request.getProductId());
        } else {
            items.put(request.getProductId(), request.getQuantity());
        }
        return getCart(username);
    }

    /**
     * Clear all cart items for user session.
     * @param username user key
     */
    @Override
    public void clearCart(String username) {
        carts.remove(username);
    }

    /**
     * Remove specific product item from user cart.
     * @param username user key
     * @param productId target product identifier
     * @return updated shopping cart session contents DTO
     */
    @Override
    public CartDto removeCartItem(String username, UUID productId) {
        Map<UUID, Integer> items = carts.get(username);
        if (items != null) {
            items.remove(productId);
        }
        return getCart(username);
    }
}
