package com.gila.ecommerce.kafka;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductImportProcessorTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductImportProcessor productImportProcessor;

    @Test
    public void testProcess_NewProductSuccess() {
        ProductDto dto = new ProductDto();
        dto.setName("New Brand");
        dto.setPrice(12.50);
        dto.setStock(10);
        dto.setCategory("Grocery");

        when(productRepository.findByName(dto.getName())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> warnings = productImportProcessor.process(Collections.singletonList(dto));

        assertTrue(warnings.isEmpty());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testProcess_DuplicateProductSkipped() {
        ProductDto dto = new ProductDto();
        dto.setName("Existing Product");
        dto.setPrice(12.50);
        dto.setStock(10);
        dto.setCategory("Grocery");

        Product existingProduct = new Product();
        existingProduct.setName("Existing Product");

        when(productRepository.findByName(dto.getName())).thenReturn(Optional.of(existingProduct));

        List<String> warnings = productImportProcessor.process(Collections.singletonList(dto));

        assertFalse(warnings.isEmpty());
        assertTrue(warnings.get(0).contains(ErrorMessages.DUPLICATE_PRODUCT_PREFIX));
        verify(productRepository, never()).save(any(Product.class));
    }
}
