package com.gila.ecommerce.kafka;

import com.gila.ecommerce.dto.ProductDto;
import java.util.ArrayList;
import java.util.List;
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
        List<String> warnings = new ArrayList<>();
        if (row == null || row.length < 5) {
            warnings.add("Row " + rowIndex + " contains insufficient columns. Expected 5.");
            return new SanitizedRow(null, warnings, false);
        }

        String name = row[0];
        String description = row[1];
        String priceStr = row[2];
        String stockStr = row[3];
        String category = row[4];

        if (!StringUtils.hasText(name)
                || !StringUtils.hasText(priceStr)
                || !StringUtils.hasText(stockStr)
                || !StringUtils.hasText(category)) {
            warnings.add("Row " + rowIndex + " contains empty required fields.");
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
                warnings.add("Row " + rowIndex + " has invalid price format: '" + priceStr + "'.");
                return new SanitizedRow(null, warnings, false);
            }
        }

        Integer stock;
        try {
            stock = Integer.parseInt(stockStr.trim());
        } catch (NumberFormatException e) {
            warnings.add("Row " + rowIndex + " has invalid stock format: '" + stockStr + "'.");
            return new SanitizedRow(null, warnings, false);
        }

        if (stock < 0) {
            warnings.add("Row " + rowIndex + ": Stock for '" + sanitizedName
                    + "' was negative (" + stock + "). Clamped to 0.");
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
