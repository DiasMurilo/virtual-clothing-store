package com.example.virtualclothingstore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.example.virtualclothingstore.dto.CategoryDTO;
import com.example.virtualclothingstore.entity.Category;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.service.CategoryService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController Unit Tests")
class CategoryControllerUnitTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController controller;

    private CategoryDTO sampleDto;
    private Category sampleEntity;

    @BeforeEach
    void setUp() {
        sampleDto = new CategoryDTO();
        sampleDto.setId(1L);
        sampleDto.setName("Foo");
        sampleEntity = new Category();
        sampleEntity.setId(1L);
        sampleEntity.setName("Foo");
    }

    @Test
    @DisplayName("getAllCategories returns list")
    void getAllCategories_returnsList() {
        when(categoryService.getAllCategoryDTOs()).thenReturn(List.of(sampleDto));

        List<CategoryDTO> result = controller.getAllCategories();
        assertEquals(1, result.size());
        assertEquals(sampleDto, result.get(0));
    }

    @Test
    @DisplayName("getCategoryById returns found entity")
    void getCategoryById_found() {
        when(categoryService.getCategoryDTOById(1L)).thenReturn(Optional.of(sampleDto));

        ResponseEntity<CategoryDTO> resp = controller.getCategoryById(1L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(sampleDto, resp.getBody());
    }

    @Test
    @DisplayName("getCategoryById throws when missing")
    void getCategoryById_missing() {
        when(categoryService.getCategoryDTOById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> controller.getCategoryById(2L));
    }

    @Test
    @DisplayName("createCategory maps and returns DTO")
    void createCategory_mapsAndReturns() {
        when(categoryService.fromDTO(sampleDto)).thenReturn(sampleEntity);
        when(categoryService.saveCategory(sampleEntity)).thenReturn(sampleEntity);
        when(categoryService.toDTO(sampleEntity)).thenReturn(sampleDto);

        CategoryDTO result = controller.createCategory(sampleDto);
        assertEquals(sampleDto, result);
        verify(categoryService).fromDTO(sampleDto);
        verify(categoryService).saveCategory(sampleEntity);
    }

    @Test
    @DisplayName("updateCategory when exists updates")
    void updateCategory_whenExists() {
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(sampleEntity));
        Category converted = new Category();
        when(categoryService.fromDTO(sampleDto)).thenReturn(converted);
        when(categoryService.saveCategory(converted)).thenReturn(sampleEntity);
        when(categoryService.toDTO(sampleEntity)).thenReturn(sampleDto);

        ResponseEntity<CategoryDTO> resp = controller.updateCategory(1L, sampleDto);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(sampleDto, resp.getBody());
    }

    @Test
    @DisplayName("updateCategory missing throws")
    void updateCategory_missing() {
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> controller.updateCategory(1L, sampleDto));
    }

    @Test
    @DisplayName("deleteCategory when not found returns 404")
    void deleteCategory_notFound() {
        when(categoryService.getCategoryById(5L)).thenReturn(Optional.empty());
        ResponseEntity<Void> resp = controller.deleteCategory(5L);
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("deleteCategory when present returns 204")
    void deleteCategory_present() {
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(sampleEntity));
        ResponseEntity<Void> resp = controller.deleteCategory(1L);
        assertEquals(204, resp.getStatusCodeValue());
    }
}
