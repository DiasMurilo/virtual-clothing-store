package com.example.virtualclothingstore.controller;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

@DisplayName("HomeController Unit Tests")
class HomeControllerUnitTest {

    private final HomeController controller = new HomeController();

    @Test
    @DisplayName("home endpoint returns welcome map")
    void home_returnsWelcomeMap() {
        ResponseEntity<Map<String,String>> resp = controller.home();
        assertEquals(200, resp.getStatusCodeValue());
        Map<String,String> body = resp.getBody();
        assertEquals("Welcome to Virtual Clothing Store API", body.get("message"));
        assertEquals("1.0", body.get("version"));
        assertEquals("running", body.get("status"));
    }
}
