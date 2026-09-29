package com.nttdata.catalog.controller;

import com.nttdata.catalog.exception.ApiExceptionHandler;
import com.nttdata.catalog.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductApiErrorTests {
    @Test
    void unexpectedFailureReturns500WithoutLeakingInternalDetails() throws Exception {
        var service = mock(ProductService.class);
        when(service.findFilters()).thenThrow(new IllegalStateException("private filesystem details"));
        var mvc = MockMvcBuilders.standaloneSetup(new ProductController(service))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/api/products/filters"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred. Please try again later."))
                .andExpect(content().string(not(containsString("private filesystem details"))));
    }

    @Test
    void unsupportedMethodKeeps405InsteadOfBecoming500() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new ProductController(mock(ProductService.class)))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(post("/api/products")).andExpect(status().isMethodNotAllowed());
    }
}
