package com.gila.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gila.ecommerce.dto.ImportProducts202Response;
import com.gila.ecommerce.dto.ProductImportStatusDto;
import com.gila.ecommerce.exception.ErrorMessages;
import com.gila.ecommerce.util.ImportStatus;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class ProductImportServiceImplTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductImportServiceImpl productImportService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(productImportService, "importRequestTopic", "import-request-topic");
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testImportProducts_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "products.csv", "text/csv", "name,price,stock,category\nProduct,10.0,5,Books".getBytes()
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        ImportProducts202Response response = productImportService.importProducts(file);

        assertNotNull(response);
        assertEquals(ImportStatus.PENDING.getValue(), response.getStatus());
        assertNotNull(response.getTaskId());
        verify(kafkaTemplate, times(1)).send(eq("import-request-topic"), anyString(), anyString());
    }

    @Test
    public void testImportProducts_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productImportService.importProducts(emptyFile);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorMessages.FILE_EMPTY, exception.getReason());
    }

    @Test
    public void testGetImportStatus_NotFound() {
        UUID randomId = UUID.randomUUID();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            productImportService.getImportStatus(randomId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ErrorMessages.TASK_NOT_FOUND, exception.getReason());
    }

    @Test
    public void testGetAndUpdateStatus() {
        UUID taskId = UUID.randomUUID();
        ProductImportStatusDto statusDto = new ProductImportStatusDto();
        statusDto.setTaskId(taskId);
        statusDto.setStatus(ImportStatus.COMPLETED.getValue());

        productImportService.updateStatus(taskId, statusDto);
        ProductImportStatusDto retrieved = productImportService.getImportStatus(taskId);

        assertNotNull(retrieved);
        assertEquals(ImportStatus.COMPLETED.getValue(), retrieved.getStatus());
    }
}
