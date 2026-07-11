package com.gila.ecommerce.kafka;

import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.repository.ProductRepository;
import com.gila.ecommerce.service.ProductMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Component processing database writes for lists of sanitized product models.
 */
@Component
public class ProductImportProcessor {

    private final ProductRepository productRepository;

    /**
     * Constructor injecting ProductRepository.
     * @param productRepository product catalog database interface
     */
    public ProductImportProcessor(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Process list of product DTOs, checking duplicates and saving new products to DB.
     * @param dtos list of product DTOs to import
     * @return list of warnings/logs generated during import execution
     */
    @Transactional
    public List<String> process(List<ProductDto> dtos) {
        List<String> warnings = new ArrayList<>();
        for (ProductDto dto : dtos) {
            if (productRepository.findByName(dto.getName()).isPresent()) {
                warnings.add("Product with name '" + dto.getName()
                        + "' already exists. Duplicate ignored.");
                continue;
            }
            Product product = ProductMapper.toEntity(dto);
            if (product.getId() == null) {
                product.setId(UUID.randomUUID());
            }
            productRepository.save(product);
        }
        return warnings;
    }
}
