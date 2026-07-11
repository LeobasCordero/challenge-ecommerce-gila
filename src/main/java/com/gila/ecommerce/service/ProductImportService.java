package com.gila.ecommerce.service;

import com.gila.ecommerce.dto.ImportProducts202Response;
import com.gila.ecommerce.dto.ProductImportStatusDto;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface defining operations for importing products from files asynchronously.
 */
public interface ProductImportService {

    /**
     * Submit a file import request task asynchronously.
     * @param file uploaded CSV multipart resource
     * @return transaction taskId reference and pending status
     */
    ImportProducts202Response importProducts(MultipartFile file);

    /**
     * Retrieve current processing logs and execution status of an import task.
     * @param taskId import task identifier
     * @return detailed task status report
     */
    ProductImportStatusDto getImportStatus(UUID taskId);

    /**
     * Update execution status details of an active import task.
     * @param taskId import task identifier
     * @param statusDto updated status report details
     */
    void updateStatus(UUID taskId, ProductImportStatusDto statusDto);
}
