package com.nttdata.catalog.dto;

import com.nttdata.catalog.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(String id, String name, String description, String format,
                              String category, BigDecimal price, String priceUnit,
                              BigDecimal originalPrice, String currency, String imageUrl,
                              String productUrl, LocalDateTime extractedAt) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.id(), product.name(), product.description(),
                product.format(), product.category(), product.price(), product.priceUnit(),
                product.originalPrice(), product.currency(), product.imageUrl(),
                product.productUrl(), product.extractedAt());
    }
}
