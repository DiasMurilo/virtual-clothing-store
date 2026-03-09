package com.example.virtualclothingstore.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CategoryDTO getters/setters")
class CategoryDTOTest {

    @Test
    void constructorAndAccessors() {
        CategoryDTO dto = new CategoryDTO(5L, "Apparel", "Clothing items");
        assertEquals(5L, dto.getId());
        assertEquals("Apparel", dto.getName());
        assertEquals("Clothing items", dto.getDescription());

        dto.setName("Shoes");
        dto.setDescription("Footwear");
        assertEquals("Shoes", dto.getName());
        assertEquals("Footwear", dto.getDescription());
    }
}