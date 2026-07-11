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
}
