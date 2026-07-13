package com.gila.ecommerce.controller;

import com.gila.ecommerce.api.AuditLogsApi;
import com.gila.ecommerce.dto.AuditLogDto;
import com.gila.ecommerce.service.AuditLogService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementing audit log query REST endpoints.
 */
@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController implements AuditLogsApi {

    private final AuditLogService auditLogService;

    /**
     * Constructor injecting AuditLogService.
     * @param auditLogService system log service manager
     */
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Retrieve paginated database audit logs.
     * @param page page number
     * @param size page capacity limit
     * @return response wrap containing list of logs
     */
    @Override
    public ResponseEntity<List<AuditLogDto>> getAuditLogs(Integer page, Integer size) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 20;
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return ResponseEntity.ok(auditLogService.getAuditLogs(pageable));
    }
}
