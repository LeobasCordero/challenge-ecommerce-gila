package com.gila.ecommerce.exception;

/**
 * Centrally defined static error message constants used throughout service layers.
 */
public final class ErrorMessages {

    public static final String PRODUCT_NAME_EXISTS = "Product name already exists";
    public static final String PRODUCT_NOT_FOUND = "Product not found";
    public static final String FILE_EMPTY = "File is empty";
    public static final String FILE_SAVE_FAILED = "Failed to save file: ";
    public static final String TASK_NOT_FOUND = "Task not found";
    public static final String INSUFFICIENT_STOCK = "Insufficient stock";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String CART_EMPTY = "Cart is empty";
    public static final String PRODUCT_NOT_FOUND_PREFIX = "Product not found: ";
    public static final String INSUFFICIENT_STOCK_PREFIX = "Insufficient stock for product: ";
    public static final String ROW_PREFIX = "Row ";
    public static final String INSUFFICIENT_COLUMNS = " contains insufficient columns. Expected 5.";
    public static final String EMPTY_REQUIRED_FIELDS = " contains empty required fields.";
    public static final String INVALID_PRICE_FORMAT = " has invalid price format: '";
    public static final String INVALID_STOCK_FORMAT = " has invalid stock format: '";
    public static final String NEGATIVE_STOCK_PREFIX = ": Stock for '";
    public static final String NEGATIVE_STOCK_MIDDLE = "' was negative (";
    public static final String NEGATIVE_STOCK_SUFFIX = "). Clamped to 0.";
    public static final String DUPLICATE_PRODUCT_PREFIX = "Product with name '";
    public static final String DUPLICATE_PRODUCT_SUFFIX = "' already exists in database. Skipped.";
    public static final String CRITICAL_SYSTEM_ERROR_PREFIX = "Critical system error: ";

    /**
     * Private constructor to prevent instantiation.
     */
    private ErrorMessages() {
    }
}
