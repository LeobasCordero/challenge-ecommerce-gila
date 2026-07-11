package com.gila.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.exception.ErrorMessages;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDto productDto;
    private UUID productId;

    @BeforeEach
    public void setUp() {
        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(10.99));
        product.setStock(100);
        product.setCategory("Electronics");

        productDto = new ProductDto();
        productDto.setId(productId);
        productDto.setName("Test Product");
        productDto.setDescription("Test Description");
        productDto.setPrice(10.99);
        productDto.setStock(100);
        productDto.setCategory("Electronics");
    }

    @Test
    public void testCreateProduct_Success() {
        when(productRepository.findByName(productDto.getName())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.createProduct(productDto);

        assertNotNull(result);
        assertEquals(productDto.getName(), result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testCreateProduct_AlreadyExists() {
        when(productRepository.findByName(productDto.getName())).thenReturn(Optional.of(product));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productService.createProduct(productDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessages.PRODUCT_NAME_EXISTS, exception.getReason());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testGetProductById_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductDto result = productService.getProductById(productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
    }

    @Test
    public void testGetProductById_NotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productService.getProductById(productId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ErrorMessages.PRODUCT_NOT_FOUND, exception.getReason());
    }

    @Test
    public void testGetProducts() {
        PageImpl<Product> page = new PageImpl<>(Collections.singletonList(product));
        when(productRepository.findProductsByFilter(any(), any(), any(Pageable.class))).thenReturn(page);

        List<ProductDto> results = productService.getProducts("Test", "Electronics", 0, 10);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Product", results.get(0).getName());
    }

    @Test
    public void testUpdateProduct_Success() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto updatedDto = new ProductDto();
        updatedDto.setName("Updated Product");
        updatedDto.setDescription("Updated Description");
        updatedDto.setPrice(15.99);
        updatedDto.setStock(50);
        updatedDto.setCategory("Books");

        ProductDto result = productService.updateProduct(productId, updatedDto);

        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testUpdateProduct_NotFound() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productService.updateProduct(productId, productDto);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ErrorMessages.PRODUCT_NOT_FOUND, exception.getReason());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    public void testDeleteProduct_Success() {
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        assertDoesNotThrow(() -> productService.deleteProduct(productId));

        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    public void testDeleteProduct_NotFound() {
        when(productRepository.existsById(productId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productService.deleteProduct(productId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ErrorMessages.PRODUCT_NOT_FOUND, exception.getReason());
        verify(productRepository, never()).deleteById(any());
    }
}
