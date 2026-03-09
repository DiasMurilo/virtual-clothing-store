package com.example.virtualclothingstore.service;

import com.example.virtualclothingstore.dto.ProductDTO;
import com.example.virtualclothingstore.entity.Category;
import com.example.virtualclothingstore.entity.Product;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService service;

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Tops");

        product = new Product();
        product.setId(1L);
        product.setName("T-Shirt");
        product.setDescription("Cotton T-Shirt");
        product.setPrice(new BigDecimal("19.99"));
        product.setStockQuantity(100);
        product.setCategory(category);
    }

    @Test
    void getAllProducts_returnsList() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        assertEquals(1, service.getAllProducts().size());
    }

    @Test
    void getProductById_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        assertTrue(service.getProductById(1L).isPresent());
    }

    @Test
    void getProductById_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertFalse(service.getProductById(99L).isPresent());
    }

    @Test
    void saveProduct_delegates() {
        when(productRepository.save(product)).thenReturn(product);
        assertEquals(product, service.saveProduct(product));
    }

    @Test
    void deleteProduct_delegates() {
        service.deleteProduct(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void toDTO_mapsAllFieldsWithCategory() {
        ProductDTO dto = service.toDTO(product);
        assertEquals(1L, dto.getId());
        assertEquals("T-Shirt", dto.getName());
        assertEquals(new BigDecimal("19.99"), dto.getPrice());
        assertEquals(100, dto.getStockQuantity());
        assertEquals(1L, dto.getCategoryId());
        assertEquals("Tops", dto.getCategoryName());
    }

    @Test
    void toDTO_nullCategoryHandled() {
        product.setCategory(null);
        ProductDTO dto = service.toDTO(product);
        assertNull(dto.getCategoryId());
        assertNull(dto.getCategoryName());
    }

    @Test
    void fromDTO_withValidCategoryId() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Jeans");
        dto.setPrice(new BigDecimal("49.99"));
        dto.setStockQuantity(50);
        dto.setCategoryId(1L);

        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(category));

        Product result = service.fromDTO(dto);
        assertEquals("Jeans", result.getName());
        assertEquals(category, result.getCategory());
    }

    @Test
    void fromDTO_invalidCategoryIdThrows() {
        ProductDTO dto = new ProductDTO();
        dto.setCategoryId(99L);

        when(categoryService.getCategoryById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.fromDTO(dto));
    }

    @Test
    void fromDTO_nullCategoryIdUsesDefault() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Socks");
        dto.setCategoryId(null);

        Category general = new Category();
        general.setName("General");
        when(categoryService.getAllCategories()).thenReturn(List.of(general));

        Product result = service.fromDTO(dto);
        assertEquals("General", result.getCategory().getName());
    }

    @Test
    void getAllProductDTOs_returnsMappedList() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        List<ProductDTO> dtos = service.getAllProductDTOs();
        assertEquals(1, dtos.size());
        assertEquals("T-Shirt", dtos.get(0).getName());
    }

    @Test
    void getProductDTOById_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        Optional<ProductDTO> result = service.getProductDTOById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    void getProductDTOById_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertFalse(service.getProductDTOById(99L).isPresent());
    }
}
