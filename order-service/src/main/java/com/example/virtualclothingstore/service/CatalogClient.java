package com.example.virtualclothingstore.service;

import com.example.virtualclothingstore.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "catalog-service", fallback = CatalogClientFallback.class)
public interface CatalogClient {
    @GetMapping("/api/products")
    List<ProductDTO> getAllProducts();

    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);
}
