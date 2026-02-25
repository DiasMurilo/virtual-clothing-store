package com.example.virtualclothingstore.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes the externalized demo-message from Config Server.
 * @RefreshScope ensures the property is re-read after POST /actuator/refresh.
 */
@RefreshScope
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Value("${app.demo-message:not set}")
    private String demoMessage;

    @GetMapping("/message")
    public ResponseEntity<Map<String, String>> getMessage() {
        return ResponseEntity.ok(Map.of("message", demoMessage));
    }
}
