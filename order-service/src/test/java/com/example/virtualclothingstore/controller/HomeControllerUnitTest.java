package com.example.virtualclothingstore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("HomeController Unit Tests")
class HomeControllerUnitTest {

    @InjectMocks
    private HomeController controller;

    @Test
    @DisplayName("home() returns welcome map")
    void home_returnsWelcome() {
        ResponseEntity<?> resp = controller.home();
        assertEquals(200, resp.getStatusCodeValue());
        // controller returns a map containing a message entry
        @SuppressWarnings("unchecked")
        var body = (java.util.Map<String,String>)resp.getBody();
        assertEquals("Welcome to Virtual Clothing Store API", body.get("message"));
    }
}
