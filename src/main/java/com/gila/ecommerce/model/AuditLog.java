package com.gila.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * JPA entity representing system audit log records.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime timestamp;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;

    @Column(nullable = false, length = 50)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details")
    private Map<String, Object> details;

    /**
     * Retrieve audit log UUID.
     * @return unique log identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Set audit log UUID.
     * @param id unique log identifier
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Retrieve log creation timestamp.
     * @return log event timestamp
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Set log creation timestamp.
     * @param timestamp log event timestamp
     */
    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Retrieve triggering username.
     * @return user who triggered event
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set triggering username.
     * @param username user who triggered event
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieve action descriptor.
     * @return action category description
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Set action descriptor.
     * @param actionType action category description
     */
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    /**
     * Retrieve action completion status.
     * @return action status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set action completion status.
     * @param status action status string
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Retrieve structured metadata map.
     * @return metadata details map
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Set structured metadata map.
     * @param details metadata details map
     */
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
