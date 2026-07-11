package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.OrderDto;

/**
 * Service interface defining order checkout and resetting operations.
 */
public interface CheckoutService {

    /**
     * Complete order checkout for the active user's cart.
     * @param username user checking out
     * @return created order transaction details
     */
    OrderDto checkout(String username);

    /**
     * Reset order transaction history and restore product stocks.
     */
    void clearOrders();
}
