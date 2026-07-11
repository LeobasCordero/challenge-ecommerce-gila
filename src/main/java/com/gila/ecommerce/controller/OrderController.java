package com.gila.ecommerce.controller;

import com.gila.ecommerce.api.OrdersApi;
import com.gila.ecommerce.dto.OrderDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Controller stub implementing checkout and administrative reset endpoints.
 */
@RestController
public class OrderController implements OrdersApi {

    /**
     * Complete checkout and generate a billing record.
     * @return checkout order transaction details
     */
    @Override
    public ResponseEntity<OrderDto> checkout() {
        OrderDto order = new OrderDto();
        order.setId(UUID.randomUUID());
        order.setStatus("PAID");
        order.setTotalPrice(199.99);
        order.setItems(new ArrayList<>());
        order.setCreatedAt(OffsetDateTime.now());
        return ResponseEntity.ok(order);
    }

    /**
     * Clear orders database history and restore initial product stock settings.
     * @return empty response entity with 204 status
     */
    @Override
    public ResponseEntity<Void> clearOrders() {
        return ResponseEntity.noContent().build();
    }
}
