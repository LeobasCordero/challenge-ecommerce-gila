package com.gila.ecommerce.repository;

import com.gila.ecommerce.model.OrderItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing OrderItem database entities.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    /**
     * Retrieve products ordered by total quantity sold.
     * @param pageable pagination limit (e.g. limit to 1)
     * @return list of products ordered descending by sales volume
     */
    @org.springframework.data.jpa.repository.Query("SELECT oi.product FROM OrderItem oi GROUP BY oi.product ORDER BY SUM(oi.quantity) DESC")
    java.util.List<com.gila.ecommerce.model.Product> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);
}
