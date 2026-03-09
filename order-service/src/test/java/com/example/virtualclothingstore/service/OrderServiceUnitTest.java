package com.example.virtualclothingstore.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
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
 * Unit tests for OrderService using Mockito.
 * These tests demonstrate the base of the Test Pyramid - fast, isolated unit tests
 * that verify business logic without any Spring context or database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceUnitTest {

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
        // Setup common test data
        testCustomer = new Customer("John", "Doe", "john@example.com", "1234567890");
        testCustomer.setId(1L);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomer(testCustomer);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("100.00"));

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setProductId(1L);
        testOrderItem.setProductName("Test Product");
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(new BigDecimal("50.00"));
        testOrderItem.setOrder(testOrder);
    }

    @Test
    @DisplayName("getAllOrders should delegate to repository")
    void getAllOrders_delegatesToRepository() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(expectedOrders);

        // Act
        List<Order> actualOrders = orderService.getAllOrders();

        // Assert
        assertSame(expectedOrders, actualOrders);
        verify(orderRepository).findAll();
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("getOrderById should return order when found")
    void getOrderById_whenExists_returnsOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        Optional<Order> result = orderService.getOrderById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testOrder, result.get());
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("getOrderById should return empty when not found")
    void getOrderById_whenNotExists_returnsEmpty() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.getOrderById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(orderRepository).findById(99L);
    }

    @Test
    @DisplayName("getOrdersByCustomerId should filter by customer")
    void getOrdersByCustomerId_returnsFilteredOrders() {
        // Arrange
        Long customerId = 1L;
        List<Order> customerOrders = Arrays.asList(testOrder);
        when(orderRepository.findByCustomerId(customerId)).thenReturn(customerOrders);

        // Act
        List<Order> result = orderService.getOrdersByCustomerId(customerId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(orderRepository).findByCustomerId(customerId);
    }

    @Test
    @DisplayName("createOrder should throw exception when customer not found")
    void createOrder_whenCustomerNotFound_throwsException() {
        // Arrange
        Long customerId = 99L;
        List<OrderItem> items = new ArrayList<>();
        when(customerService.getCustomerById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(customerId, items);
        });
        assertEquals("Customer not found", exception.getMessage());
        verify(customerService).getCustomerById(customerId);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("createOrder should calculate total correctly")
    void createOrder_calculatesTotal_correctly() {
        // Arrange
        Long customerId = 1L;
        when(customerService.getCustomerById(customerId)).thenReturn(Optional.of(testCustomer));

        OrderItem item1 = new OrderItem();
        item1.setPrice(new BigDecimal("50.00"));
        item1.setQuantity(2);

        OrderItem item2 = new OrderItem();
        item2.setPrice(new BigDecimal("30.00"));
        item2.setQuantity(1);

        List<OrderItem> items = Arrays.asList(item1, item2);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        Order result = orderService.createOrder(customerId, items);

        // Assert
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        
        // Total should be: (50 * 2) + (30 * 1) = 130
        assertEquals(new BigDecimal("130.00"), savedOrder.getTotalAmount());
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(testCustomer, savedOrder.getCustomer());
        assertNotNull(savedOrder.getOrderDate());
    }

    @Test
    @DisplayName("addProductToOrder should throw exception when order not found")
    void addProductToOrder_whenOrderNotFound_throwsException() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.addProductToOrder(99L, 1L, 1);
        });
        assertEquals("Order not found", exception.getMessage());
        verifyNoInteractions(catalogClient);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    @DisplayName("addProductToOrder should throw exception when product not found")
    void addProductToOrder_whenProductNotFound_throwsException() {
        // Arrange
        testOrder.setOrderItems(new ArrayList<>());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(catalogClient.getProductById(99L)).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.addProductToOrder(1L, 99L, 1);
        });
        assertEquals("Product not found", exception.getMessage());
        verify(catalogClient).getProductById(99L);
        verifyNoInteractions(orderItemRepository);
    }

    @Test
    @DisplayName("addProductToOrder should add item and recalculate total")
    void addProductToOrder_addsItemAndRecalculatesTotal() {
        // Arrange
        testOrder.setOrderItems(new ArrayList<>());
        testOrder.setTotalAmount(BigDecimal.ZERO);
        
        ProductDTO productDto = new ProductDTO();
        productDto.setId(1L);
        productDto.setName("Product A");
        productDto.setPrice(new BigDecimal("25.00"));
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(catalogClient.getProductById(1L)).thenReturn(productDto);
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        orderService.addProductToOrder(1L, 1L, 3);

        // Assert
        verify(orderItemRepository).save(any(OrderItem.class));
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        
        Order updatedOrder = orderCaptor.getValue();
        // Total should be 25 * 3 = 75
        assertEquals(new BigDecimal("75.00"), updatedOrder.getTotalAmount());
    }

    @Test
    @DisplayName("deleteOrder should delegate to repository")
    void deleteOrder_delegatesToRepository() {
        // Arrange
        Long orderId = 1L;
        doNothing().when(orderRepository).deleteById(orderId);

        // Act
        orderService.deleteOrder(orderId);

        // Assert
        verify(orderRepository).deleteById(orderId);
    }

    @Test
    @DisplayName("saveOrder should delegate to repository")
    void saveOrder_delegatesToRepository() {
        // Arrange
        when(orderRepository.save(testOrder)).thenReturn(testOrder);

        // Act
        Order result = orderService.saveOrder(testOrder);

        // Assert
        assertEquals(testOrder, result);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("conversion methods produce correct DTOs")
    void conversion_methods_work() {
        // Arrange
        OrderItemDTO itemDto = new OrderItemDTO();
        itemDto.setId(5L);
        itemDto.setProductId(2L);
        itemDto.setProductName("X");
        itemDto.setPrice(new BigDecimal("9.99"));
        itemDto.setQuantity(3);

        OrderItem item = new OrderItem();
        item.setId(5L);
        item.setProductId(2L);
        item.setProductName("X");
        item.setPrice(new BigDecimal("9.99"));
        item.setQuantity(3);
        item.setOrder(testOrder);

        testOrder.setOrderItems(Arrays.asList(item));

        // Act
        OrderDTO dto = orderService.toDTO(testOrder);
        OrderItemDTO dtoItem = orderService.toOrderItemDTO(item);

        // Assert
        assertEquals(testOrder.getId(), dto.getId());
        assertEquals(testOrder.getCustomer().getId(), dto.getCustomerId());
        assertEquals(testOrder.getTotalAmount(), dto.getTotalAmount());
        assertEquals(1, dto.getItems().size());
        OrderItemDTO first = dto.getItems().get(0);
        assertEquals(item.getProductName(), first.getProductName());
        assertEquals(item.getQuantity(), first.getQuantity());
        assertEquals(item.getPrice(), first.getPrice());
    }

    @Test
    @DisplayName("fromDTO builds order with customer and items")
    void fromDTO_createsOrderCorrectly() {
        // Arrange
        OrderDTO dto = new OrderDTO();
        dto.setId(10L);
        dto.setCustomerId(testCustomer.getId());
        dto.setStatus(OrderStatus.PENDING.name());
        // leave total null so service computes it
        // dto.setTotalAmount(new BigDecimal("0"));

        OrderItemDTO iDto = new OrderItemDTO();
        iDto.setProductId(1L);
        iDto.setProductName("name");
        iDto.setPrice(new BigDecimal("5"));
        iDto.setQuantity(2);
        dto.setItems(Arrays.asList(iDto));

        when(customerService.getCustomerById(testCustomer.getId())).thenReturn(Optional.of(testCustomer));
        ProductDTO prod = new ProductDTO();
        prod.setId(1L);
        prod.setName("name");
        prod.setPrice(new BigDecimal("5"));
        when(catalogClient.getProductById(1L)).thenReturn(prod);

        // Act
        Order result = orderService.fromDTO(dto);

        // Assert
        assertEquals(dto.getCustomerId(), result.getCustomer().getId());
        assertEquals(new BigDecimal("10"), result.getTotalAmount());
        assertEquals(1, result.getOrderItems().size());
    }

    @Test
    @DisplayName("fromDTO throws when product missing")
    void fromDTO_productMissing_throws() {
        // Arrange
        OrderDTO dto = new OrderDTO();
        dto.setCustomerId(testCustomer.getId());
        OrderItemDTO iDto = new OrderItemDTO();
        iDto.setProductId(99L);
        dto.setItems(Arrays.asList(iDto));
        when(customerService.getCustomerById(testCustomer.getId())).thenReturn(Optional.of(testCustomer));
        when(catalogClient.getProductById(99L)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> orderService.fromDTO(dto));
    }

    @Test
    @DisplayName("DTO pagination wrappers delegate correctly")
    void pagination_wrappers() {
        // Arrange
        Page<Order> page = new org.springframework.data.domain.PageImpl<>(Arrays.asList(testOrder));
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(orderRepository.findByCustomerId(any(Long.class), any(Pageable.class))).thenReturn(page);
        when(orderRepository.findByOrderDateBetween(any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class))).thenReturn(page);

        // Act
        Page<OrderDTO> p1 = orderService.getAllOrderDTOs(Pageable.unpaged());
        Page<OrderDTO> p2 = orderService.getOrderDTOsByCustomerId(1L, Pageable.unpaged());
        Page<OrderDTO> p3 = orderService.getOrderDTOsByDateRange(LocalDateTime.now(), LocalDateTime.now(), Pageable.unpaged());

        // Assert
        assertEquals(1, p1.getTotalElements());
        assertEquals(1, p2.getTotalElements());
        assertEquals(1, p3.getTotalElements());
    }

    @Test
    @DisplayName("getAllOrderDTOs should map results")
    void getAllOrderDTOs_returnsMappedList() {
        List<Order> list = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(list);
        List<OrderDTO> dtos = orderService.getAllOrderDTOs();
        assertEquals(1, dtos.size());
        assertEquals(testOrder.getId(), dtos.get(0).getId());
    }

    @Test
    @DisplayName("getOrderDTOById should return DTO when found")
    void getOrderDTOById_returnsMappedOptional() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        Optional<OrderDTO> dto = orderService.getOrderDTOById(1L);
        assertTrue(dto.isPresent());
        assertEquals(testOrder.getId(), dto.get().getId());
    }
}
