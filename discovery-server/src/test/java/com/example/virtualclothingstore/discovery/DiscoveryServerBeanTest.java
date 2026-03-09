package com.example.virtualclothingstore.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DiscoveryServerBeanTest {

    @Autowired
    private ApplicationContext ctx;

    @Test
    void discoveryServerApplicationBeanExists() {
        assertNotNull(ctx.getBean(DiscoveryServerApplication.class));
    }
}