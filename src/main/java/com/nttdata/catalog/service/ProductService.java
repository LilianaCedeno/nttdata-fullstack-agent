package com.nttdata.catalog.service;

import com.nttdata.catalog.model.Product;
import com.nttdata.catalog.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Queries the repository snapshot without accessing or changing the source catalog. */
@Service
public class ProductService {

    public static final int PAGE_SIZE = 8;

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    /**
     * Null or empty parameters omit a filter; nonempty values are not trimmed.
     * Nonnegative pages beyond the last return no items, preserving the requested
     * page and totals. hasPrevious indicates page > 0; hasNext is false there.
     */
    public ProductPage findProducts(String search, String category, String format, int page) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        String query = search == null ? "" : search.toLowerCase(Locale.ROOT);
        List<Product> matches = repository.findAll().stream()
                .filter(product -> product.name().toLowerCase(Locale.ROOT).contains(query))
                .filter(product -> category == null || category.isEmpty()
                        || product.categoryGroup().equals(category))
                .filter(product -> format == null || format.isEmpty() || product.format().equals(format))
                .toList();
        int totalElements = matches.size();
        int totalPages = (int) ((totalElements + (long) PAGE_SIZE - 1) / PAGE_SIZE);
        long offset = (long) page * PAGE_SIZE;
        int start = (int) Math.min(offset, totalElements);
        int end = (int) Math.min(offset + PAGE_SIZE, totalElements);
        return new ProductPage(matches.subList(start, end), page, PAGE_SIZE, totalElements,
                totalPages, page > 0, page < totalPages - 1);
    }

    /** Exact String lookup; absence is left explicit for the future API layer. */
    public Optional<Product> findById(String id) {
        return repository.findById(id);
    }

    /** Unique values in first-occurrence order, taken from the complete catalog. */
    public ProductFilters findFilters() {
        List<Product> products = repository.findAll();
        return new ProductFilters(
                products.stream().map(Product::categoryGroup).distinct().toList(),
                products.stream().map(Product::format).distinct().toList());
    }

    /** Service result, independent of the HTTP response DTOs introduced in stage 5. */
    public record ProductPage(List<Product> items, int page, int size, int totalElements,
                              int totalPages, boolean hasPrevious, boolean hasNext) {
        public ProductPage {
            items = List.copyOf(items);
        }
    }

    public record ProductFilters(List<String> categories, List<String> formats) {
        public ProductFilters {
            categories = List.copyOf(categories);
            formats = List.copyOf(formats);
        }
    }
}
