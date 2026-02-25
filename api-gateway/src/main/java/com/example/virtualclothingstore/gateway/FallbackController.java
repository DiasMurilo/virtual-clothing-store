package com.example.virtualclothingstore.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /** Root fallback — catches the global default circuit breaker forward */
    @GetMapping
    public ResponseEntity<List<Object>> defaultFallback() {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/api/products")
    public ResponseEntity<List<Object>> products() {
        // return empty list when catalogue-service is unreachable
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/api/orders")
    public ResponseEntity<List<Object>> orders() {
        // return empty list when order-service is unreachable
        return ResponseEntity.ok(Collections.emptyList());
    }
}
