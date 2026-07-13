package com.gila.ecommerce.service;

import java.util.Map;

/**
 * Service interface defining operations for publishing system audit log events.
 */
public interface AuditLogService {

    /**
     * Log a security or core business transaction.
     * @param username user who initiated the event
     * @param actionType category of action performed
     * @param status status of action (e.g. SUCCESS, FAILURE)
     * @param details structured map containing metadata details
     */
    void log(String username, String actionType, String status, Map<String, Object> details);

    /**
     * Retrieve paginated database audit logs.
     * @param pageable pagination parameters
     * @return list of audit log DTOs
     */
    java.util.List<com.gila.ecommerce.dto.AuditLogDto> getAuditLogs(org.springframework.data.domain.Pageable pageable);
}
