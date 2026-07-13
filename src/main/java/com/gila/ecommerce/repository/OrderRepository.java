package com.gila.ecommerce.repository;

import com.gila.ecommerce.model.Order;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Order database entities.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Calculate total revenue since a specific timestamp.
     * @param start start timestamp boundary
     * @return total revenue decimal sum
     */
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.createdAt >= :start")
    java.math.BigDecimal calculateRevenueSince(@org.springframework.data.repository.query.Param("start") java.time.OffsetDateTime start);

    /**
     * Count total checkout order transactions since a specific timestamp.
     * @param start start timestamp boundary
     * @return total orders count
     */
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :start")
    long countOrdersSince(@org.springframework.data.repository.query.Param("start") java.time.OffsetDateTime start);
}
