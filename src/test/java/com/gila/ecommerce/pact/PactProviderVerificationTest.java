package com.gila.ecommerce.pact;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.gila.ecommerce.controller.CartController;
import com.gila.ecommerce.controller.OrderController;
import com.gila.ecommerce.controller.ProductController;
import com.gila.ecommerce.dto.CartDto;
import com.gila.ecommerce.dto.CartItemDto;
import com.gila.ecommerce.dto.OrderDto;
import com.gila.ecommerce.dto.OrderItemDto;
import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.security.CustomUserDetailsService;
import com.gila.ecommerce.security.JwtTokenProvider;
import com.gila.ecommerce.service.AuditLogService;
import com.gila.ecommerce.service.CartService;
import com.gila.ecommerce.service.CheckoutService;
import com.gila.ecommerce.service.ProductImportService;
import com.gila.ecommerce.service.ProductService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.gila.ecommerce.controller.AuthController;
import com.gila.ecommerce.security.QueryMethodFilter;

/**
 * Pact provider verification test suite verifying Angular consumer contracts against backend controllers.
 */
@WebMvcTest({AuthController.class, ProductController.class, CartController.class, OrderController.class})
@AutoConfigureMockMvc(addFilters = false)
@Provider("GilaECommerceAPI")
@Consumer("GilaAngularConsumer")
@PactFolder("frontend/pacts")
public class PactProviderVerificationTest {

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
    private AuditLogService auditLogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private QueryMethodFilter queryMethodFilter;

    /**
     * Configure MockMvc test target and inject admin security context before each interaction.
     * @param context Pact verification context
     */
    @BeforeEach
    void setupTestTarget(PactVerificationContext context) {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new AuthController(authenticationManager, jwtTokenProvider),
                new ProductController(productService, productImportService),
                new CartController(cartService),
                new OrderController(checkoutService)
        ).build();
        context.setTarget(new MockMvcTestTarget(mvc));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("ROLE_CUSTOMER")
                        )
                )
        );
    }

    /**
     * Pact verification test template — runs each consumer interaction.
     * @param context Pact verification context
     */
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    /**
     * Set up product list state for GET /api/v1/products interaction.
     */
    @State("products exist")
    void setupProductsExistState() {
        ProductDto product = new ProductDto();
        product.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        product.setName("Test Product");
        product.setCategory("Electronics");
        product.setPrice(9.99);
        product.setStock(100);
        when(productService.getProducts(any(), any(), any(), any())).thenReturn(Collections.singletonList(product));
    }

    /**
     * Set up cart state with at least one item for GET /api/v1/cart interaction.
     */
    @State("cart has items")
    void setupCartHasItemsState() {
        CartItemDto cartItem = new CartItemDto();
        ProductDto itemProduct = new ProductDto();
        itemProduct.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        itemProduct.setName("Test Product");
        itemProduct.setPrice(9.99);
        cartItem.setProduct(itemProduct);
        cartItem.setQuantity(2);

        CartDto cartDto = new CartDto();
        cartDto.setItems(Collections.singletonList(cartItem));
        when(cartService.getCart(anyString())).thenReturn(cartDto);
    }

    /**
     * Set up state for updating a cart item quantity for PUT /api/v1/cart/items/{productId}.
     */
    @State("cart item can be updated")
    void setupCartItemCanBeUpdatedState() {
        CartItemDto cartItem = new CartItemDto();
        ProductDto itemProduct = new ProductDto();
        itemProduct.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        itemProduct.setName("Test Product");
        itemProduct.setPrice(9.99);
        cartItem.setProduct(itemProduct);
        cartItem.setQuantity(3);

        CartDto cartDto = new CartDto();
        cartDto.setItems(Collections.singletonList(cartItem));
        when(cartService.updateCartItem(anyString(), any())).thenReturn(cartDto);
    }

    /**
     * Set up state for checkout with a pre-loaded cart for POST /api/v1/orders/checkout.
     */
    @State("cart is ready for checkout")
    void setupCartReadyForCheckoutState() {
        OrderItemDto orderItem = new OrderItemDto();
        ProductDto itemProduct = new ProductDto();
        itemProduct.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        itemProduct.setName("Test Product");
        itemProduct.setPrice(9.99);
        orderItem.setProduct(itemProduct);
        orderItem.setQuantity(2);
        orderItem.setPriceAtPurchase(9.99);

        OrderDto order = new OrderDto();
        order.setId(UUID.fromString("550e8400-e29b-41d4-a716-446655440099"));
        order.setStatus("PAID");
        order.setTotalPrice(19.98);
        order.setItems(Collections.singletonList(orderItem));
        when(checkoutService.checkout(anyString())).thenReturn(order);
    }

    /**
     * Set up state with existing orders for DELETE /api/v1/orders/clear interaction.
     */
    @State("orders exist")
    void setupOrdersExistState() {
        doNothing().when(checkoutService).clearOrders();
    }
}
