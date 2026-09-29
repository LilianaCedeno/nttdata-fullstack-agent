package com.nttdata.catalog.controller;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.nttdata.catalog.model.Product;
import com.nttdata.catalog.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductApiTests {
    @LocalServerPort
    private int port;
    @Autowired
    private ProductRepository repository;
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void listsRealCatalogWithStableOrderAndDefaultMetadata() throws Exception {
        var json = get("", 200);
        assertPage(json, 0, 4032, 504, false, true);
        assertThat(json.<List<String>>read("$.items[*].id")).containsExactlyElementsOf(
                repository.findAll().subList(0, 8).stream().map(Product::id).toList());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 503, 504, Integer.MAX_VALUE})
    void preservesBoundaryPageBehavior(int page) throws Exception {
        var json = get("?page=" + page, 200);
        assertPage(json, page, 4032, 504, true, page < 503);
        int start = (int) Math.min((long) page * 8, 4032);
        assertThat(json.<List<String>>read("$.items[*].id")).containsExactlyElementsOf(
                repository.findAll().subList(start, Math.min(start + 8, 4032))
                        .stream().map(Product::id).toList());
    }

    @Test
    void searchesPartialNamesIgnoringCaseAndSupportsUnicode() throws Exception {
        for (String search : List.of("cHoCoLaTe", "LÍQUIDO")) {
            var expected = repository.findAll().stream()
                    .filter(p -> p.name().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
                    .toList();
            var json = get("?search=" + encode(search), 200);
            assertThat(json.<Integer>read("$.totalElements")).isEqualTo(expected.size());
            assertThat(json.<List<String>>read("$.items[*].id"))
                    .containsExactlyElementsOf(expected.stream().limit(8).map(Product::id).toList());
        }
    }

    @Test
    void filtersByMainCategoryAndExactFormatAndCombinesWithAnd() throws Exception {
        String category = "Huevos, leche y mantequilla";
        String format = "6 mini bricks x 200 ml";
        for (String query : List.of("?category=" + encode(category), "?format=" + encode(format),
                "?search=CHOCOLATE&category=" + encode(category) + "&format=" + encode(format))) {
            var expected = repository.findAll().stream()
                    .filter(p -> !query.contains("category=") || p.categoryGroup().equals(category))
                    .filter(p -> !query.contains("format=") || p.format().equals(format))
                    .filter(p -> !query.contains("search=") || p.name().toLowerCase(Locale.ROOT).contains("chocolate"))
                    .toList();
            var json = get(query, 200);
            assertThat(json.<Integer>read("$.totalElements")).isEqualTo(expected.size());
            assertThat(json.<List<String>>read("$.items[*].id"))
                    .containsExactlyElementsOf(expected.stream().limit(8).map(Product::id).toList());
        }
        assertThat(get("?format=brick", 200).<List<?>>read("$.items")).isEmpty();
        assertThat(get("?format=" + encode("Brick "), 200).<List<?>>read("$.items")).isEmpty();
    }

    @Test
    void returnsEmptySuccessAndAcceptsEmptyOptionalFilters() throws Exception {
        var json = get("?search=no-such-product-xyz", 200);
        assertPage(json, 0, 0, 0, false, false);
        assertThat(json.<List<?>>read("$.items")).isEmpty();
        assertPage(get("?search=&category=&format=", 200), 0, 4032, 504, false, true);
    }

    @Test
    void filtersRouteReturnsRealDistinctOptionsInsteadOfIdLookup() throws Exception {
        var json = get("/filters", 200);
        assertThat(json.<List<String>>read("$.categories")).hasSize(26).containsExactlyElementsOf(
                repository.findAll().stream().map(Product::categoryGroup).distinct().toList());
        assertThat(json.<List<String>>read("$.formats")).hasSize(252).containsExactlyElementsOf(
                repository.findAll().stream().map(Product::format).distinct().toList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"10005", "12049.1", "12049.2"})
    void detailPreservesEverySourceFieldAndStringIds(String id) throws Exception {
        Product product = repository.findById(id).orElseThrow();
        var json = get("/" + id, 200);
        assertThat(json.<String>read("$.id")).isEqualTo(id);
        assertThat(json.<String>read("$.name")).isEqualTo(product.name());
        assertThat(json.<String>read("$.description")).isEqualTo(product.description());
        assertThat(json.<String>read("$.format")).isEqualTo(product.format());
        assertThat(json.<String>read("$.category")).isEqualTo(product.category());
        assertThat(new java.math.BigDecimal(json.read("$.price").toString())).isEqualByComparingTo(product.price());
        assertThat(new java.math.BigDecimal(json.read("$.originalPrice").toString())).isEqualByComparingTo(product.originalPrice());
        assertThat(json.<String>read("$.priceUnit")).isEqualTo(product.priceUnit());
        assertThat(json.<String>read("$.currency")).isEqualTo("CLP");
        assertThat(json.<String>read("$.imageUrl")).isEqualTo(product.imageUrl());
        assertThat(json.<String>read("$.productUrl")).isEqualTo(product.productUrl());
        assertThat(java.time.LocalDateTime.parse(json.read("$.extractedAt"))).isEqualTo(product.extractedAt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "012049.1", "12049.10"})
    void missingExactIdReturns404(String id) throws Exception {
        assertThat(get("/" + id, 404).<Integer>read("$.status")).isEqualTo(404);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-2147483648", "abc", "1.5", "2147483648"})
    void invalidPageReturns400(String page) throws Exception {
        assertThat(get("?page=" + page, 400).<Integer>read("$.status")).isEqualTo(400);
    }

    private DocumentContext get(String suffix, int expectedStatus) throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create(
                "http://localhost:" + port + "/api/products" + suffix)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertThat(response.statusCode()).as(response.body()).isEqualTo(expectedStatus);
        assertThat(response.headers().firstValue("content-type").orElseThrow()).contains("json");
        return JsonPath.parse(response.body());
    }

    private void assertPage(DocumentContext json, int page, int total, int pages, boolean previous, boolean next) {
        assertThat(json.<Integer>read("$.page")).isEqualTo(page);
        assertThat(json.<Integer>read("$.size")).isEqualTo(8);
        assertThat(json.<Integer>read("$.totalElements")).isEqualTo(total);
        assertThat(json.<Integer>read("$.totalPages")).isEqualTo(pages);
        assertThat(json.<Boolean>read("$.hasPrevious")).isEqualTo(previous);
        assertThat(json.<Boolean>read("$.hasNext")).isEqualTo(next);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
