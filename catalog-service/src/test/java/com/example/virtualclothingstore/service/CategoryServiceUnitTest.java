package com.example.virtualclothingstore.service;

import com.example.virtualclothingstore.dto.CategoryDTO;
import com.example.virtualclothingstore.entity.Category;
import com.example.virtualclothingstore.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceUnitTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService service;

    private Category cat;

    @BeforeEach
    void setUp() {
        cat = new Category();
        cat.setId(1L);
        cat.setName("Tops");
        cat.setDescription("Top wear");
    }

    @Test
    void getAllCategories_returnsList() {
        when(categoryRepository.findAll()).thenReturn(List.of(cat));
        assertEquals(1, service.getAllCategories().size());
    }

    @Test
    void getCategoryById_found() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        assertTrue(service.getCategoryById(1L).isPresent());
    }

    @Test
    void getCategoryById_notFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertFalse(service.getCategoryById(99L).isPresent());
    }

    @Test
    void saveCategory_delegates() {
        when(categoryRepository.save(cat)).thenReturn(cat);
        assertEquals(cat, service.saveCategory(cat));
        verify(categoryRepository).save(cat);
    }

    @Test
    void deleteCategory_delegates() {
        service.deleteCategory(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void toDTO_mapsAllFields() {
        CategoryDTO dto = service.toDTO(cat);
        assertEquals(1L, dto.getId());
        assertEquals("Tops", dto.getName());
        assertEquals("Top wear", dto.getDescription());
    }

    @Test
    void fromDTO_mapsAllFields() {
        CategoryDTO dto = new CategoryDTO(2L, "Bottoms", "Bottom wear");
        Category result = service.fromDTO(dto);
        assertEquals(2L, result.getId());
        assertEquals("Bottoms", result.getName());
        assertEquals("Bottom wear", result.getDescription());
    }

    @Test
    void getAllCategoryDTOs_returnsMappedList() {
        when(categoryRepository.findAll()).thenReturn(List.of(cat));
        List<CategoryDTO> dtos = service.getAllCategoryDTOs();
        assertEquals(1, dtos.size());
        assertEquals("Tops", dtos.get(0).getName());
    }

    @Test
    void getCategoryDTOById_found() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        Optional<CategoryDTO> result = service.getCategoryDTOById(1L);
        assertTrue(result.isPresent());
        assertEquals("Tops", result.get().getName());
    }

    @Test
    void getCategoryDTOById_notFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertFalse(service.getCategoryDTOById(99L).isPresent());
    }
}
