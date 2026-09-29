package com.nttdata.catalog.repository;

import com.nttdata.catalog.config.CatalogProperties;
import com.nttdata.catalog.loader.CsvProductLoader;
import com.nttdata.catalog.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductRepositoryTests {

    @TempDir
    Path directory;

    @Test
    void loadsOnceAndKeepsSnapshotAfterSourceIsDeleted() throws IOException {
        Path csv = Files.copy(Path.of("data", "catalog.csv"), directory.resolve("catalog.csv"));
        CatalogProperties properties = new CatalogProperties();
        properties.setPath(csv);
        CountingLoader loader = new CountingLoader();
        ProductRepository repository = new ProductRepository(loader, properties);
        Files.delete(csv);

        for (int read = 0; read < 3; read++) {
            assertThat(repository.findAll()).hasSize(4032);
            assertThat(repository.findById("12049.1")).isPresent();
            assertThat(repository.findById("missing")).isEmpty();
        }
        assertThat(loader.loads).isEqualTo(1);
    }

    @Test
    void preservesEntireSourceOrderAndIndexesEveryExactStringId() {
        List<Product> source = new CsvProductLoader().load(Path.of("data", "catalog.csv"));
        ProductRepository repository = new ProductRepository(new CsvProductLoader(), new CatalogProperties());

        assertThat(repository.findAll()).containsExactlyElementsOf(source).hasSize(4032);
        assertThat(repository.findAll().stream().map(Product::categoryGroup).distinct().count()).isEqualTo(26);
        assertThat(repository.findAll().stream().map(Product::format).distinct().count()).isEqualTo(252);
        for (Product product : repository.findAll()) {
            assertThat(repository.findById(product.id())).containsSame(product);
        }
        assertThat(repository.findById("12049.1")).get().extracting(Product::id).isEqualTo("12049.1");
        assertThat(repository.findById("12049.2")).get().extracting(Product::id).isEqualTo("12049.2");
        assertThat(repository.findById("012049.1")).isEmpty();
        assertThat(repository.findById("12049.1 ")).isEmpty();
        assertThat(repository.findById("")).isEmpty();
    }

    @Test
    void snapshotCannotBeModifiedThroughReturnedListOrLoaderList() {
        List<Product> source = new ArrayList<>(new CsvProductLoader().load(Path.of("data", "catalog.csv")));
        Product first = source.get(0);
        CsvProductLoader loader = new CsvProductLoader() {
            @Override
            public List<Product> load(Path path) {
                return source;
            }
        };
        ProductRepository repository = new ProductRepository(loader, new CatalogProperties());
        source.clear();

        assertThat(repository.findAll()).hasSize(4032);
        assertThat(repository.findById(first.id())).containsSame(first);
        assertThatThrownBy(() -> repository.findAll().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> repository.findAll().set(0, first)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> repository.findAll().add(first)).isInstanceOf(UnsupportedOperationException.class);
    }

    private static class CountingLoader extends CsvProductLoader {
        private int loads;

        @Override
        public List<Product> load(Path path) {
            loads++;
            return super.load(path);
        }
    }
}
