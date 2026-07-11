package com.gila.ecommerce.util;

/**
 * Enum defining typesafe actions recorded in system audit logs.
 */
public enum AuditAction {
    LOGIN("LOGIN"),
    CHECKOUT("CHECKOUT"),
    PRODUCT_CREATE("PRODUCT_CREATE"),
    PRODUCT_UPDATE("PRODUCT_UPDATE"),
    PRODUCT_DELETE("PRODUCT_DELETE"),
    CSV_IMPORT_START("CSV_IMPORT_START"),
    CSV_IMPORT_COMPLETE("CSV_IMPORT_COMPLETE"),
    CSV_IMPORT_FAILED("CSV_IMPORT_FAILED");

    private final String value;

    /**
     * Constructor setting string value.
     * @param value raw action string representation
     */
    AuditAction(String value) {
        this.value = value;
    }

    /**
     * Retrieve the string value of the action.
     * @return raw action value
     */
    public String getValue() {
        return value;
    }
}
