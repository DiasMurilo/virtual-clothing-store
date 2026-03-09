package com.example.virtualclothingstore.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.virtualclothingstore.dto.ProductDTO;

@DisplayName("CatalogClientFallback Tests")
class CatalogClientFallbackTest {

    private final CatalogClientFallback fallback = new CatalogClientFallback();

    @Test
    @DisplayName("getAllProducts returns empty list")
    void getAllProducts_empty() {
        assertEquals(0, fallback.getAllProducts().size());
    }

    @Test
    @DisplayName("getProductById returns null")
    void getProductById_null() {
        assertNull(fallback.getProductById(123L));
    }
}