package com.gila.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gila.ecommerce.dto.ImportProducts202Response;
import com.gila.ecommerce.dto.ProductImportStatusDto;
import com.gila.ecommerce.exception.ErrorMessages;
import com.gila.ecommerce.util.ImportStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service implementation processing bulk CSV import request file saving and status checks.
 */
@Service
public class ProductImportServiceImpl implements ProductImportService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Map<UUID, ProductImportStatusDto> statuses = new ConcurrentHashMap<>();
    private final Path tempDir = Paths.get("temp-imports");

    @Value("${app.kafka.topics.import-request}")
    private String importRequestTopic;

    /**
     * Constructor injecting KafkaTemplate and ObjectMapper.
     * @param kafkaTemplate kafka template instance
     * @param objectMapper json mapper instance
     */
    public ProductImportServiceImpl(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Submit a file import request task asynchronously.
     * @param file uploaded CSV multipart resource
     * @return transaction taskId reference and pending status
     */
    @Override
    public ImportProducts202Response importProducts(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ErrorMessages.FILE_EMPTY);
        }
        UUID taskId = UUID.randomUUID();
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            Path filePath = tempDir.resolve(taskId + ".csv");
            file.transferTo(filePath.toFile());

            ProductImportStatusDto status = new ProductImportStatusDto();
            status.setTaskId(taskId);
            status.setStatus(ImportStatus.PENDING.getValue());
            status.setTotalRows(0);
            status.setProcessedRows(0);
            status.setErrorCount(0);
            status.setWarnings(new ArrayList<>());
            statuses.put(taskId, status);

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            Map<String, String> event = Map.of(
                    "taskId", taskId.toString(),
                    "filePath", filePath.toAbsolutePath().toString(),
                    "username", username
            );
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(importRequestTopic, taskId.toString(), payload);

            ImportProducts202Response response = new ImportProducts202Response();
            response.setTaskId(taskId);
            response.setStatus(ImportStatus.PENDING.getValue());
            return response;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.FILE_SAVE_FAILED + e.getMessage()
            );
        }
    }

    /**
     * Retrieve current processing logs and execution status of an import task.
     * @param taskId import task identifier
     * @return detailed task status report
     */
    @Override
    public ProductImportStatusDto getImportStatus(UUID taskId) {
        ProductImportStatusDto status = statuses.get(taskId);
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ErrorMessages.TASK_NOT_FOUND);
        }
        return status;
    }

    /**
     * Update execution status details of an active import task.
     * @param taskId import task identifier
     * @param statusDto updated status report details
     */
    @Override
    public void updateStatus(UUID taskId, ProductImportStatusDto statusDto) {
        statuses.put(taskId, statusDto);
    }
}
