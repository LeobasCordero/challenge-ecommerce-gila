package com.gila.ecommerce.controller;

import com.gila.ecommerce.api.OrdersApi;
import com.gila.ecommerce.dto.OrderDto;
import com.gila.ecommerce.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementing checkout and administrative reset endpoints.
 */
@RestController
public class OrderController implements OrdersApi {

    private final CheckoutService checkoutService;

    /**
     * Constructor injecting CheckoutService.
     * @param checkoutService transactional checkout business service
     */
    public OrderController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    /**
     * Complete checkout and generate a billing record.
     * @return checkout order transaction details
     */
    @Override
    public ResponseEntity<OrderDto> checkout() {
        return ResponseEntity.ok(checkoutService.checkout(getAuthenticatedUsername()));
    }

    /**
     * Clear orders database history and restore initial product stock settings.
     * @return empty response entity with 204 status
     */
    @Override
    public ResponseEntity<Void> clearOrders() {
        checkoutService.clearOrders();
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve authenticated username context.
     * @return username key from security principal
     */
    private String getAuthenticatedUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
