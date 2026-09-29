package com.nttdata.catalog.controller;

import com.nttdata.catalog.dto.ProductFiltersResponse;
import com.nttdata.catalog.dto.ProductPageResponse;
import com.nttdata.catalog.dto.ProductResponse;
import com.nttdata.catalog.exception.ProductNotFoundException;
import com.nttdata.catalog.service.ProductService;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ProductPageResponse findProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String format,
            @RequestParam(defaultValue = "0") @Min(0) int page) {
        return ProductPageResponse.from(service.findProducts(search, category, format, page));
    }

    @GetMapping("/filters")
    public ProductFiltersResponse findFilters() {
        return ProductFiltersResponse.from(service.findFilters());
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable String id) {
        return ProductResponse.from(service.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id)));
    }
}
