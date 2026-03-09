package com.example.virtualclothingstore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigController Unit Tests")
class ConfigControllerUnitTest {

    @InjectMocks
    private ConfigController controller;

    @Test
    @DisplayName("getMessage returns configured demo message")
    void getMessage_returnsConfigured() {
        // set private field via reflection
        ReflectionTestUtils.setField(controller, "demoMessage", "hello");

        ResponseEntity<Map<String, String>> resp = controller.getMessage();
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("hello", resp.getBody().get("message"));
    }
}
