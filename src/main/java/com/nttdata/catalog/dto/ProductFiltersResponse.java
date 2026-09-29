package com.nttdata.catalog.dto;

import com.nttdata.catalog.service.ProductService.ProductFilters;

import java.util.List;

public record ProductFiltersResponse(List<String> categories, List<String> formats) {
    public ProductFiltersResponse {
        categories = List.copyOf(categories);
        formats = List.copyOf(formats);
    }

    public static ProductFiltersResponse from(ProductFilters filters) {
        return new ProductFiltersResponse(filters.categories(), filters.formats());
    }
}
