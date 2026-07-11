package com.gila.ecommerce.kafka;

import com.gila.ecommerce.dto.ProductDto;
import java.util.List;

/**
 * Data container representing the result of sanitizing a CSV row.
 */
public class SanitizedRow {

    private final ProductDto productDto;
    private final List<String> warnings;
    private final boolean valid;

    /**
     * Constructor initializing sanitization results.
     * @param productDto mapped product details if valid, null otherwise
     * @param warnings list of warning logs generated during sanitization
     * @param valid true if row is valid, false otherwise
     */
    public SanitizedRow(ProductDto productDto, List<String> warnings, boolean valid) {
        this.productDto = productDto;
        this.warnings = warnings;
        this.valid = valid;
    }

    /**
     * Retrieve the mapped product DTO.
     * @return product DTO model
     */
    public ProductDto getProductDto() {
        return productDto;
    }

    /**
     * Retrieve list of warning logs.
     * @return list of warnings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Check if row is valid.
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }
}
