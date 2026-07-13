package com.gila.ecommerce.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.dto.ProductImportStatusDto;
import com.gila.ecommerce.exception.ErrorMessages;
import com.gila.ecommerce.service.AuditLogService;
import com.gila.ecommerce.service.ProductImportService;
import com.gila.ecommerce.util.AuditAction;
import com.gila.ecommerce.util.AuditStatus;
import com.gila.ecommerce.util.ImportStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka listener consuming product import request events and executing imports.
 */
@Component
public class ProductImportConsumer {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProductImportConsumer.class);

    private final ProductImportService importService;
    private final ProductCsvParser csvParser;
    private final ProductRowSanitizer rowSanitizer;
    private final ProductImportProcessor importProcessor;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.import-status}")
    private String importStatusTopic;

    /**
     * Constructor injecting dependencies.
     * @param importService CSV import status service
     * @param csvParser OpenCSV parser
     * @param rowSanitizer validator/sanitizer
     * @param importProcessor DB persistent writer
     * @param kafkaTemplate kafka event publisher
     * @param auditLogService audit logging service interface
     * @param objectMapper JSON serialization mapper
     */
    public ProductImportConsumer(
            ProductImportService importService,
            ProductCsvParser csvParser,
            ProductRowSanitizer rowSanitizer,
            ProductImportProcessor importProcessor,
            KafkaTemplate<String, String> kafkaTemplate,
            AuditLogService auditLogService,
            ObjectMapper objectMapper
    ) {
        this.importService = importService;
        this.csvParser = csvParser;
        this.rowSanitizer = rowSanitizer;
        this.importProcessor = importProcessor;
        this.kafkaTemplate = kafkaTemplate;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * Consume import request event payload sequentially.
     * @param message JSON payload containing taskId and filePath
     */
    @KafkaListener(topics = "${app.kafka.topics.import-request}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeImportRequest(String message) {
        UUID taskId = null;
        String filePath = null;
        String username = "system";
        try {
            Map<String, String> event = objectMapper.readValue(
                    message, new TypeReference<Map<String, String>>() {}
            );
            taskId = UUID.fromString(event.get("taskId"));
            filePath = event.get("filePath");
            username = event.getOrDefault("username", "system");

            ProductImportStatusDto status = importService.getImportStatus(taskId);
            status.setStatus(ImportStatus.PROCESSING.getValue());
            importService.updateStatus(taskId, status);

            auditLogService.log(
                    username,
                    AuditAction.CSV_IMPORT_START.getValue(),
                    AuditStatus.SUCCESS.getValue(),
                    Map.of("taskId", taskId.toString())
            );

            List<String[]> rows = csvParser.parse(filePath);
            status.setTotalRows(rows.size());

            Map<String, Integer> headerMap = new HashMap<>();
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
                String headerLine = br.readLine();
                if (headerLine != null) {
                    String[] headers = headerLine.split("[\\t,;]");
                    for (int idx = 0; idx < headers.length; idx++) {
                        String col = headers[idx].replace("\"", "").replace("'", "").trim().toLowerCase();
                        headerMap.put(col, idx);
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to read CSV header for dynamic mapping, falling back to default column order", e);
            }

            List<ProductDto> validDtos = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            int errorCount = 0;

            for (int i = 0; i < rows.size(); i++) {
                SanitizedRow sanitized = rowSanitizer.sanitize(rows.get(i), i + 1, headerMap);
                warnings.addAll(sanitized.getWarnings());
                if (sanitized.isValid()) {
                    validDtos.add(sanitized.getProductDto());
                } else {
                    errorCount++;
                }
            }

            List<String> dbWarnings = importProcessor.process(validDtos);
            warnings.addAll(dbWarnings);

            status.setErrorCount(errorCount + dbWarnings.size());
            status.setProcessedRows(validDtos.size() - dbWarnings.size());
            status.setWarnings(warnings);
            status.setStatus(ImportStatus.COMPLETED.getValue());

            importService.updateStatus(taskId, status);
            kafkaTemplate.send(
                    java.util.Objects.requireNonNull(importStatusTopic), taskId.toString(),
                    objectMapper.writeValueAsString(status)
            );

            auditLogService.log(
                    username,
                    AuditAction.CSV_IMPORT_COMPLETE.getValue(),
                    AuditStatus.SUCCESS.getValue(),
                    Map.of("taskId", taskId.toString(), "processedRows", status.getProcessedRows())
            );

        } catch (Exception e) {
            if (taskId != null) {
                try {
                    ProductImportStatusDto status = importService.getImportStatus(taskId);
                    status.setStatus(ImportStatus.FAILED.getValue());
                    status.getWarnings().add(ErrorMessages.CRITICAL_SYSTEM_ERROR_PREFIX + e.getMessage());
                    importService.updateStatus(taskId, status);
                    kafkaTemplate.send(
                            java.util.Objects.requireNonNull(importStatusTopic), taskId.toString(),
                            objectMapper.writeValueAsString(status)
                    );
                } catch (Exception ex) {
                    log.error("Failed to update status to FAILED in secondary catch block", ex);
                }
            }
            auditLogService.log(
                    username,
                    AuditAction.CSV_IMPORT_FAILED.getValue(),
                    AuditStatus.FAILURE.getValue(),
                    Map.of("error", e.getMessage())
            );
        } finally {
            if (filePath != null) {
                try {
                    Files.deleteIfExists(Paths.get(filePath));
                } catch (IOException e) {
                    log.warn("Failed to delete CSV file: {}", filePath, e);
                }
            }
        }
    }
}
