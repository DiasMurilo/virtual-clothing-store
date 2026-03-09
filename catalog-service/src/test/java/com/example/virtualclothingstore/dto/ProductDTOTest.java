package com.example.virtualclothingstore.dto;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductDTO Tests")
class ProductDTOTest {

    @Test
    @DisplayName("default constructor creates empty ProductDTO")
    void defaultConstructor_createsEmpty() {
        ProductDTO dto = new ProductDTO();
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getPrice());
        assertNull(dto.getStockQuantity());
        assertNull(dto.getCategoryId());
        assertNull(dto.getCategoryName());
    }

    @Test
    @DisplayName("parameterized constructor sets all fields")
    void parameterizedConstructor_setsAllFields() {
        ProductDTO dto = new ProductDTO(
                1L, "Blue Jeans", "Slim fit", new BigDecimal("59.99"), 100, 2L, "Bottoms");

        assertEquals(1L, dto.getId());
        assertEquals("Blue Jeans", dto.getName());
        assertEquals("Slim fit", dto.getDescription());
        assertEquals(new BigDecimal("59.99"), dto.getPrice());
        assertEquals(100, dto.getStockQuantity());
        assertEquals(2L, dto.getCategoryId());
        assertEquals("Bottoms", dto.getCategoryName());
    }

    @Test
    @DisplayName("setters update all fields")
    void setters_updateAllFields() {
        ProductDTO dto = new ProductDTO();
        dto.setId(5L);
        dto.setName("White T-Shirt");
        dto.setDescription("100% cotton");
        dto.setPrice(new BigDecimal("19.99"));
        dto.setStockQuantity(50);
        dto.setCategoryId(3L);
        dto.setCategoryName("Tops");

        assertEquals(5L, dto.getId());
        assertEquals("White T-Shirt", dto.getName());
        assertEquals("100% cotton", dto.getDescription());
        assertEquals(new BigDecimal("19.99"), dto.getPrice());
        assertEquals(50, dto.getStockQuantity());
        assertEquals(3L, dto.getCategoryId());
        assertEquals("Tops", dto.getCategoryName());
    }
}
