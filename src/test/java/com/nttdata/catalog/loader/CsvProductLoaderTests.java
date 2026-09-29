package com.nttdata.catalog.loader;

import com.nttdata.catalog.exception.CatalogLoadException;
import com.nttdata.catalog.model.Product;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvProductLoaderTests {

    private static final String HEADER = "id,name,description,format,category,price,priceUnit,"
            + "originalPrice,currency,imageUrl,productUrl,extractedAt";
    private final CsvProductLoader loader = new CsvProductLoader();

    @TempDir
    Path directory;

    @Test
    void preservesAllFieldsQuotedCommasEscapedQuotesUnicodeAndVariantIds() throws IOException {
        // Handwritten CSV independently exercises the parsing of quotes and embedded newlines.
        Path csv = write(HEADER + "\r\n"
                + "12049.1,\"Leche, café ñ\",\"Dice \"\"sí\"\", frío\nsegunda línea\","
                + "  Pack 2 x 1 L  ,\"Huevos, leche y mantequilla > Leche > Entera\","
                + "1234.50,/pack,1500.00,CLP,https://example.test/ñ.jpg,"
                + "https://example.test/12049.1,2025-11-06 20:29:53\r\n"
                + row(validRow("12049.2")));

        List<Product> products = loader.load(csv);
        assertThat(products).extracting(Product::id).containsExactly("12049.1", "12049.2");
        Product product = products.get(0);
        assertThat(product.name()).isEqualTo("Leche, café ñ");
        assertThat(product.description()).isEqualTo("Dice \"sí\", frío\nsegunda línea");
        assertThat(product.format()).isEqualTo("  Pack 2 x 1 L  ");
        assertThat(product.category()).isEqualTo("Huevos, leche y mantequilla > Leche > Entera");
        assertThat(product.categoryGroup()).isEqualTo("Huevos, leche y mantequilla");
        assertThat(product.price()).isEqualTo(new BigDecimal("1234.50"));
        assertThat(product.originalPrice()).isEqualTo(new BigDecimal("1500.00"));
        assertThat(product.priceUnit()).isEqualTo("/pack");
        assertThat(product.currency()).isEqualTo("CLP");
        assertThat(product.imageUrl()).isEqualTo("https://example.test/ñ.jpg");
        assertThat(product.productUrl()).isEqualTo("https://example.test/12049.1");
        assertThat(product.extractedAt()).isEqualTo(LocalDateTime.of(2025, 11, 6, 20, 29, 53));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11})
    void rejectsEveryMissingRequiredField(int index) throws IOException {
        String[] fields = validRow("12049.1");
        fields[index] = "";
        assertInvalid(fields, "Required field is blank");
        fields[index] = " \t ";
        assertInvalid(fields, "Required field is blank");
    }

    @ParameterizedTest
    @CsvSource({"5,abc,price", "5,-1,price", "5,NaN,price", "5,Infinity,price",
            "7,abc,originalPrice", "7,-1,originalPrice", "7,NaN,originalPrice",
            "8,EUR,currency", "11,2025-02-30 12:00:00,extractedAt",
            "11,2025-11-06 25:00:00,extractedAt", "11,not-a-date,extractedAt",
            "4,> Leche,category"})
    void rejectsInvalidTypedValues(int index, String value, String field) throws IOException {
        String[] fields = validRow("12049.1");
        fields[index] = value;
        assertInvalid(fields, field);
    }

    @Test
    void acceptsZeroPricesAndCategoryWithoutSubcategory() throws IOException {
        String[] fields = validRow("0012049.1");
        fields[4] = "  Panadería  ";
        fields[5] = "0";
        fields[7] = "0";
        Product product = loader.load(write(HEADER + "\n" + row(fields))).get(0);
        assertThat(product.id()).isEqualTo("0012049.1");
        assertThat(product.category()).isEqualTo("  Panadería  ");
        assertThat(product.categoryGroup()).isEqualTo("Panadería");
        assertThat(product.price()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(product.originalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void rejectsDuplicateIdsWithoutRejectingDuplicateNames() throws IOException {
        String first = row(validRow("12049.1"));
        assertThat(loader.load(write(HEADER + "\n" + first + row(validRow("12049.2")))))
                .hasSize(2);
        Path csv = write(HEADER + "\n" + first + first);
        assertThatThrownBy(() -> loader.load(csv)).isInstanceOf(CatalogLoadException.class)
                .hasMessageContaining("Duplicate id: 12049.1").hasMessageContaining("record: 2");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "id,name", "id,name,description,format,category,price,priceUnit,"
            + "originalPrice,currency,imageUrl,productUrl,unknown",
            "id,name,description,format,category,price,priceUnit,originalPrice,currency,imageUrl,productUrl,id"})
    void rejectsMissingUnknownOrDuplicateHeaders(String header) throws IOException {
        Path csv = write(header + "\n" + row(validRow("1")));
        assertThatThrownBy(() -> loader.load(csv)).isInstanceOf(CatalogLoadException.class);
    }

    @Test
    void rejectsExtraHeader() throws IOException {
        Path csv = write(HEADER + ",extra\n" + row(validRow("1")));
        assertThatThrownBy(() -> loader.load(csv)).isInstanceOf(CatalogLoadException.class)
                .hasMessageContaining("12 unique columns");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1,two\n", "1,2,3,4,5,6,7,8,9,10,11,12,13\n", "\"unclosed", "\r\n"})
    void rejectsMalformedRowsInsteadOfSkippingThem(String record) throws IOException {
        Path csv = write(HEADER + "\n" + row(validRow("1")) + record);
        assertThatThrownBy(() -> loader.load(csv)).isInstanceOf(CatalogLoadException.class)
                .hasMessageContaining(csv.toString()).hasCauseInstanceOf(Exception.class);
    }

    @Test
    void rejectsEmptyCatalog() throws IOException {
        Path csv = write(HEADER + "\n");
        assertThatThrownBy(() -> loader.load(csv)).isInstanceOf(CatalogLoadException.class)
                .hasMessageContaining("no products");
    }

    @Test
    void reportsAbsentOrUnreadableSource() {
        for (Path path : List.of(directory.resolve("missing.csv"), directory)) {
            assertThatThrownBy(() -> loader.load(path)).isInstanceOf(CatalogLoadException.class)
                    .hasMessageContaining(path.toString()).hasCauseInstanceOf(IOException.class);
        }
    }

    @Test
    void rejectsInvalidUtf8() throws IOException {
        Path csv = directory.resolve("invalid.csv");
        Files.write(csv, new byte[]{(byte) 0xc3, (byte) 0x28});
        assertThatThrownBy(() -> loader.load(csv)).isInstanceOf(CatalogLoadException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void loadsRealCatalogWithExpectedCountsAndOriginalValues() {
        List<Product> products = loader.load(Path.of("data", "catalog.csv"));
        assertThat(products).hasSize(4032);
        assertThat(products).extracting(Product::id).doesNotHaveDuplicates()
                .contains("12049.1", "12049.2");
        assertThat(products.stream().map(Product::categoryGroup).distinct().count()).isEqualTo(26);
        assertThat(products.stream().map(Product::format).distinct().count()).isEqualTo(252);
        assertThat(products).allSatisfy(product -> {
            assertThat(product.currency()).isEqualTo("CLP");
            assertThat(product.price()).isNotNegative();
            assertThat(product.originalPrice()).isNotNegative();
            assertThat(product.extractedAt()).isNotNull();
        });
        Product first = products.get(0);
        assertThat(first.id()).isEqualTo("10005");
        assertThat(first.name()).isEqualTo("Chocolate líquido a la taza Hacendado");
        assertThat(first.price()).isEqualTo(new BigDecimal("2551"));
        assertThat(first.category()).isEqualTo("Cacao, café e infusiones > Cacao soluble y chocolate a la taza");
        assertThat(first.categoryGroup()).isEqualTo("Cacao, café e infusiones");
        assertThat(products.get(1).id()).isEqualTo("10043");
        assertThat(products.get(1).originalPrice()).isEqualTo(new BigDecimal("3040"));
    }

    private void assertInvalid(String[] fields, String diagnostic) throws IOException {
        Path csv = write(HEADER + "\n" + row(fields));
        assertThatThrownBy(() -> loader.load(csv)).isInstanceOf(CatalogLoadException.class)
                .hasMessageContaining(diagnostic).hasMessageContaining("record: 1")
                .hasMessageContaining(csv.toString());
    }

    private Path write(String contents) throws IOException {
        return Files.writeString(directory.resolve("catalog.csv"), contents, StandardCharsets.UTF_8);
    }

    private String row(String[] fields) throws IOException {
        StringWriter output = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(output, CSVFormat.RFC4180)) {
            printer.printRecord((Object[]) fields);
        }
        return output.toString();
    }

    private String[] validRow(String id) {
        return new String[]{id, "Leche entera", "Descripción con ñ", "Brick", "Lácteos > Leche",
                "1000", "/ud.", "1200", "CLP", "https://example.test/image.jpg",
                "https://example.test/product/" + id, "2025-11-06 20:29:53"};
    }
}
