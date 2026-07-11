package com.gila.ecommerce.repository;

import com.gila.ecommerce.model.Product;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Product database entities.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Retrieve a product by name.
     * @param name product name lookup key
     * @return optional containing the product if found
     */
    Optional<Product> findByName(String name);

    /**
     * Retrieve product catalog items matching a text query and category with pagination.
     * @param query search query matching name or description
     * @param category category filter value
     * @param pageable pagination options
     * @return page of matching product results
     */
    @Query("SELECT p FROM Product p WHERE "
            + "(:category IS NULL OR TRIM(:category) = '' OR LOWER(p.category) = LOWER(TRIM(:category))) AND "
            + "(:query IS NULL OR TRIM(:query) = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', TRIM(:query), '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', TRIM(:query), '%')))")
    Page<Product> findProductsByFilter(
            @Param("query") String query,
            @Param("category") String category,
            Pageable pageable
    );

    /**
     * Retrieve a product with a database-level pessimistic write lock.
     * @param id target product identifier
     * @return optional containing the locked product if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findWithLockById(@Param("id") UUID id);
}
