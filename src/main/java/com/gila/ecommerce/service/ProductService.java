package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.ProductDto;
import java.util.List;
import java.util.UUID;

/**
 * Service interface defining catalog product management operations.
 */
public interface ProductService {

    /**
     * Create a new product.
     * @param productDto details of product to create
     * @return the created product details
     */
    ProductDto createProduct(ProductDto productDto);

    /**
     * Retrieve details of a singular product by id.
     * @param id target product identifier
     * @return product details DTO
     */
    ProductDto getProductById(UUID id);

    /**
     * Retrieve list of products with filters and pagination.
     * @param query search query
     * @param category category filter
     * @param page page number
     * @param size page limit size
     * @return list of matching products
     */
    List<ProductDto> getProducts(String query, String category, Integer page, Integer size);

    /**
     * Update details of an existing product.
     * @param id target product identifier
     * @param productDto updated product values
     * @return updated product details
     */
    ProductDto updateProduct(UUID id, ProductDto productDto);

    /**
     * Delete an existing product by id.
     * @param id target product identifier
     */
    void deleteProduct(UUID id);
}
