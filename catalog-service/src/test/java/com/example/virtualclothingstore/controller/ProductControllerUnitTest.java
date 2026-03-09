package com.example.virtualclothingstore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import java.util.List;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.example.virtualclothingstore.dto.ProductDTO;
import com.example.virtualclothingstore.entity.Product;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.service.ProductService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerUnitTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController controller;

    private ProductDTO sampleDto;
    private Product sampleEntity;

    @BeforeEach
    void setUp() {
        sampleDto = new ProductDTO();
        sampleDto.setId(1L);
        sampleDto.setName("Widget");
        sampleEntity = new Product();
        sampleEntity.setId(1L);
        sampleEntity.setName("Widget");
    }

    @Test
    @DisplayName("getAllProducts returns page")
    void getAllProducts_returnsPage() {
        Page<ProductDTO> page = new PageImpl<>(List.of(sampleDto));
        when(productService.getAllProductDTOs(any(Pageable.class))).thenReturn(page);

        Page<ProductDTO> result = controller.getAllProducts(0, 10);
        assertEquals(1, result.getTotalElements());
        assertEquals(sampleDto, result.getContent().get(0));
        verify(productService).getAllProductDTOs(any(Pageable.class));
    }

    @Test
    @DisplayName("getProductById found returns DTO")
    void getProductById_found() {
        when(productService.getProductDTOById(1L)).thenReturn(Optional.of(sampleDto));

        ResponseEntity<ProductDTO> resp = controller.getProductById(1L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(sampleDto, resp.getBody());
    }

    @Test
    @DisplayName("getProductById missing throws")
    void getProductById_missing() {
        when(productService.getProductDTOById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> controller.getProductById(2L));
    }

    @Test
    @DisplayName("createProduct maps and returns")
    void createProduct_mapsAndReturns() {
        when(productService.fromDTO(sampleDto)).thenReturn(sampleEntity);
        when(productService.saveProduct(sampleEntity)).thenReturn(sampleEntity);
        when(productService.toDTO(sampleEntity)).thenReturn(sampleDto);

        ProductDTO result = controller.createProduct(sampleDto);
        assertEquals(sampleDto, result);
        verify(productService).fromDTO(sampleDto);
        verify(productService).saveProduct(sampleEntity);
    }

    @Test
    @DisplayName("updateProduct when exists updates")
    void updateProduct_whenExists() {
        when(productService.getProductById(1L)).thenReturn(Optional.of(sampleEntity));
        when(productService.fromDTO(sampleDto)).thenReturn(new Product());
        Product updated = new Product();
        updated.setId(1L);
        when(productService.saveProduct(any(Product.class))).thenReturn(updated);
        when(productService.toDTO(updated)).thenReturn(sampleDto);

        ResponseEntity<ProductDTO> resp = controller.updateProduct(1L, sampleDto);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(sampleDto, resp.getBody());
    }

    @Test
    @DisplayName("updateProduct missing throws")
    void updateProduct_missing() {
        when(productService.getProductById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> controller.updateProduct(1L, sampleDto));
    }

    @Test
    @DisplayName("deleteProduct not found returns 404")
    void deleteProduct_notFound() {
        when(productService.getProductById(5L)).thenReturn(Optional.empty());
        ResponseEntity<Void> resp = controller.deleteProduct(5L);
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("deleteProduct present returns 204")
    void deleteProduct_present() {
        when(productService.getProductById(1L)).thenReturn(Optional.of(sampleEntity));
        ResponseEntity<Void> resp = controller.deleteProduct(1L);
        assertEquals(204, resp.getStatusCodeValue());
    }
}
