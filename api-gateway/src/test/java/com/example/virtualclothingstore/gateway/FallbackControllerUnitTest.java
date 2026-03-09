package com.example.virtualclothingstore.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FallbackController Unit Tests")
class FallbackControllerUnitTest {
    private final FallbackController controller = new FallbackController();

    @Test
    @DisplayName("root fallback returns empty list")
    void defaultFallback_returnsEmpty() {
        ResponseEntity<List<Object>> resp = controller.defaultFallback();
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
    }

    @Test
    @DisplayName("products fallback returns empty list")
    void products_returnsEmpty() {
        ResponseEntity<List<Object>> resp = controller.products();
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
    }

    @Test
    @DisplayName("orders fallback returns empty list")
    void orders_returnsEmpty() {
        ResponseEntity<List<Object>> resp = controller.orders();
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
    }
}