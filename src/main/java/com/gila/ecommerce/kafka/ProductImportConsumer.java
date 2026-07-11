package com.gila.ecommerce.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.dto.ProductImportStatusDto;
import com.gila.ecommerce.service.ProductImportService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka listener consuming product import request events and executing imports.
 */
@Component
public class ProductImportConsumer {

    private final ProductImportService importService;
    private final ProductCsvParser csvParser;
    private final ProductRowSanitizer rowSanitizer;
    private final ProductImportProcessor importProcessor;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructor injecting dependencies.
     * @param importService CSV import status service
     * @param csvParser OpenCSV parser
     * @param rowSanitizer validator/sanitizer
     * @param importProcessor DB persistent writer
     * @param kafkaTemplate kafka event publisher
     * @param objectMapper JSON serialization mapper
     */
    public ProductImportConsumer(
            ProductImportService importService,
            ProductCsvParser csvParser,
            ProductRowSanitizer rowSanitizer,
            ProductImportProcessor importProcessor,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.importService = importService;
        this.csvParser = csvParser;
        this.rowSanitizer = rowSanitizer;
        this.importProcessor = importProcessor;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Consume import request event payload sequentially.
     * @param message JSON payload containing taskId and filePath
     */
    @KafkaListener(topics = "product-import-request", groupId = "ecommerce-group")
    public void consumeImportRequest(String message) {
        UUID taskId = null;
        String filePath = null;
        try {
            Map<String, String> event = objectMapper.readValue(
                    message, new TypeReference<Map<String, String>>() {}
            );
            taskId = UUID.fromString(event.get("taskId"));
            filePath = event.get("filePath");

            ProductImportStatusDto status = importService.getImportStatus(taskId);
            status.setStatus("PROCESSING");
            importService.updateStatus(taskId, status);

            List<String[]> rows = csvParser.parse(filePath);
            status.setTotalRows(rows.size());

            List<ProductDto> validDtos = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            int errorCount = 0;

            for (int i = 0; i < rows.size(); i++) {
                SanitizedRow sanitized = rowSanitizer.sanitize(rows.get(i), i + 1);
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
            status.setStatus("COMPLETED");

            importService.updateStatus(taskId, status);
            kafkaTemplate.send(
                    "product-import-status", taskId.toString(),
                    objectMapper.writeValueAsString(status)
            );

        } catch (Exception e) {
            if (taskId != null) {
                try {
                    ProductImportStatusDto status = importService.getImportStatus(taskId);
                    status.setStatus("FAILED");
                    status.getWarnings().add("Critical system error: " + e.getMessage());
                    importService.updateStatus(taskId, status);
                    kafkaTemplate.send(
                            "product-import-status", taskId.toString(),
                            objectMapper.writeValueAsString(status)
                    );
                } catch (Exception ex) {
                    // fall through
                }
            }
        } finally {
            if (filePath != null) {
                try {
                    Files.deleteIfExists(Paths.get(filePath));
                } catch (IOException e) {
                    // fall through
                }
            }
        }
    }
}
