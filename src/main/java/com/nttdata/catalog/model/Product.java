package com.nttdata.catalog.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** The twelve source fields, without currency conversion or text normalization. */
public record Product(
        String id,
        String name,
        String description,
        String format,
        String category,
        BigDecimal price,
        String priceUnit,
        BigDecimal originalPrice,
        String currency,
        String imageUrl,
        String productUrl,
        LocalDateTime extractedAt) {

    /** Derived independently so the original category path remains intact. */
    public String categoryGroup() {
        int separator = category.indexOf('>');
        return (separator < 0 ? category : category.substring(0, separator)).strip();
    }
}
