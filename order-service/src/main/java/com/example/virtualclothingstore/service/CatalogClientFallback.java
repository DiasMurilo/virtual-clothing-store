package com.example.virtualclothingstore.service;

import com.example.virtualclothingstore.dto.ProductDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CatalogClientFallback implements CatalogClient {

    @Override
    public List<ProductDTO> getAllProducts() {
        // return empty list when catalog service is unavailable
        return Collections.emptyList();
    }

    @Override
    public ProductDTO getProductById(Long id) {
        // indicate failure by returning null; callers should handle absence
        return null;
    }
}
