package com.example.virtualclothingstore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.example.virtualclothingstore.dto.OrderDTO;
import com.example.virtualclothingstore.entity.Order;
import com.example.virtualclothingstore.exception.BadRequestException;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.service.OrderService;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Unit Tests")
public class OrderControllerUnitTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController controller;

    private OrderDTO sampleDto;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleDto = new OrderDTO();
        sampleDto.setId(1L);

        sampleOrder = new Order();
        sampleOrder.setId(1L);
    }

    @Test
    @DisplayName("getOrderById returns 200 when found")
    void getOrderById_whenFound_returnsOk() {
        when(orderService.getOrderDTOById(1L)).thenReturn(Optional.of(sampleDto));

        ResponseEntity<OrderDTO> response = controller.getOrderById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(sampleDto, response.getBody());
    }

    @Test
    @DisplayName("getOrderById throws ResourceNotFoundException when missing")
    void getOrderById_whenMissing_throwsNotFound() {
        when(orderService.getOrderDTOById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> controller.getOrderById(2L));
    }

    @Test
    @DisplayName("addProductToOrder converts RuntimeException to BadRequest")
    void addProductToOrder_whenServiceThrows_translatesToBadRequest() {
        when(orderService.addProductToOrder(1L, 2L, 1)).thenThrow(new RuntimeException("fail"));

        assertThrows(BadRequestException.class, () -> {
            controller.addProductToOrder(1L, 2L, 1);
        });
    }

    @Test
    @DisplayName("deleteOrder returns not found when service indicates absent")
    void deleteOrder_whenNotFound_returns404() {
        when(orderService.getOrderById(5L)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = controller.deleteOrder(5L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("deleteOrder returns no content when present")
    void deleteOrder_whenPresent_returns204() {
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(sampleOrder));

        ResponseEntity<Void> response = controller.deleteOrder(1L);
        assertEquals(204, response.getStatusCodeValue());
    }
}
