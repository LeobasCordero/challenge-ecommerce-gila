package com.gila.ecommerce.aspect;

import com.gila.ecommerce.dto.LoginRequestDto;
import com.gila.ecommerce.dto.OrderDto;
import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.service.AuditLogService;
import com.gila.ecommerce.util.AuditAction;
import com.gila.ecommerce.util.AuditStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Aspect-Oriented Programming (AOP) component intercepting methods annotated with {@link Auditable}.
 */
@Aspect
@Component
public class AuditAspect {

    private final AuditLogService auditLogService;

    /**
     * Constructor injecting dependency.
     * @param auditLogService audit logging service interface
     */
    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Intercept method execution and log outcome status and metadata.
     * @param joinPoint joint point representing method call execution
     * @param auditable auditable configuration parameters
     * @return original method return object value
     * @throws Throwable exception thrown by target method execution
     */
    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        AuditAction action = auditable.action();
        String username = getUsername(joinPoint, action);
        try {
            Object result = joinPoint.proceed();
            Map<String, Object> metadata = getSuccessMetadata(joinPoint, action, result);
            auditLogService.log(username, action.getValue(), AuditStatus.SUCCESS.getValue(), metadata);
            return result;
        } catch (Throwable ex) {
            Map<String, Object> metadata = getFailureMetadata(ex);
            auditLogService.log(username, action.getValue(), AuditStatus.FAILURE.getValue(), metadata);
            throw ex;
        }
    }

    /**
     * Resolve username from request arguments or security context.
     * @param joinPoint join point representation
     * @param action target action
     * @return username triggering request
     */
    private String getUsername(ProceedingJoinPoint joinPoint, AuditAction action) {
        Object[] args = joinPoint.getArgs();
        if (action == AuditAction.LOGIN && args.length > 0 && args[0] instanceof LoginRequestDto) {
            return ((LoginRequestDto) args[0]).getUsername();
        }
        if (action == AuditAction.CHECKOUT && args.length > 0 && args[0] instanceof String) {
            return (String) args[0];
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "system";
    }

    /**
     * Compile transaction details on successful executions.
     * @param joinPoint join point representation
     * @param action target action
     * @param result target return value
     * @return metadata details
     */
    private Map<String, Object> getSuccessMetadata(ProceedingJoinPoint joinPoint, AuditAction action, Object result) {
        Map<String, Object> metadata = new HashMap<>();
        if (action == AuditAction.CHECKOUT && result instanceof OrderDto) {
            OrderDto order = (OrderDto) result;
            metadata.put("orderId", order.getId().toString());
            metadata.put("totalPrice", order.getTotalPrice());
        } else if ((action == AuditAction.PRODUCT_CREATE || action == AuditAction.PRODUCT_UPDATE) && result instanceof ProductDto) {
            ProductDto prod = (ProductDto) result;
            metadata.put("productId", prod.getId().toString());
            metadata.put("name", prod.getName());
        } else if (action == AuditAction.PRODUCT_DELETE) {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0 && args[0] instanceof UUID) {
                metadata.put("productId", args[0].toString());
            }
        }
        return metadata;
    }

    /**
     * Compile failure execution logs.
     * @param ex target exception details
     * @return metadata containing reason
     */
    private Map<String, Object> getFailureMetadata(Throwable ex) {
        Map<String, Object> metadata = new HashMap<>();
        if (ex instanceof ResponseStatusException) {
            metadata.put("reason", ((ResponseStatusException) ex).getReason());
        } else {
            metadata.put("reason", ex.getMessage());
        }
        return metadata;
    }
}
