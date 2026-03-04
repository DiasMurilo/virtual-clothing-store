package com.example.virtualclothingstore.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
    }
)
// Use an in-memory database so the full context can start without needing
// an external PostgreSQL instance.  The auto-configured H2 is sufficient
// for a simple smoke test.
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class OrderServiceE2ETest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void whenApplicationRuns_thenGetOrdersReturnsOk() {
        ResponseEntity<String> response = rest.getForEntity("/api/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // basic assertion, further assertions not needed for simple end-to-end check
    }
}
