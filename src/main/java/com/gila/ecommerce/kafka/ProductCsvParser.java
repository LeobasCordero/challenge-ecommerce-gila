package com.gila.ecommerce.kafka;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Component parsing CSV data records using OpenCSV tools.
 */
@Component
public class ProductCsvParser {

    /**
     * Read and parse lines from a CSV file, skipping the header line.
     * @param filePath absolute path to the CSV file
     * @return list of parsed string array rows
     * @throws IOException on file reading errors
     * @throws CsvValidationException on invalid CSV formats
     */
    public List<String[]> parse(String filePath) throws IOException, CsvValidationException {
        List<String[]> rows = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            boolean isHeader = true;
            while ((line = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                rows.add(line);
            }
        }
        return rows;
    }
}
