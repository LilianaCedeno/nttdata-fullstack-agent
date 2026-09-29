package com.nttdata.catalog.dto;

import com.nttdata.catalog.service.ProductService.ProductPage;

import java.util.List;

public record ProductPageResponse(List<ProductResponse> items, int page, int size,
                                  int totalElements, int totalPages,
                                  boolean hasPrevious, boolean hasNext) {
    public ProductPageResponse {
        items = List.copyOf(items);
    }

    public static ProductPageResponse from(ProductPage result) {
        return new ProductPageResponse(result.items().stream().map(ProductResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages(),
                result.hasPrevious(), result.hasNext());
    }
}
