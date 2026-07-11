package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemDto;
import com.gila.ecommerce.dto.CartItemRequestDto;
import com.gila.ecommerce.exception.ErrorMessages;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Redis-backed implementation of CartService, storing cart state as a Hash keyed by username.
 */
@Service
@Primary
public class RedisCartServiceImpl implements CartService {

    private static final String CART_KEY_PREFIX = "cart:";

    private final StringRedisTemplate redisTemplate;
    private final ProductRepository productRepository;

    /**
     * Constructor injecting Redis template and product repository.
     * @param redisTemplate Spring Redis string template for hash operations
     * @param productRepository product catalog database interface
     */
    public RedisCartServiceImpl(StringRedisTemplate redisTemplate, ProductRepository productRepository) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
    }

    /**
     * Retrieve the active shopping cart for a given username from Redis.
     * @param username user lookup key
     * @return active cart DTO with all items and total price
     */
    @Override
    public CartDto getCart(String username) {
        String cartKey = CART_KEY_PREFIX + username;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey);

        CartDto dto = new CartDto();
        dto.setItems(new ArrayList<>());
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            UUID productId = UUID.fromString((String) entry.getKey());
            int quantity = Integer.parseInt((String) entry.getValue());

            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                CartItemDto itemDto = new CartItemDto();
                itemDto.setProduct(ProductMapper.toDto(product));
                itemDto.setQuantity(quantity);
                dto.getItems().add(itemDto);
                total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            }
        }

        dto.setTotalPrice(total.doubleValue());
        return dto;
    }

    /**
     * Add or update an item quantity in a user's Redis cart hash.
     * @param username user lookup key
     * @param request cart item modification request
     * @return updated cart DTO
     */
    @Override
    public CartDto updateCartItem(String username, CartItemRequestDto request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.PRODUCT_NOT_FOUND));

        if (product.getStock() < request.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.INSUFFICIENT_STOCK);
        }

        String cartKey = CART_KEY_PREFIX + username;
        String field = request.getProductId().toString();

        if (request.getQuantity() <= 0) {
            redisTemplate.opsForHash().delete(cartKey, field);
        } else {
            redisTemplate.opsForHash().put(cartKey, field, String.valueOf(request.getQuantity()));
        }

        return getCart(username);
    }

    /**
     * Remove an item from a user's Redis cart hash.
     * @param username user lookup key
     * @param productId target product to remove
     * @return updated cart DTO
     */
    @Override
    public CartDto removeCartItem(String username, UUID productId) {
        String cartKey = CART_KEY_PREFIX + username;
        redisTemplate.opsForHash().delete(cartKey, productId.toString());
        return getCart(username);
    }

    /**
     * Clear all items from a user's Redis cart by deleting the cart key entirely.
     * @param username user lookup key
     */
    @Override
    public void clearCart(String username) {
        redisTemplate.delete(CART_KEY_PREFIX + username);
    }
}
