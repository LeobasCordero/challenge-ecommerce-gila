package com.gila.ecommerce.kafka;

import com.gila.ecommerce.dto.ProductDto;
import com.gila.ecommerce.exception.ErrorMessages;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * Component sanitizing, escaping, and formatting raw CSV row fields.
 */
@Component
public class ProductRowSanitizer {

    /**
     * Parse and sanitize raw CSV string array fields into a structured SanitizedRow.
     * @param row raw CSV fields array
     * @param rowIndex index of the current row (for log reporting)
     * @return SanitizedRow containing product DTO and warnings
     */
    public SanitizedRow sanitize(String[] row, int rowIndex) {
        return sanitize(row, rowIndex, Map.of());
    }

    /**
     * Parse and sanitize raw CSV string array fields into a structured SanitizedRow using dynamic headers.
     * @param row raw CSV fields array
     * @param rowIndex index of the current row (for log reporting)
     * @param headerMap map of header names to CSV column indexes
     * @return SanitizedRow containing product DTO and warnings
     */
    public SanitizedRow sanitize(String[] row, int rowIndex, Map<String, Integer> headerMap) {
        List<String> warnings = new ArrayList<>();
        
        int nameIdx = headerMap.getOrDefault("name", 0);
        int descIdx = headerMap.getOrDefault("description", 1);
        int priceIdx = headerMap.getOrDefault("price", 2);
        int stockIdx = headerMap.getOrDefault("stock", 3);
        int catIdx = headerMap.getOrDefault("category", 4);

        int maxIdx = Math.max(Math.max(nameIdx, descIdx), Math.max(priceIdx, Math.max(stockIdx, catIdx)));
        if (row == null || row.length <= maxIdx) {
            warnings.add(ErrorMessages.ROW_PREFIX + rowIndex + ErrorMessages.INSUFFICIENT_COLUMNS);
            return new SanitizedRow(null, warnings, false);
        }

        String name = row[nameIdx];
        String description = row[descIdx];
        String priceStr = row[priceIdx];
        String stockStr = row[stockIdx];
        String category = row[catIdx];

        if (!StringUtils.hasText(name)
                || !StringUtils.hasText(priceStr)
                || !StringUtils.hasText(stockStr)
                || !StringUtils.hasText(category)) {
            warnings.add(ErrorMessages.ROW_PREFIX + rowIndex + ErrorMessages.EMPTY_REQUIRED_FIELDS);
            return new SanitizedRow(null, warnings, false);
        }

        String sanitizedName = HtmlUtils.htmlEscape(name.trim());
        String sanitizedDescription = StringUtils.hasText(description)
                ? HtmlUtils.htmlEscape(description.trim()) : "";
        String sanitizedCategory = HtmlUtils.htmlEscape(category.trim());

        Double price;
        String cleanPrice = priceStr.trim().replace("$", "").trim();
        if ("free".equalsIgnoreCase(cleanPrice)) {
            price = 0.0;
        } else {
            try {
                price = Double.parseDouble(cleanPrice);
            } catch (NumberFormatException e) {
                warnings.add(ErrorMessages.ROW_PREFIX + rowIndex + ErrorMessages.INVALID_PRICE_FORMAT + priceStr + "'.");
                return new SanitizedRow(null, warnings, false);
            }
        }

        Integer stock;
        try {
            stock = Integer.parseInt(stockStr.trim());
        } catch (NumberFormatException e) {
            warnings.add(ErrorMessages.ROW_PREFIX + rowIndex + ErrorMessages.INVALID_STOCK_FORMAT + stockStr + "'.");
            return new SanitizedRow(null, warnings, false);
        }

        if (stock < 0) {
            warnings.add(ErrorMessages.ROW_PREFIX + rowIndex + ErrorMessages.NEGATIVE_STOCK_PREFIX + sanitizedName
                    + ErrorMessages.NEGATIVE_STOCK_MIDDLE + stock + ErrorMessages.NEGATIVE_STOCK_SUFFIX);
            stock = 0;
        }

        ProductDto dto = new ProductDto();
        dto.setName(sanitizedName);
        dto.setDescription(sanitizedDescription);
        dto.setPrice(price);
        dto.setStock(stock);
        dto.setCategory(sanitizedCategory);

        return new SanitizedRow(dto, warnings, true);
    }
}
