package com.gila.ecommerce.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AuditLogServiceImplTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(auditLogService, "auditLogTopic", "audit-log-topic");
    }

    @Test
    public void testLog_Success() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"payload\"}");

        auditLogService.log("user1", "LOGIN", "SUCCESS", Map.of("ip", "127.0.0.1"));

        verify(objectMapper, times(1)).writeValueAsString(any());
        verify(kafkaTemplate, times(1)).send(eq("audit-log-topic"), anyString(), eq("{\"message\":\"payload\"}"));
    }

    @Test
    public void testLog_JacksonExceptionDoesNotThrow() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Mock serialization error") {});

        // Should not throw exception
        auditLogService.log("user1", "LOGIN", "SUCCESS", Map.of("ip", "127.0.0.1"));

        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
