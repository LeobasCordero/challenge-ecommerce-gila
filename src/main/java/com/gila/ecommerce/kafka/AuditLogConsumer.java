package com.gila.ecommerce.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gila.ecommerce.model.AuditLog;
import com.gila.ecommerce.repository.AuditLogRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener consuming audit event payloads and persisting records.
 */
@Component
public class AuditLogConsumer {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injecting repository and mapper.
     * @param auditLogRepository audit log database interface
     * @param objectMapper json mapper instance
     */
    public AuditLogConsumer(
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper
    ) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Consume audit event and save to the database.
     * @param message JSON event payload
     */
    @SuppressWarnings("unchecked")
    @KafkaListener(topics = "audit-log", groupId = "ecommerce-group")
    public void consumeAuditLog(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(
                    message, new TypeReference<Map<String, Object>>() {}
            );

            AuditLog log = new AuditLog();
            log.setId(UUID.fromString((String) event.get("id")));
            log.setTimestamp(OffsetDateTime.parse((String) event.get("timestamp")));
            log.setUsername((String) event.get("username"));
            log.setActionType((String) event.get("actionType"));
            log.setStatus((String) event.get("status"));
            log.setDetails((Map<String, Object>) event.get("details"));

            auditLogRepository.save(log);
        } catch (Exception e) {
            // fail-safe logging consumer to prevent system message listener blocks
        }
    }
}
