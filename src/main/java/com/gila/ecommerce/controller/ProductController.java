package com.gila.ecommerce.controller;

import com.gila.ecommerce.api.ProductsApi;
import com.gila.ecommerce.dto.ImportProducts202Response;
import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.dto.ProductImportStatusDto;
import com.gila.ecommerce.service.ProductImportService;
import com.gila.ecommerce.service.ProductService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller implementing products REST catalog endpoints.
 */
@RestController
public class ProductController implements ProductsApi {

    private final ProductService productService;
    private final ProductImportService productImportService;

    /**
     * Constructor injecting dependencies.
     * @param productService product catalog business service
     * @param productImportService CSV import pipeline service
     */
    public ProductController(
            ProductService productService,
            ProductImportService productImportService
    ) {
        this.productService = productService;
        this.productImportService = productImportService;
    }

    /**
     * Create a new product.
     * @param productDto details of product to create
     * @return response containing the created product details
     */
    @Override
    public ResponseEntity<ProductDto> createProduct(ProductDto productDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productDto));
    }

    /**
     * Delete an existing product by id.
     * @param id target product identifier
     * @return empty response entity with 204 status
     */
    @Override
    public ResponseEntity<Void> deleteProduct(UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve status of a products CSV import request.
     * @param taskId CSV import task identifier
     * @return status report of the import process
     */
    @Override
    public ResponseEntity<ProductImportStatusDto> getImportStatus(UUID taskId) {
        return ResponseEntity.ok(productImportService.getImportStatus(taskId));
    }

    /**
     * Retrieve details of a singular product by id.
     * @param id target product identifier
     * @return product details DTO
     */
    @Override
    public ResponseEntity<ProductDto> getProductById(UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Retrieve list of products with filters.
     * @param query search query
     * @param category category filter
     * @param page page number
     * @param size page limit size
     * @return list of matching products
     */
    @Override
    public ResponseEntity<List<ProductDto>> getProducts(
            String query,
            String category,
            Integer page,
            Integer size
    ) {
        return ResponseEntity.ok(productService.getProducts(query, category, page, size));
    }

    /**
     * Import products from a CSV file asynchronously.
     * @param file CSV file input stream resource
     * @return transaction reference taskId
     */
    @Override
    public ResponseEntity<ImportProducts202Response> importProducts(MultipartFile file) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(productImportService.importProducts(file));
    }

    /**
     * Update details of an existing product.
     * @param id target product identifier
     * @param productDto updated product values
     * @return updated product details
     */
    @Override
    public ResponseEntity<ProductDto> updateProduct(UUID id, ProductDto productDto) {
        return ResponseEntity.ok(productService.updateProduct(id, productDto));
    }
}
