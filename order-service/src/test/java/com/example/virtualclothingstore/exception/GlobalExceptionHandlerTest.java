package com.example.virtualclothingstore.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @Test
    @DisplayName("handleMethodNotSupported returns 405")
    void handleMethodNotSupported() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("DELETE");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resp = handler.handleMethodNotSupported(ex, request);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @Test
    @DisplayName("handleValidationExceptions returns 400 with field errors map")
    void handleValidationExceptions() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "email", "Email is required");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> resp =
                handler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Email is required", resp.getBody().getFieldErrors().get("email"));
    }

    // -----------------------------------------------------------------------
    // ErrorResponse inner class – cover all getters and setters
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ErrorResponse getters and setters work correctly")
    void errorResponse_gettersAndSetters() {
        LocalDateTime now = LocalDateTime.now();
        GlobalExceptionHandler.ErrorResponse er =
                new GlobalExceptionHandler.ErrorResponse(now, 404, "Not Found", "msg", "/path");

        // Verify constructor values via getters
        assertEquals(now, er.getTimestamp());
        assertEquals(404, er.getStatus());
        assertEquals("Not Found", er.getError());
        assertEquals("msg", er.getMessage());
        assertEquals("/path", er.getPath());

        // Exercise setters
        LocalDateTime later = now.plusDays(1);
        er.setTimestamp(later);
        er.setStatus(500);
        er.setError("Internal Server Error");
        er.setMessage("updated msg");
        er.setPath("/new-path");

        assertEquals(later, er.getTimestamp());
        assertEquals(500, er.getStatus());
        assertEquals("Internal Server Error", er.getError());
        assertEquals("updated msg", er.getMessage());
        assertEquals("/new-path", er.getPath());
    }

    // -----------------------------------------------------------------------
    // ValidationErrorResponse inner class – cover constructor, all getters/setters
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ValidationErrorResponse getters and setters work correctly")
    void validationErrorResponse_gettersAndSetters() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> fieldErrors = Map.of("name", "Name is required");
        GlobalExceptionHandler.ValidationErrorResponse ver =
                new GlobalExceptionHandler.ValidationErrorResponse(
                        now, 400, "Validation Failed", "Input validation failed", "/api/customers", fieldErrors);

        // Verify constructor values via getters
        assertEquals(now, ver.getTimestamp());
        assertEquals(400, ver.getStatus());
        assertEquals("Validation Failed", ver.getError());
        assertEquals("Input validation failed", ver.getMessage());
        assertEquals("/api/customers", ver.getPath());
        assertEquals(fieldErrors, ver.getFieldErrors());

        // Exercise setters
        LocalDateTime later = now.plusHours(1);
        Map<String, String> newErrors = Map.of("email", "Email is invalid");
        ver.setTimestamp(later);
        ver.setStatus(422);
        ver.setError("Unprocessable Entity");
        ver.setMessage("new msg");
        ver.setPath("/api/orders");
        ver.setFieldErrors(newErrors);

        assertEquals(later, ver.getTimestamp());
        assertEquals(422, ver.getStatus());
        assertEquals("Unprocessable Entity", ver.getError());
        assertEquals("new msg", ver.getMessage());
        assertEquals("/api/orders", ver.getPath());
        assertEquals(newErrors, ver.getFieldErrors());
    }
}