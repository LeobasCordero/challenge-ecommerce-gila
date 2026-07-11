package com.gila.ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service implementation publishing audit log events to the designated Kafka topic.
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.audit-log}")
    private String auditLogTopic;

    /**
     * Constructor injecting KafkaTemplate and ObjectMapper.
     * @param kafkaTemplate kafka template instance
     * @param objectMapper JSON serialization mapper
     */
    public AuditLogServiceImpl(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish an audit event payload asynchronously.
     * @param username user who performed the action
     * @param action string identifier representing action category
     * @param status transaction success status level
     * @param metadata map of arbitrary key-value details relating to operation context
     */
    @Override
    public void log(
            String username,
            String action,
            String status,
            Map<String, Object> metadata
    ) {
        String messageId = UUID.randomUUID().toString();
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", messageId);
        payload.put("username", username);
        payload.put("action", action);
        payload.put("status", status);
        payload.put("timestamp", OffsetDateTime.now().toString());
        payload.put("metadata", metadata != null ? metadata : Map.of());

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(auditLogTopic, messageId, jsonPayload);
        } catch (JsonProcessingException e) {
            // fall through
        }
    }
}
