package com.example.virtualclothingstore.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Arrays;

import com.example.virtualclothingstore.dto.OrderDTO;
import com.example.virtualclothingstore.entity.Order;
import com.example.virtualclothingstore.exception.BadRequestException;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.service.OrderService;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Unit Tests")
class OrderControllerUnitTest {

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
    @DisplayName("addProductToOrder propagates BadRequestException from service")
    void addProductToOrder_whenServiceThrowsBadRequest_rethrowsBadRequest() {
        doThrow(new BadRequestException("invalid quantity")).when(orderService).addProductToOrder(1L, 2L, 1);

        assertThrows(BadRequestException.class, () -> controller.addProductToOrder(1L, 2L, 1));
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

    @Test
    @DisplayName("getAllOrders returns page of DTOs")
    void getAllOrders_returnsPage() {
        Page<OrderDTO> page = new PageImpl<>(Arrays.asList(sampleDto));
        when(orderService.getAllOrderDTOs(any(Pageable.class))).thenReturn(page);

        Page<OrderDTO> result = controller.getAllOrders(null, null, 0, 10);
        assertEquals(1, result.getTotalElements());
        assertEquals(sampleDto, result.getContent().get(0));
    }

    @Test
    @DisplayName("getAllOrders with date range delegates to date range service method")
    void getAllOrders_withDateRange_callsDateRangeMethod() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        Page<OrderDTO> page = new PageImpl<>(Arrays.asList(sampleDto));
        when(orderService.getOrderDTOsByDateRange(eq(start), eq(end), any(Pageable.class))).thenReturn(page);

        Page<OrderDTO> result = controller.getAllOrders(start, end, 0, 10);
        assertEquals(1, result.getTotalElements());
        verify(orderService).getOrderDTOsByDateRange(eq(start), eq(end), any(Pageable.class));
    }

    @Test
    @DisplayName("removeProductFromOrder wraps RuntimeException in BadRequestException")
    void removeProductFromOrder_whenServiceThrows_rethrowsBadRequest() {
        doThrow(new RuntimeException("product not found")).when(orderService).removeProductFromOrder(1L, 99L);

        assertThrows(BadRequestException.class, () -> controller.removeProductFromOrder(1L, 99L));
    }

    @Test
    @DisplayName("getOrdersByCustomer returns filtered page")
    void getOrdersByCustomer_returnsFilteredPage() {
        Page<OrderDTO> page = new PageImpl<>(Arrays.asList(sampleDto));
        when(orderService.getOrderDTOsByCustomerId(eq(5L), any(Pageable.class))).thenReturn(page);

        Page<OrderDTO> result = controller.getOrdersByCustomer(5L, 0, 5);
        assertEquals(1, result.getTotalElements());
        verify(orderService).getOrderDTOsByCustomerId(eq(5L), any(Pageable.class));
    }

    @Test
    @DisplayName("createOrder maps and returns DTO")
    void createOrder_mapsAndReturns() {
        Order order = new Order();
        order.setId(2L);
        when(orderService.fromDTO(sampleDto)).thenReturn(order);
        when(orderService.saveOrder(order)).thenReturn(order);
        when(orderService.toDTO(order)).thenReturn(sampleDto);

        OrderDTO result = controller.createOrder(sampleDto);
        assertEquals(sampleDto, result);
        verify(orderService).fromDTO(sampleDto);
        verify(orderService).saveOrder(order);
    }

    @Test
    @DisplayName("addProductToOrder success returns ok")
    void addProductToOrder_success_returnsOk() {
        doNothing().when(orderService).addProductToOrder(1L, 2L, 3);
        ResponseEntity<Void> resp = controller.addProductToOrder(1L, 2L, 3);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("removeProductFromOrder success returns no content")
    void removeProductFromOrder_success_returns204() {
        doNothing().when(orderService).removeProductFromOrder(1L, 2L);
        ResponseEntity<Void> resp = controller.removeProductFromOrder(1L, 2L);
        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("updateOrder when exists updates and returns DTO")
    void updateOrder_whenExists_updates() {
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderService.fromDTO(sampleDto)).thenReturn(new Order());
        Order testOrder = new Order();
        testOrder.setId(2L);
        when(orderService.saveOrder(any(Order.class))).thenReturn(testOrder);
        when(orderService.toDTO(testOrder)).thenReturn(sampleDto);

        ResponseEntity<OrderDTO> resp = controller.updateOrder(1L, sampleDto);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(sampleDto, resp.getBody());
    }

    @Test
    @DisplayName("updateOrder when missing throws NotFound")
    void updateOrder_whenMissing_throwsNotFound() {
        when(orderService.getOrderById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> controller.updateOrder(1L, sampleDto));
    }
}
