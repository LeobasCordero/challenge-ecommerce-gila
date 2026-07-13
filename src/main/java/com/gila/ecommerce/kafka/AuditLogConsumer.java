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
 * Kafka listener consuming audit events and saving them to the database.
 */
@Component
public class AuditLogConsumer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuditLogConsumer.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injecting dependencies.
     * @param auditLogRepository audit log repository
     * @param objectMapper json serialization mapper
     */
    public AuditLogConsumer(
            AuditLogRepository auditLogRepository,
            ObjectMapper objectMapper
    ) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Consume audit log event and persist to database.
     * @param message raw JSON payload from Kafka
     */
    @KafkaListener(topics = "${app.kafka.topics.audit-log}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAuditLog(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(
                    message, new TypeReference<Map<String, Object>>() {}
            );

            AuditLog logEntity = new AuditLog();
            logEntity.setId(UUID.fromString((String) payload.get("id")));
            logEntity.setUsername((String) payload.get("username"));
            logEntity.setActionType((String) payload.get("action"));
            logEntity.setStatus((String) payload.get("status"));
            logEntity.setTimestamp(OffsetDateTime.parse((String) payload.get("timestamp")));

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) payload.get("metadata");
            logEntity.setDetails(metadata);

            auditLogRepository.save(logEntity);
        } catch (Exception e) {
            log.error("Failed to parse or save audit log message: {}", message, e);
        }
    }
}
