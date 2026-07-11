package com.gila.ecommerce.repository;

import com.gila.ecommerce.model.AuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing AuditLog database entities.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
