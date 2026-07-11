package com.gila.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service implementation publishing audit log events asynchronously to a Kafka topic.
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injecting dependencies.
     * @param kafkaTemplate kafka Template utility
     * @param objectMapper jackson ObjectMapper reference
     */
    public AuditLogServiceImpl(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Log a security or core business transaction.
     * @param username user who initiated the event
     * @param actionType category of action performed
     * @param status status of action (e.g. SUCCESS, FAILURE)
     * @param details structured map containing metadata details
     */
    @Override
    public void log(
            String username,
            String actionType,
            String status,
            Map<String, Object> details
    ) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("id", UUID.randomUUID().toString());
            event.put("timestamp", OffsetDateTime.now().toString());
            event.put("username", username);
            event.put("actionType", actionType);
            event.put("status", status);
            event.put("details", details);

            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("audit-log", payload);
        } catch (Exception e) {
            // fail-safe logging to avoid breaking main business processes
        }
    }
}
