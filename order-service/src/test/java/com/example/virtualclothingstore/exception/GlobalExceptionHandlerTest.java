package com.example.virtualclothingstore.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    @Test
    @DisplayName("handleResourceNotFound returns 404 with message")
    void handleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleResourceNotFoundException(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("not found", resp.getBody().getMessage());
    }

    @Test
    @DisplayName("handleBadRequest returns 400")
    void handleBadRequest() {
        BadRequestException ex = new BadRequestException("bad");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleBadRequestException(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    @DisplayName("handleGlobalException returns 500")
    void handleGlobal() {
        Exception ex = new Exception("oops");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleGlobalException(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertEquals("An unexpected error occurred", resp.getBody().getMessage());
    }
}