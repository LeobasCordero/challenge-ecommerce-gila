package com.gila.ecommerce.util;

/**
 * Enum defining typesafe status values for bulk CSV product import tasks.
 */
public enum ImportStatus {
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");

    private final String value;

    /**
     * Constructor setting string value.
     * @param value raw status value
     */
    ImportStatus(String value) {
        this.value = value;
    }

    /**
     * Retrieve the raw status string representation.
     * @return status representation
     */
    public String getValue() {
        return value;
    }
}
