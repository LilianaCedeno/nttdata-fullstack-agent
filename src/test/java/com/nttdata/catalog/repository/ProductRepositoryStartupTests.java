package com.nttdata.catalog.repository;

import com.nttdata.catalog.config.CatalogProperties;
import com.nttdata.catalog.exception.CatalogLoadException;
import com.nttdata.catalog.loader.CsvProductLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryStartupTests {

    @TempDir
    Path directory;

    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(CatalogProperties.class, CsvProductLoader.class, ProductRepository.class);

    @Test
    void loadsConfiguredCatalogDuringStartup() throws IOException {
        Path csv = Files.copy(Path.of("data", "catalog.csv"), directory.resolve("configured.csv"));
        context.withPropertyValues("catalog.path=" + csv).run(application -> {
            assertThat(application).hasNotFailed().hasSingleBean(ProductRepository.class);
            // Removing the source before the first bean lookup proves eager startup loading.
            Files.delete(csv);
            ProductRepository repository = application.getBean(ProductRepository.class);
            assertThat(repository.findAll()).hasSize(4032);
            assertThat(application.getBean(ProductRepository.class)).isSameAs(repository);
        });
    }

    @Test
    void failsStartupWithSourceDiagnosticForMissingUnreadableAndInvalidCsv() throws IOException {
        Path malformed = Files.writeString(directory.resolve("invalid.csv"), "id,name\n1,invalid\n");
        for (Path source : new Path[]{directory.resolve("missing.csv"), directory, malformed}) {
            context.withPropertyValues("catalog.path=" + source).run(application -> {
                assertThat(application).hasFailed();
                assertThat(application.getStartupFailure())
                        .hasRootCauseInstanceOf(Exception.class)
                        .hasStackTraceContaining(CatalogLoadException.class.getName())
                        .hasStackTraceContaining("Cannot load catalog '" + source + "'");
            });
        }
    }
}
