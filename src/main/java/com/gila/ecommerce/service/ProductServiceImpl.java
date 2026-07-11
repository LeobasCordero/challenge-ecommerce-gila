package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.model.Product;
import com.gila.ecommerce.repository.ProductRepository;
import com.gila.ecommerce.util.AuditAction;
import com.gila.ecommerce.util.AuditStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service implementation managing product catalog database operations.
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    /**
     * Constructor injecting ProductRepository and AuditLogService.
     * @param productRepository product catalog database interface
     * @param auditLogService audit logging service interface
     */
    public ProductServiceImpl(
            ProductRepository productRepository,
            AuditLogService auditLogService
    ) {
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    /**
     * Create a new product.
     * @param productDto details of product to create
     * @return the created product details
     */
    @Override
    public ProductDto createProduct(ProductDto productDto) {
        if (productRepository.findByName(productDto.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name already exists");
        }
        Product product = ProductMapper.toEntity(productDto);
        if (product.getId() == null) {
            product.setId(UUID.randomUUID());
        }
        Product saved = productRepository.save(product);

        auditLogService.log(
                getAuthenticatedUsername(),
                AuditAction.PRODUCT_CREATE.getValue(),
                AuditStatus.SUCCESS.getValue(),
                Map.of("productId", saved.getId().toString(), "name", saved.getName())
        );

        return ProductMapper.toDto(saved);
    }

    /**
     * Retrieve details of a singular product by id.
     * @param id target product identifier
     * @return product details DTO
     */
    @Override
    public ProductDto getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return ProductMapper.toDto(product);
    }

    /**
     * Retrieve list of products with filters and pagination.
     * @param query search query
     * @param category category filter
     * @param page page number
     * @param size page limit size
     * @return list of matching products
     */
    @Override
    public List<ProductDto> getProducts(
            String query,
            String category,
            Integer page,
            Integer size
    ) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 20;
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return productRepository.findProductsByFilter(query, category, pageable)
                .getContent().stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Update details of an existing product.
     * @param id target product identifier
     * @param productDto updated product values
     * @return updated product details
     */
    @Override
    public ProductDto updateProduct(UUID id, ProductDto productDto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        existing.setName(productDto.getName());
        existing.setDescription(productDto.getDescription());
        if (productDto.getPrice() != null) {
            existing.setPrice(BigDecimal.valueOf(productDto.getPrice()));
        }
        existing.setStock(productDto.getStock());
        existing.setCategory(productDto.getCategory());
        Product saved = productRepository.save(existing);

        auditLogService.log(
                getAuthenticatedUsername(),
                AuditAction.PRODUCT_UPDATE.getValue(),
                AuditStatus.SUCCESS.getValue(),
                Map.of("productId", saved.getId().toString(), "name", saved.getName())
        );

        return ProductMapper.toDto(saved);
    }

    /**
     * Delete an existing product by id.
     * @param id target product identifier
     */
    @Override
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(id);

        auditLogService.log(
                getAuthenticatedUsername(),
                AuditAction.PRODUCT_DELETE.getValue(),
                AuditStatus.SUCCESS.getValue(),
                Map.of("productId", id.toString())
        );
    }

    /**
     * Helper to resolve authenticated username from SecurityContext.
     * @return username from principal if authenticated, "system" otherwise
     */
    private String getAuthenticatedUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }
}
