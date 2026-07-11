package com.gila.ecommerce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc contract validation test suite.
 */
@WebMvcTest({AuthController.class, ProductController.class, CartController.class, OrderController.class})
public class ContractValidationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Verify auth login endpoint maps correctly and returns JWT.
     */
    @Test
    public void testAuthLoginContract() throws Exception {
        String payload = "{\"username\":\"admin\",\"password\":\"password\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token-for-testing"));
    }

    /**
     * Verify GET products returns catalog results.
     */
    @Test
    public void testGetProductsContract() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sample Item"));
    }

    /**
     * Verify POST products creates a product.
     */
    @Test
    public void testCreateProductContract() throws Exception {
        String payload = "{\"name\":\"New Item\",\"price\":99.99,\"stock\":5,\"category\":\"Home\"}";
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Item"))
                .andExpect(jsonPath("$.id").exists());
    }

    /**
     * Verify GET shopping cart endpoint.
     */
    @Test
    public void testGetCartContract() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(0.0));
    }

    /**
     * Verify POST orders checkout processes successfully.
     */
    @Test
    public void testCheckoutContract() throws Exception {
        mockMvc.perform(post("/api/v1/orders/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    /**
     * Verify DELETE clear orders truncates correctly.
     */
    @Test
    public void testClearOrdersContract() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/clear"))
                .andExpect(status().isNoContent());
    }
}
