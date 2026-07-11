package com.gila.ecommerce.kafka;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ProductCsvParserTest {

    private final ProductCsvParser csvParser = new ProductCsvParser();

    @Test
    public void testParseCsvFile(@TempDir Path tempDir) throws Exception {
        Path tempFile = tempDir.resolve("test-products.csv");
        String csvContent = "name,description,price,stock,category\n" +
                "Item1,Desc1,10.0,5,Books\n" +
                "Item2,Desc2,20.0,10,Clothing\n";

        Files.writeString(tempFile, csvContent);

        List<String[]> parsedRows = csvParser.parse(tempFile.toAbsolutePath().toString());

        assertNotNull(parsedRows);
        assertEquals(2, parsedRows.size());
        assertEquals("Item1", parsedRows.get(0)[0]);
        assertEquals("Item2", parsedRows.get(1)[0]);
    }
}
