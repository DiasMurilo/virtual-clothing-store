package com.example.virtualclothingstore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Catalog Exception Classes")
class CatalogExceptionsTest {

    @Test
    void resourceNotFoundException_storesMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("item missing");
        assertEquals("item missing", ex.getMessage());
    }

    @Test
    void badRequestException_noArg() {
        BadRequestException ex = new BadRequestException();
        assertNotNull(ex);
    }

    @Test
    void badRequestException_withMessage() {
        BadRequestException ex = new BadRequestException("invalid input");
        assertEquals("invalid input", ex.getMessage());
    }
}
