package com.example.virtualclothingstore.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ConfigServerBeanTest {

    @Autowired
    private ApplicationContext ctx;

    @Test
    void configServerApplicationBeanExists() {
        assertNotNull(ctx.getBean(ConfigServerApplication.class));
    }
}