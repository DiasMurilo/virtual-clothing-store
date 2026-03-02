package com.example.virtualclothingstore.controller;

import com.example.virtualclothingstore.dto.OrderDTO;
import com.example.virtualclothingstore.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderControllerTest {

    private OrderController controller;
    private StubOrderService stubService;

    static class StubOrderService extends OrderService {
        private Optional<OrderDTO> result;
        public void setResult(Optional<OrderDTO> r) { this.result = r; }
        @Override public Optional<OrderDTO> getOrderDTOById(Long id) { return result; }
        // other methods inherited but unused
    }

    @BeforeEach
    void setup() {
        stubService = new StubOrderService();
        controller = new OrderController();
        try {
            java.lang.reflect.Field f = OrderController.class.getDeclaredField("orderService");
            f.setAccessible(true);
            f.set(controller, stubService);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    void getOrderById_returnsOrderDto() {
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);
        ((StubOrderService)stubService).setResult(Optional.of(dto));

        ResponseEntity<OrderDTO> resp = controller.getOrderById(1L);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody().getId()).isEqualTo(1L);
    }

    @Test
    void getOrderById_notFound_throws() {
        ((StubOrderService)stubService).setResult(Optional.empty());
        try {
            controller.getOrderById(2L);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Order not found");
        }
    }
}
