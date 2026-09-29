package com.nttdata.catalog.loader;

import com.nttdata.catalog.exception.CatalogLoadException;
import com.nttdata.catalog.model.Product;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Parses and validates a CSV on demand. Startup storage belongs to the repository stage. */
@Component
public class CsvProductLoader {

    private static final List<String> HEADERS = List.of(
            "id", "name", "description", "format", "category", "price", "priceUnit",
            "originalPrice", "currency", "imageUrl", "productUrl", "extractedAt");
    private static final CSVFormat FORMAT = CSVFormat.RFC4180.builder()
            .setHeader().setSkipHeaderRecord(true).get();
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.STRICT);

    public List<Product> load(Path path) {
        long recordNumber = 0;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = FORMAT.parse(reader)) {
            List<String> headers = parser.getHeaderNames();
            if (headers.size() != HEADERS.size()
                    || !new HashSet<>(headers).equals(new HashSet<>(HEADERS))) {
                throw new IllegalArgumentException("Expected exactly these 12 unique columns: " + HEADERS);
            }
            List<Product> products = new ArrayList<>();
            Set<String> ids = new HashSet<>();
            for (CSVRecord record : parser) {
                recordNumber = record.getRecordNumber();
                if (!record.isConsistent()) {
                    throw new IllegalArgumentException("Expected 12 fields, found " + record.size());
                }
                for (String field : HEADERS) {
                    if (record.get(field).isBlank()) {
                        throw new IllegalArgumentException("Required field is blank: " + field);
                    }
                }
                if (!ids.add(record.get("id"))) {
                    throw new IllegalArgumentException("Duplicate id: " + record.get("id"));
                }
                if (!"CLP".equals(record.get("currency"))) {
                    throw new IllegalArgumentException("currency must be CLP");
                }
                Product product = new Product(record.get("id"), record.get("name"),
                        record.get("description"), record.get("format"), record.get("category"),
                        price(record, "price"), record.get("priceUnit"), price(record, "originalPrice"),
                        record.get("currency"), record.get("imageUrl"), record.get("productUrl"),
                        timestamp(record.get("extractedAt")));
                if (product.categoryGroup().isBlank()) {
                    throw new IllegalArgumentException("category must contain a main category before '>'");
                }
                products.add(product);
            }
            if (products.isEmpty()) {
                throw new IllegalArgumentException("Catalog contains no products");
            }
            return List.copyOf(products);
        } catch (IOException | UncheckedIOException | IllegalArgumentException ex) {
            throw new CatalogLoadException("Cannot load catalog '" + path + "' (last CSV record: "
                    + recordNumber + "): " + ex.getMessage(), ex);
        }
    }

    private BigDecimal price(CSVRecord record, String field) {
        try {
            BigDecimal value = new BigDecimal(record.get(field));
            if (value.signum() < 0) {
                throw new IllegalArgumentException(field + " must be non-negative");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " must be a valid decimal number", ex);
        }
    }

    private LocalDateTime timestamp(String value) {
        try {
            return LocalDateTime.parse(value, TIMESTAMP);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("extractedAt must be a valid date: uuuu-MM-dd HH:mm:ss", ex);
        }
    }
}
