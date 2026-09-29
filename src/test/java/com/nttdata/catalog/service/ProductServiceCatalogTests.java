package com.nttdata.catalog.service;

import com.nttdata.catalog.model.Product;
import com.nttdata.catalog.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductServiceCatalogTests {

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repository;

    @Test
    void realCatalogHas4032ProductsAcross504StablePages() {
        List<Product> collected = new ArrayList<>();
        for (int page = 0; page < 504; page++) {
            var result = service.findProducts(null, null, null, page);
            assertThat(result.items()).hasSize(8);
            assertThat(result.page()).isEqualTo(page);
            assertThat(result.size()).isEqualTo(8);
            assertThat(result.totalElements()).isEqualTo(4032);
            assertThat(result.totalPages()).isEqualTo(504);
            assertThat(result.hasPrevious()).isEqualTo(page > 0);
            assertThat(result.hasNext()).isEqualTo(page < 503);
            collected.addAll(result.items());
        }
        assertThat(collected).containsExactlyElementsOf(repository.findAll());
        assertThat(collected).extracting(Product::id).doesNotHaveDuplicates();
    }

    @Test
    void realCatalogPagesBeyond503AreEmptyAndKeepCatalogTotals() {
        for (int requestedPage : new int[]{504, 505, Integer.MAX_VALUE}) {
            var page = service.findProducts(null, null, null, requestedPage);
            assertThat(page.items()).isEmpty();
            assertThat(page.page()).isEqualTo(requestedPage);
            assertThat(page.size()).isEqualTo(8);
            assertThat(page.totalElements()).isEqualTo(4032);
            assertThat(page.totalPages()).isEqualTo(504);
            assertThat(page.hasPrevious()).isTrue();
            assertThat(page.hasNext()).isFalse();
        }
    }

    @Test
    void realCatalogProvides26CategoriesAnd252ExactFormats() {
        var filters = service.findFilters();
        assertThat(filters.categories()).hasSize(26).doesNotHaveDuplicates()
                .containsExactlyElementsOf(repository.findAll().stream().map(Product::categoryGroup).distinct().toList());
        assertThat(filters.formats()).hasSize(252).doesNotHaveDuplicates()
                .containsExactlyElementsOf(repository.findAll().stream().map(Product::format).distinct().toList());
    }

    @Test
    void variantIdsRemainDistinctAndKeepAllSourceFields() {
        assertThat(service.findById("12049.1")).isEqualTo(repository.findById("12049.1")).isPresent();
        assertThat(service.findById("12049.2")).isEqualTo(repository.findById("12049.2")).isPresent();
        assertThat(service.findById("12049.1")).isNotEqualTo(service.findById("12049.2"));
        assertThat(service.findById("012049.1")).isEmpty();
        assertThat(service.findById("12049.1 ")).isEmpty();
    }
}
