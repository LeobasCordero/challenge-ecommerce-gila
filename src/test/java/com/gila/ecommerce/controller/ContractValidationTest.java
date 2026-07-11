package com.gila.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.OrderDto;
import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.security.CustomUserDetailsService;
import com.gila.ecommerce.security.JwtTokenProvider;
import com.gila.ecommerce.service.CartService;
import com.gila.ecommerce.service.CheckoutService;
import com.gila.ecommerce.service.ProductImportService;
import com.gila.ecommerce.service.ProductService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc contract validation test suite.
 */
@WebMvcTest({AuthController.class, ProductController.class, CartController.class, OrderController.class})
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "customer")
public class ContractValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ProductImportService productImportService;

    @MockBean
    private CartService cartService;

    @MockBean
    private CheckoutService checkoutService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    /**
     * Verify auth login endpoint maps correctly and returns JWT.
     */
    @Test
    public void testAuthLoginContract() throws Exception {
        String payload = "{\"username\":\"admin\",\"password\":\"password\"}";
        Authentication mockAuth = new UsernamePasswordAuthenticationToken("admin", "password");
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtTokenProvider.generateToken("admin")).thenReturn("mock-jwt-token-for-testing");

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
        ProductDto item = new ProductDto();
        item.setId(UUID.randomUUID());
        item.setName("Sample Item");
        item.setPrice(19.99);
        item.setStock(10);
        item.setCategory("Electronics");

        when(productService.getProducts(any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(item));

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
        ProductDto created = new ProductDto();
        created.setId(UUID.randomUUID());
        created.setName("New Item");
        created.setPrice(99.99);
        created.setStock(5);
        created.setCategory("Home");

        when(productService.createProduct(any(ProductDto.class))).thenReturn(created);

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
        CartDto cart = new CartDto();
        cart.setItems(new ArrayList<>());
        cart.setTotalPrice(0.0);

        when(cartService.getCart(anyString())).thenReturn(cart);

        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(0.0));
    }

    /**
     * Verify POST orders checkout processes successfully.
     */
    @Test
    public void testCheckoutContract() throws Exception {
        OrderDto order = new OrderDto();
        order.setId(UUID.randomUUID());
        order.setStatus("PAID");
        order.setTotalPrice(199.99);
        order.setItems(new ArrayList<>());

        when(checkoutService.checkout(anyString())).thenReturn(order);

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
