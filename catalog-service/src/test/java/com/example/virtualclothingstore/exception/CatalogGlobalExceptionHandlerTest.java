package com.example.virtualclothingstore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Catalog GlobalExceptionHandler Unit Tests")
class CatalogGlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        ResponseEntity<String> resp = handler.handleNotFound(ex);
        assertEquals(404, resp.getStatusCodeValue());
        assertEquals("not found", resp.getBody());
    }

    @Test
    void handleBadRequest_returns400() {
        BadRequestException ex = new BadRequestException("bad input");
        ResponseEntity<String> resp = handler.handleBadRequest(ex);
        assertEquals(400, resp.getStatusCodeValue());
        assertEquals("bad input", resp.getBody());
    }

    @Test
    void handleGeneric_returns500() {
        Exception ex = new Exception("something broke");
        ResponseEntity<String> resp = handler.handleGeneric(ex);
        assertEquals(500, resp.getStatusCodeValue());
        assertTrue(resp.getBody().contains("something broke"));
    }
}
