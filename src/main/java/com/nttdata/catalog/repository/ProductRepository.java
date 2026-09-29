package com.nttdata.catalog.repository;

import com.nttdata.catalog.config.CatalogProperties;
import com.nttdata.catalog.loader.CsvProductLoader;
import com.nttdata.catalog.model.Product;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Loads one validated snapshot at startup; reads never access the CSV again. */
@Repository
public class ProductRepository {

    private final List<Product> products;
    private final Map<String, Product> productsById;

    public ProductRepository(CsvProductLoader loader, CatalogProperties properties) {
        products = List.copyOf(loader.load(properties.getPath()));
        productsById = products.stream().collect(
                Collectors.toUnmodifiableMap(Product::id, Function.identity()));
    }

    /** Preserves source order independently of the index's iteration order. */
    public List<Product> findAll() {
        return products;
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(productsById.get(id));
    }
}
