package com.example.virtualclothingstore.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Catalog Entity Getters/Setters")
class CatalogEntityTest {

    @Test
    void categoryEntity_accessors() {
        Category c = new Category();
        c.setId(1L);
        c.setName("Shoes");
        c.setDescription("Footwear");
        assertEquals(1L, c.getId());
        assertEquals("Shoes", c.getName());
        assertEquals("Footwear", c.getDescription());
    }

    @Test
    void categoryEntity_constructor() {
        Category c = new Category("Hats", "Headwear");
        assertEquals("Hats", c.getName());
        assertEquals("Headwear", c.getDescription());
    }

    @Test
    void productEntity_accessors() {
        Category cat = new Category();
        cat.setId(1L);

        Product p = new Product();
        p.setId(10L);
        p.setName("T-Shirt");
        p.setDescription("Cotton");
        p.setPrice(new BigDecimal("9.99"));
        p.setStockQuantity(50);
        p.setCategory(cat);

        assertEquals(10L, p.getId());
        assertEquals("T-Shirt", p.getName());
        assertEquals("Cotton", p.getDescription());
        assertEquals(new BigDecimal("9.99"), p.getPrice());
        assertEquals(50, p.getStockQuantity());
        assertEquals(cat, p.getCategory());
    }

    @Test
    void productEntity_allArgsConstructor() {
        Category cat = new Category();
        Product p = new Product("Jeans", "Denim", new BigDecimal("49.99"), 25, cat);
        assertEquals("Jeans", p.getName());
        assertEquals(25, p.getStockQuantity());
    }
}
