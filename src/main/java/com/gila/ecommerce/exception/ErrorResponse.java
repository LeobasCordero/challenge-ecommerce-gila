package com.gila.ecommerce.exception;

import java.time.OffsetDateTime;

/**
 * Standard data carrier model representing structured REST API error responses.
 */
public class ErrorResponse {

    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * Retrieve error timestamp.
     * @return timestamp when error occurred
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Set error timestamp.
     * @param timestamp timestamp when error occurred
     */
    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Retrieve HTTP status code integer.
     * @return HTTP status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set HTTP status code integer.
     * @param status HTTP status code
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Retrieve HTTP status standard phrase.
     * @return standard error name
     */
    public String getError() {
        return error;
    }

    /**
     * Set HTTP status standard phrase.
     * @param error standard error name
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Retrieve detail error message reason.
     * @return custom explanation message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set detail error message reason.
     * @param message custom explanation message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Retrieve API request endpoint path.
     * @return request path context
     */
    public String getPath() {
        return path;
    }

    /**
     * Set API request endpoint path.
     * @param path request path context
     */
    public void setPath(String path) {
        this.path = path;
    }
}
