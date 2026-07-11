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
}
