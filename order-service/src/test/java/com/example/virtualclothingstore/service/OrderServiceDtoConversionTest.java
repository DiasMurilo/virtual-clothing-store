package com.example.virtualclothingstore.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.virtualclothingstore.dto.OrderDTO;
import com.example.virtualclothingstore.dto.OrderItemDTO;
import com.example.virtualclothingstore.dto.ProductDTO;
import com.example.virtualclothingstore.entity.Customer;
import com.example.virtualclothingstore.entity.Order;
import com.example.virtualclothingstore.entity.OrderItem;
import com.example.virtualclothingstore.entity.OrderStatus;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.repository.OrderItemRepository;
import com.example.virtualclothingstore.repository.OrderRepository;

/**
 * Additional unit tests for OrderService covering DTO conversions, date range queries,
 * removeProductFromOrder, and pageable variants.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService DTO Conversion & Query Tests")
class OrderServiceDtoConversionTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private OrderService orderService;

    private Customer testCustomer;
    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer("Alice", "Smith", "alice@example.com", "555-1234");
        testCustomer.setId(10L);

        testOrder = new Order();
        testOrder.setId(5L);
        testOrder.setCustomer(testCustomer);
        testOrder.setOrderDate(LocalDateTime.of(2024, 6, 1, 12, 0));
        testOrder.setStatus(OrderStatus.CONFIRMED);
        testOrder.setTotalAmount(new BigDecimal("99.99"));
        testOrder.setOrderItems(new ArrayList<>());

        testOrderItem = new OrderItem();
        testOrderItem.setId(20L);
        testOrderItem.setProductId(100L);
        testOrderItem.setProductName("Blue Shirt");
        testOrderItem.setQuantity(3);
        testOrderItem.setPrice(new BigDecimal("33.33"));
        testOrderItem.setOrder(testOrder);
    }

    // -----------------------------------------------------------------------
    // getOrdersByDateRange
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getOrdersByDateRange returns orders from repository")
    void getOrdersByDateRange_returnsOrders() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        when(orderRepository.findByOrderDateBetween(start, end)).thenReturn(List.of(testOrder));

        List<Order> result = orderService.getOrdersByDateRange(start, end);

        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(orderRepository).findByOrderDateBetween(start, end);
    }

    // -----------------------------------------------------------------------
    // removeProductFromOrder
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("removeProductFromOrder throws when order not found")
    void removeProductFromOrder_orderNotFound_throws() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.removeProductFromOrder(99L, 1L));
    }

    @Test
    @DisplayName("removeProductFromOrder removes matching item and saves")
    void removeProductFromOrder_removesItemAndSaves() {
        testOrder.getOrderItems().add(testOrderItem); // productId = 100L
        when(orderRepository.findById(5L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.removeProductFromOrder(5L, 100L);

        assertTrue(testOrder.getOrderItems().isEmpty());
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("removeProductFromOrder keeps items with non-matching productId")
    void removeProductFromOrder_keepsNonMatchingItems() {
        testOrder.getOrderItems().add(testOrderItem); // productId = 100L
        when(orderRepository.findById(5L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.removeProductFromOrder(5L, 999L); // different product

        assertEquals(1, testOrder.getOrderItems().size());
        verify(orderRepository).save(testOrder);
    }

    // -----------------------------------------------------------------------
    // toDTO
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("toDTO maps all fields including customer and items")
    void toDTO_mapsAllFields() {
        testOrder.getOrderItems().add(testOrderItem);

        OrderDTO dto = orderService.toDTO(testOrder);

        assertEquals(5L, dto.getId());
        assertEquals(10L, dto.getCustomerId());
        assertEquals("Alice Smith", dto.getCustomerName());
        assertEquals(new BigDecimal("99.99"), dto.getTotalAmount());
        assertEquals("CONFIRMED", dto.getStatus());
        assertEquals(1, dto.getItems().size());
    }

    @Test
    @DisplayName("toDTO handles null customer gracefully")
    void toDTO_nullCustomer_handledGracefully() {
        testOrder.setCustomer(null);

        OrderDTO dto = orderService.toDTO(testOrder);

        assertNull(dto.getCustomerId());
        assertNull(dto.getCustomerName());
    }

    @Test
    @DisplayName("toDTO handles null status gracefully")
    void toDTO_nullStatus_handledGracefully() {
        testOrder.setStatus(null);

        OrderDTO dto = orderService.toDTO(testOrder);

        assertNull(dto.getStatus());
    }

    // -----------------------------------------------------------------------
    // toOrderItemDTO
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("toOrderItemDTO maps all fields")
    void toOrderItemDTO_mapsAllFields() {
        OrderItemDTO dto = orderService.toOrderItemDTO(testOrderItem);

        assertEquals(20L, dto.getId());
        assertEquals(100L, dto.getProductId());
        assertEquals("Blue Shirt", dto.getProductName());
        assertEquals(3, dto.getQuantity());
        assertEquals(new BigDecimal("33.33"), dto.getPrice());
    }

    // -----------------------------------------------------------------------
    // fromOrderItemDTO
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("fromOrderItemDTO maps id, quantity, price")
    void fromOrderItemDTO_mapsFields() {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(55L);
        dto.setQuantity(7);
        dto.setPrice(new BigDecimal("12.50"));

        OrderItem item = orderService.fromOrderItemDTO(dto);

        assertEquals(55L, item.getId());
        assertEquals(7, item.getQuantity());
        assertEquals(new BigDecimal("12.50"), item.getPrice());
    }

    // -----------------------------------------------------------------------
    // fromDTO
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("fromDTO with no customerId leaves customer null")
    void fromDTO_noCustomerId_customerNull() {
        OrderDTO dto = new OrderDTO();
        dto.setId(7L);
        dto.setStatus("PENDING");
        dto.setTotalAmount(new BigDecimal("50.00"));
        dto.setOrderDate(LocalDateTime.now());

        Order result = orderService.fromDTO(dto);

        assertEquals(7L, result.getId());
        assertNull(result.getCustomer());
    }

    @Test
    @DisplayName("fromDTO with valid customerId sets customer")
    void fromDTO_validCustomerId_setsCustomer() {
        OrderDTO dto = new OrderDTO();
        dto.setId(8L);
        dto.setCustomerId(10L);
        dto.setStatus("SHIPPED");
        dto.setTotalAmount(new BigDecimal("75.00"));
        dto.setOrderDate(LocalDateTime.now());

        when(customerService.getCustomerById(10L)).thenReturn(Optional.of(testCustomer));

        Order result = orderService.fromDTO(dto);

        assertEquals(testCustomer, result.getCustomer());
        assertEquals(OrderStatus.SHIPPED, result.getStatus());
    }

    @Test
    @DisplayName("fromDTO throws ResourceNotFoundException when customer not found")
    void fromDTO_customerNotFound_throws() {
        OrderDTO dto = new OrderDTO();
        dto.setCustomerId(999L);
        dto.setStatus("PENDING");
        dto.setTotalAmount(new BigDecimal("10.00"));
        dto.setOrderDate(LocalDateTime.now());

        when(customerService.getCustomerById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.fromDTO(dto));
    }

    @Test
    @DisplayName("fromDTO with items calls catalogClient and builds order items")
    void fromDTO_withItems_buildsOrderItems() {
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(100L);
        itemDTO.setQuantity(2);
        itemDTO.setPrice(new BigDecimal("20.00"));

        OrderDTO dto = new OrderDTO();
        dto.setStatus("PENDING");
        dto.setOrderDate(LocalDateTime.now());
        dto.setItems(List.of(itemDTO));

        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(100L);
        productDTO.setName("Red Hat");
        productDTO.setPrice(new BigDecimal("20.00"));

        when(catalogClient.getProductById(100L)).thenReturn(productDTO);

        Order result = orderService.fromDTO(dto);

        assertEquals(1, result.getOrderItems().size());
        assertEquals("Red Hat", result.getOrderItems().get(0).getProductName());
        // Total should be auto-calculated from items since dto.totalAmount is null
        assertNotNull(result.getTotalAmount());
    }

    @Test
    @DisplayName("fromDTO throws when product not found via catalogClient")
    void fromDTO_productNotFound_throws() {
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(777L);
        itemDTO.setQuantity(1);

        OrderDTO dto = new OrderDTO();
        dto.setStatus("PENDING");
        dto.setOrderDate(LocalDateTime.now());
        dto.setItems(List.of(itemDTO));

        when(catalogClient.getProductById(777L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> orderService.fromDTO(dto));
    }

    @Test
    @DisplayName("fromDTO preserves totalAmount when already set in DTO")
    void fromDTO_totalAmountAlreadySet_preserved() {
        OrderDTO dto = new OrderDTO();
        dto.setStatus("CONFIRMED");
        dto.setTotalAmount(new BigDecimal("999.99"));
        dto.setOrderDate(LocalDateTime.now());

        Order result = orderService.fromDTO(dto);

        assertEquals(new BigDecimal("999.99"), result.getTotalAmount());
    }

    // -----------------------------------------------------------------------
    // getAllOrderDTOs
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getAllOrderDTOs returns list of DTOs")
    void getAllOrderDTOs_returnsDtoList() {
        when(orderRepository.findAll()).thenReturn(List.of(testOrder));

        List<OrderDTO> result = orderService.getAllOrderDTOs();

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
    }

    @Test
    @DisplayName("getAllOrderDTOs with Pageable returns paged DTOs")
    void getAllOrderDTOs_pageable_returnsPagedDtos() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findAll(pageable)).thenReturn(page);

        Page<OrderDTO> result = orderService.getAllOrderDTOs(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(5L, result.getContent().get(0).getId());
    }

    // -----------------------------------------------------------------------
    // getOrderDTOById
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getOrderDTOById returns DTO when order exists")
    void getOrderDTOById_exists_returnsDto() {
        when(orderRepository.findById(5L)).thenReturn(Optional.of(testOrder));

        Optional<OrderDTO> result = orderService.getOrderDTOById(5L);

        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getId());
    }

    @Test
    @DisplayName("getOrderDTOById returns empty when order not found")
    void getOrderDTOById_notFound_returnsEmpty() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<OrderDTO> result = orderService.getOrderDTOById(99L);

        assertFalse(result.isPresent());
    }

    // -----------------------------------------------------------------------
    // getOrderDTOsByCustomerId
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getOrderDTOsByCustomerId returns DTOs filtered by customer")
    void getOrderDTOsByCustomerId_returnsDtoList() {
        when(orderRepository.findByCustomerId(10L)).thenReturn(List.of(testOrder));

        List<OrderDTO> result = orderService.getOrderDTOsByCustomerId(10L);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
    }

    @Test
    @DisplayName("getOrderDTOsByCustomerId with Pageable returns paged DTOs")
    void getOrderDTOsByCustomerId_pageable_returnsPagedDtos() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findByCustomerId(10L, pageable)).thenReturn(page);

        Page<OrderDTO> result = orderService.getOrderDTOsByCustomerId(10L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    // -----------------------------------------------------------------------
    // getOrderDTOsByDateRange
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getOrderDTOsByDateRange returns DTOs in date range")
    void getOrderDTOsByDateRange_returnsDtoList() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        when(orderRepository.findByOrderDateBetween(start, end)).thenReturn(List.of(testOrder));

        List<OrderDTO> result = orderService.getOrderDTOsByDateRange(start, end);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getOrderDTOsByDateRange with Pageable returns paged DTOs")
    void getOrderDTOsByDateRange_pageable_returnsPagedDtos() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findByOrderDateBetween(start, end, pageable)).thenReturn(page);

        Page<OrderDTO> result = orderService.getOrderDTOsByDateRange(start, end, pageable);

        assertEquals(1, result.getTotalElements());
    }
}
