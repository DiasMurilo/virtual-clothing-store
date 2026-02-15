package com.example.virtualclothingstore.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.virtualclothingstore.dto.ProductDTO;
import com.example.virtualclothingstore.entity.Category;
import com.example.virtualclothingstore.entity.Product;
import com.example.virtualclothingstore.repository.ProductRepository;
import com.example.virtualclothingstore.service.CategoryService;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // DTO conversion methods
    public ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);
        return dto;
    }

    public Product fromDTO(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());

        // Set category
        if (dto.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        } else {
            // Assign default category
            Category defaultCategory = getOrCreateDefaultCategory();
            product.setCategory(defaultCategory);
        }

        return product;
    }

    private Category getOrCreateDefaultCategory() {
        // Try to find existing default category
        Optional<Category> existingDefault = categoryService.getAllCategories().stream()
                .filter(cat -> "General".equals(cat.getName()))
                .findFirst();

        if (existingDefault.isPresent()) {
            return existingDefault.get();
        }

        // Create new default category
        Category defaultCategory = new Category();
        defaultCategory.setName("General");
        defaultCategory.setDescription("Default category for products without specific category");
        return categoryService.saveCategory(defaultCategory);
    }

    public List<ProductDTO> getAllProductDTOs() {
        return getAllProducts().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductDTO> getAllProductDTOs(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::toDTO);
    }

    public Optional<ProductDTO> getProductDTOById(Long id) {
        return getProductById(id).map(this::toDTO);
    }
}