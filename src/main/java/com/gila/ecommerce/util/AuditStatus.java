package com.gila.ecommerce.util;

/**
 * Enum defining typesafe status values for system audit logs.
 */
public enum AuditStatus {
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE");

    private final String value;

    /**
     * Constructor setting string value.
     * @param value raw status level value
     */
    AuditStatus(String value) {
        this.value = value;
    }

    /**
     * Retrieve the raw status level value.
     * @return raw status value
     */
    public String getValue() {
        return value;
    }
}
