package com.example.virtualclothingstore.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.virtualclothingstore.dto.OrderDTO;
import com.example.virtualclothingstore.entity.Order;
import com.example.virtualclothingstore.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration test for OrderController using @WebMvcTest.
 * This is a slice test - it loads only the web layer and mocks the service layer.
 * Positioned at the middle of the Test Pyramid - fewer tests than unit level.
 */
@WebMvcTest(OrderController.class)
@DisplayName("OrderController Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderDTO testOrderDTO;

    @BeforeEach
    void setUp() {
        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(1L);
        testOrderDTO.setCustomerId(1L);
        testOrderDTO.setCustomerName("John Doe");
        testOrderDTO.setOrderDate(LocalDateTime.of(2026, 3, 1, 10, 0));
        testOrderDTO.setStatus("PENDING");
        testOrderDTO.setTotalAmount(new BigDecimal("100.00"));
        testOrderDTO.setItems(Collections.emptyList());
    }

    @Test
    @DisplayName("GET /api/orders should return paginated orders")
    void getAllOrders_returnsPaginatedOrders() throws Exception {
        // Arrange
        Page<OrderDTO> page = new PageImpl<>(Arrays.asList(testOrderDTO));
        when(orderService.getAllOrderDTOs(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].customerName").value("John Doe"))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));

        verify(orderService).getAllOrderDTOs(any());
    }

    @Test
    @DisplayName("GET /api/orders/{id} should return order when found")
    void getOrderById_whenExists_returnsOrder() throws Exception {
        // Arrange
        when(orderService.getOrderDTOById(1L)).thenReturn(Optional.of(testOrderDTO));

        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.totalAmount").value(100.00));

        verify(orderService).getOrderDTOById(1L);
    }

    @Test
    @DisplayName("GET /api/orders/{id} should return 404 when not found")
    void getOrderById_whenNotExists_returns404() throws Exception {
        // Arrange
        when(orderService.getOrderDTOById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());

        verify(orderService).getOrderDTOById(99L);
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId} should filter by customer")
    void getOrdersByCustomer_returnsFilteredOrders() throws Exception {
        // Arrange
        Page<OrderDTO> page = new PageImpl<>(Arrays.asList(testOrderDTO));
        when(orderService.getOrderDTOsByCustomerId(eq(1L), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].customerId").value(1));

        verify(orderService).getOrderDTOsByCustomerId(eq(1L), any());
    }

    @Test
    @DisplayName("POST /api/orders should create new order")
    void createOrder_createsAndReturnsOrder() throws Exception {
        // Arrange
        Order mockOrder = new Order();
        mockOrder.setId(1L);
        
        when(orderService.fromDTO(any(OrderDTO.class))).thenReturn(mockOrder);
        when(orderService.saveOrder(any(Order.class))).thenReturn(mockOrder);
        when(orderService.toDTO(any(Order.class))).thenReturn(testOrderDTO);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService).fromDTO(any(OrderDTO.class));
        verify(orderService).saveOrder(any(Order.class));
        verify(orderService).toDTO(any(Order.class));
    }

    @Test
    @DisplayName("POST /api/orders/{orderId}/products should add product to order")
    void addProductToOrder_addsProduct() throws Exception {
        // Arrange
        doNothing().when(orderService).addProductToOrder(1L, 10L, 2);

        // Act & Assert
        mockMvc.perform(post("/api/orders/1/products")
                .param("productId", "10")
                .param("quantity", "2"))
                .andExpect(status().isOk());

        verify(orderService).addProductToOrder(1L, 10L, 2);
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} should delete order when exists")
    void deleteOrder_whenExists_deletesOrder() throws Exception {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(new Order()));
        doNothing().when(orderService).deleteOrder(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService).getOrderById(1L);
        verify(orderService).deleteOrder(1L);
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} should return 404 when not found")
    void deleteOrder_whenNotExists_returns404() throws Exception {
        // Arrange
        when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/orders/99"))
                .andExpect(status().isNotFound());

        verify(orderService).getOrderById(99L);
        verify(orderService, never()).deleteOrder(anyLong());
    }

    @Test
    @DisplayName("PUT /api/orders/{id} should update order when exists")
    void updateOrder_whenExists_updatesOrder() throws Exception {
        // Arrange
        Order mockOrder = new Order();
        mockOrder.setId(1L);
        
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderService.fromDTO(any(OrderDTO.class))).thenReturn(mockOrder);
        when(orderService.saveOrder(any(Order.class))).thenReturn(mockOrder);
        when(orderService.toDTO(any(Order.class))).thenReturn(testOrderDTO);

        // Act & Assert
        mockMvc.perform(put("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(orderService).getOrderById(1L);
        verify(orderService).saveOrder(any(Order.class));
    }
}
