package com.nttdata.catalog.service;

import com.nttdata.catalog.config.CatalogProperties;
import com.nttdata.catalog.loader.CsvProductLoader;
import com.nttdata.catalog.model.Product;
import com.nttdata.catalog.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductServiceTests {

    private List<Product> source;
    private ProductService service;

    @BeforeEach
    void setUp() {
        // A small, unchanged slice of real products makes expected matches explicit.
        source = new CsvProductLoader().load(Path.of("data", "catalog.csv")).subList(0, 12);
        service = serviceFor(source);
    }

    @Test
    void searchesPartialNamesIgnoringCaseIncludingUnicode() {
        assertThat(service.findProducts("cHoCoLaTe", null, null, 0).items())
                .extracting(Product::id).containsExactly("10005", "10043", "10050", "10055", "10066");
        assertThat(service.findProducts("LÍQUIDO", null, null, 0).items())
                .extracting(Product::id).containsExactly("10005");
        assertThat(service.findProducts("10005", null, null, 0).items()).isEmpty();
    }

    @Test
    void searchDoesNotDependOnDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(service.findProducts("BATIDO", null, null, 0).items())
                    .extracting(Product::id).containsExactly("10043", "10050", "10051", "10054", "10055", "10066");
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void filtersByMainCategoryAndPreservesOriginalPath() {
        var page = service.findProducts(null, "Cacao, café e infusiones", null, 0);
        assertThat(page.items()).containsExactly(source.get(0));
        assertThat(page.items().get(0).category())
                .isEqualTo("Cacao, café e infusiones > Cacao soluble y chocolate a la taza");
        assertThat(service.findProducts(null, source.get(0).category(), null, 0).items()).isEmpty();
        assertThat(service.findProducts(null, "Cacao", null, 0).items()).isEmpty();
    }

    @Test
    void filtersFormatExactlyWithoutCaseOrWhitespaceNormalization() {
        assertThat(service.findProducts(null, null, "Brick", 0).items())
                .extracting(Product::id).containsExactly("10005", "10161");
        for (String format : List.of("brick", "Brick ", "Bric", "unknown")) {
            assertThat(service.findProducts(null, null, format, 0).items()).isEmpty();
        }
    }

    @Test
    void combinesAllFiltersWithAndBeforePagination() {
        var page = service.findProducts("CHOCOLATE", "Huevos, leche y mantequilla", "6 mini bricks x 200 ml", 0);
        assertThat(page.items()).extracting(Product::id).containsExactly("10043", "10050", "10055");
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.hasNext()).isFalse();
        assertThat(service.findProducts("chocolate", "Zumos", "6 mini bricks x 200 ml", 0).items()).isEmpty();
        assertThat(service.findProducts("chocolate", null, "Brick", 0).items()).containsExactly(source.get(0));
        assertThat(service.findProducts("chocolate", "Cacao, café e infusiones", null, 0).items())
                .containsExactly(source.get(0));
        assertThat(service.findProducts(null, "Huevos, leche y mantequilla", "Brick", 0).items())
                .containsExactly(source.get(11));
    }

    @Test
    void paginatesFromZeroWithFixedSizeAndStableOrder() {
        var first = service.findProducts(null, null, null, 0);
        assertThat(first.items()).containsExactlyElementsOf(source.subList(0, 8));
        assertThat(first.page()).isZero();
        assertThat(first.size()).isEqualTo(8);
        assertThat(first.totalElements()).isEqualTo(12);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.hasPrevious()).isFalse();
        assertThat(first.hasNext()).isTrue();
        var last = service.findProducts(null, null, null, 1);
        assertThat(last.items()).containsExactlyElementsOf(source.subList(8, 12));
        assertThat(last.page()).isEqualTo(1);
        assertThat(last.size()).isEqualTo(8);
        assertThat(last.totalElements()).isEqualTo(12);
        assertThat(last.totalPages()).isEqualTo(2);
        assertThat(last.hasPrevious()).isTrue();
        assertThat(last.hasNext()).isFalse();
        assertThat(service.findProducts("", "", "", 0)).isEqualTo(first);
    }

    @Test
    void paginatesFilteredMatchesInsteadOfFilteringAnAlreadySelectedPage() {
        var first = service.findProducts(null, "Huevos, leche y mantequilla", null, 0);
        var last = service.findProducts(null, "Huevos, leche y mantequilla", null, 1);
        assertThat(first.items()).extracting(Product::id)
                .containsExactly("10043", "10050", "10051", "10054", "10055", "10066", "10117", "10146");
        assertThat(first.totalElements()).isEqualTo(9);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.hasNext()).isTrue();
        assertThat(last.items()).extracting(Product::id).containsExactly("10161");
        assertThat(last.totalElements()).isEqualTo(9);
        assertThat(last.hasPrevious()).isTrue();
        assertThat(last.hasNext()).isFalse();
    }

    @Test
    void handlesZeroMatchesAndEmptyCatalog() {
        for (ProductService current : List.of(service, serviceFor(List.of()))) {
            var page = current.findProducts("no-such-product", null, null, 0);
            assertThat(page.items()).isEmpty();
            assertThat(page.totalElements()).isZero();
            assertThat(page.totalPages()).isZero();
            assertThat(page.hasPrevious()).isFalse();
            assertThat(page.hasNext()).isFalse();
        }
        assertThat(serviceFor(List.of()).findFilters().categories()).isEmpty();
        assertThat(serviceFor(List.of()).findFilters().formats()).isEmpty();
    }

    @Test
    void pagesBeyondLastPreserveRequestedPageAndPaginationMetadata() {
        for (int requestedPage : new int[]{2, 3, Integer.MAX_VALUE}) {
            var page = service.findProducts(null, null, null, requestedPage);
            assertThat(page.items()).isEmpty();
            assertThat(page.page()).isEqualTo(requestedPage);
            assertThat(page.size()).isEqualTo(8);
            assertThat(page.totalElements()).isEqualTo(12);
            assertThat(page.totalPages()).isEqualTo(2);
            assertThat(page.hasPrevious()).isTrue();
            assertThat(page.hasNext()).isFalse();
        }
    }

    @Test
    void pagesBeyondFilteredResultsKeepFilteredTotals() {
        var page = service.findProducts("CHOCOLATE", "Huevos, leche y mantequilla", "6 mini bricks x 200 ml", 1);
        assertThat(page.items()).isEmpty();
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.size()).isEqualTo(8);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.hasPrevious()).isTrue();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void positivePagesWithZeroMatchesOrEmptyCatalogPreserveMetadata() {
        for (ProductService current : List.of(service, serviceFor(List.of()))) {
            for (int requestedPage : new int[]{1, Integer.MAX_VALUE}) {
                var page = current.findProducts("no-such-product", null, null, requestedPage);
                assertThat(page.items()).isEmpty();
                assertThat(page.page()).isEqualTo(requestedPage);
                assertThat(page.size()).isEqualTo(8);
                assertThat(page.totalElements()).isZero();
                assertThat(page.totalPages()).isZero();
                assertThat(page.hasPrevious()).isTrue();
                assertThat(page.hasNext()).isFalse();
            }
        }
    }

    @Test
    void rejectsNegativePages() {
        for (int page : new int[]{-1, Integer.MIN_VALUE}) {
            assertThatThrownBy(() -> service.findProducts(null, null, null, page))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("page");
        }
    }

    @Test
    void getsDetailByExactStringIdAndMakesAbsenceExplicit() {
        assertThat(service.findById("10005")).containsSame(source.get(0));
        for (String id : List.of("missing", "010005", "10005 ", "", source.get(0).name())) {
            assertThat(service.findById(id)).isEmpty();
        }
    }

    @Test
    void derivesDistinctFilterOptionsFromWholeCatalogInSourceOrder() {
        service.findProducts("chocolate", null, "Brick", 0);
        var filters = service.findFilters();
        assertThat(filters.categories()).containsExactly("Cacao, café e infusiones", "Huevos, leche y mantequilla", "Zumos");
        assertThat(filters.formats()).containsExactly("Brick", "6 mini bricks x 200 ml", "4 mini botellas x 188 ml", "Tarrina", "6 bricks x 1 L");
    }

    @Test
    void returnedCollectionsCannotChangeServiceResults() {
        assertThatThrownBy(() -> service.findProducts(null, null, null, 0).items().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> service.findFilters().categories().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> service.findFilters().formats().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(service.findProducts(null, null, null, 0).totalElements()).isEqualTo(12);
    }

    private ProductService serviceFor(List<Product> products) {
        CsvProductLoader loader = new CsvProductLoader() {
            @Override
            public List<Product> load(Path path) {
                return products;
            }
        };
        return new ProductService(new ProductRepository(loader, new CatalogProperties()));
    }
}
