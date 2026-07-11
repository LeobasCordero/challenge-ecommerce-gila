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

    /**
     * Private constructor to prevent instantiation.
     */
    private ErrorMessages() {
    }
}
