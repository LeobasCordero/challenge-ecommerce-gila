package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.model.Product;
import java.math.BigDecimal;

/**
 * Mapper helper class for converting between Product entities and ProductDto models.
 */
public class ProductMapper {

    /**
     * Convert a Product JPA entity into a ProductDto DTO.
     * @param entity source Product database record
     * @return populated ProductDto model
     */
    public static ProductDto toDto(Product entity) {
        ProductDto dto = new ProductDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice().doubleValue());
        dto.setStock(entity.getStock());
        dto.setCategory(entity.getCategory());
        return dto;
    }

    /**
     * Convert a ProductDto DTO into a Product JPA entity.
     * @param dto source ProductDto model
     * @return populated Product database record
     */
    public static Product toEntity(ProductDto dto) {
        Product entity = new Product();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        if (dto.getPrice() != null) {
            entity.setPrice(BigDecimal.valueOf(dto.getPrice()));
        }
        entity.setStock(dto.getStock());
        entity.setInitialStock(dto.getStock());
        entity.setCategory(dto.getCategory());
        return entity;
    }
}
