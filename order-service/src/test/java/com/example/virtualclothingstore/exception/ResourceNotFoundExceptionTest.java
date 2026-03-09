package com.example.virtualclothingstore.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResourceNotFoundException basic behavior")
class ResourceNotFoundExceptionTest {
    @Test
    void messageIsStored() {
        String msg = "thing not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(msg);
        assertEquals(msg, ex.getMessage());
    }
}