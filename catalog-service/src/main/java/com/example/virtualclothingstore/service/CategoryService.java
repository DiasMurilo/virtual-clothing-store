package com.example.virtualclothingstore.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.virtualclothingstore.dto.CategoryDTO;
import com.example.virtualclothingstore.entity.Category;
import com.example.virtualclothingstore.repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // DTO conversion methods
    public CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }

    public Category fromDTO(CategoryDTO dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    public List<CategoryDTO> getAllCategoryDTOs() {
        return getAllCategories().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<CategoryDTO> getCategoryDTOById(Long id) {
        return getCategoryById(id).map(this::toDTO);
    }
}