package com.example.virtualclothingstore.controller;

import com.example.virtualclothingstore.dto.CategoryDTO;
import com.example.virtualclothingstore.entity.Category;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategoryDTOs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO categoryDTO = categoryService.getCategoryDTOById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return ResponseEntity.ok(categoryDTO);
    }

    @PostMapping
    public CategoryDTO createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        Category category = categoryService.fromDTO(categoryDTO);
        Category saved = categoryService.saveCategory(category);
        return categoryService.toDTO(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        if (!categoryService.getCategoryById(id).isPresent()) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        Category category = categoryService.fromDTO(categoryDTO);
        category.setId(id);
        Category saved = categoryService.saveCategory(category);
        return ResponseEntity.ok(categoryService.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (!categoryService.getCategoryById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}