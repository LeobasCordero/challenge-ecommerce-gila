package com.gila.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gila.ecommerce.kafka.ProductRowSanitizer;
import com.gila.ecommerce.kafka.SanitizedRow;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.util.HtmlUtils;

/**
 * JUnit 5 Parameterized test verifying XSS sanitization, SQL Injection safety, and stock clamping logic.
 */
public class SecurityParameterizedTest {

    private final ProductRowSanitizer rowSanitizer = new ProductRowSanitizer();

    /**
     * Verify that XSS scripts and tags are escaped to HTML entities during sanitization.
     * @param input raw XSS payload string
     * @param expected expected HTML-escaped string
     */
    @ParameterizedTest
    @CsvSource({
        "'<script>alert(1)</script>', '&lt;script&gt;alert(1)&lt;/script&gt;'",
        "'<img src=x onerror=alert(1)>', '&lt;img src=x onerror=alert(1)&gt;'",
        "'<div>Test</div>', '&lt;div&gt;Test&lt;/div&gt;'"
    })
    public void testXssSanitization(String input, String expected) {
        String[] row = {input, "Desc", "10.0", "5", "Books"};
        SanitizedRow sanitized = rowSanitizer.sanitize(row, 1);
        assertTrue(sanitized.isValid());
        assertEquals(expected, sanitized.getProductDto().getName());
    }

    /**
     * Verify that SQL injection syntax vectors are safely mapped as plain literal text.
     * @param sqlPayload raw SQL injection payload string
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "admin' --",
        "' OR '1'='1",
        "'; DROP TABLE products; --",
        "UNION SELECT null, null, null"
    })
    public void testSqlInjectionStringsAreSafe(String sqlPayload) {
        String[] row = {sqlPayload, "Sql Desc", "15.99", "10", "Electronics"};
        SanitizedRow sanitized = rowSanitizer.sanitize(row, 1);
        assertTrue(sanitized.isValid());
        assertEquals(HtmlUtils.htmlEscape(sqlPayload), sanitized.getProductDto().getName());
    }

    /**
     * Verify prices formats ($ removal, free mapped to 0) are parsed correctly.
     * @param priceInput raw price string
     * @param expectedPrice expected double price value
     */
    @ParameterizedTest
    @CsvSource({
        "'$19.99', 19.99",
        "'19.99', 19.99",
        "'free', 0.0",
        "'FREE ', 0.0"
    })
    public void testPriceSanitization(String priceInput, double expectedPrice) {
        String[] row = {"Product", "Desc", priceInput, "5", "Books"};
        SanitizedRow sanitized = rowSanitizer.sanitize(row, 1);
        assertTrue(sanitized.isValid());
        assertEquals(expectedPrice, sanitized.getProductDto().getPrice());
    }

    /**
     * Verify negative stock counts are clamped to 0.
     * @param stockInput raw stock count string
     * @param expectedStock expected stock integer value
     */
    @ParameterizedTest
    @CsvSource({
        "'-5', 0",
        "'10', 10",
        "'-999', 0"
    })
    public void testStockClamping(String stockInput, int expectedStock) {
        String[] row = {"Product", "Desc", "10.00", stockInput, "Books"};
        SanitizedRow sanitized = rowSanitizer.sanitize(row, 1);
        assertTrue(sanitized.isValid());
        assertEquals(expectedStock, sanitized.getProductDto().getStock());
    }
}
