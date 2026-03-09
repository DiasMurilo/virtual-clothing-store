package com.example.virtualclothingstore.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderEntity Tests")
class OrderEntityTest {

    // -----------------------------------------------------------------------
    // Customer entity
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Customer default constructor creates empty customer")
    void customer_defaultConstructor() {
        Customer c = new Customer();
        assertNull(c.getId());
        assertNull(c.getFirstName());
        assertNull(c.getEmail());
    }

    @Test
    @DisplayName("Customer parameterized constructor sets all fields")
    void customer_parameterizedConstructor() {
        Customer c = new Customer("Bob", "Jones", "bob@test.com", "123");
        assertEquals("Bob", c.getFirstName());
        assertEquals("Jones", c.getLastName());
        assertEquals("bob@test.com", c.getEmail());
        assertEquals("123", c.getPhone());
        assertNotNull(c.getCreatedAt());
    }

    @Test
    @DisplayName("Customer setters update fields")
    void customer_setters() {
        Customer c = new Customer();
        c.setId(1L);
        c.setFirstName("Jane");
        c.setLastName("Doe");
        c.setEmail("jane@test.com");
        c.setPhone("999");
        c.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
        List<Order> orders = new ArrayList<>();
        c.setOrders(orders);

        assertEquals(1L, c.getId());
        assertEquals("Jane", c.getFirstName());
        assertEquals("Doe", c.getLastName());
        assertEquals("jane@test.com", c.getEmail());
        assertEquals("999", c.getPhone());
        assertNotNull(c.getCreatedAt());
        assertTrue(c.getOrders().isEmpty());
    }

    // -----------------------------------------------------------------------
    // Order entity
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Order default constructor creates empty order with empty items list")
    void order_defaultConstructor() {
        Order o = new Order();
        assertNull(o.getId());
        assertNotNull(o.getOrderItems());
        assertTrue(o.getOrderItems().isEmpty());
    }

    @Test
    @DisplayName("Order parameterized constructor sets fields")
    void order_parameterizedConstructor() {
        Customer c = new Customer("Tim", "Lee", "tim@test.com", null);
        LocalDateTime dt = LocalDateTime.of(2024, 3, 15, 10, 0);
        BigDecimal total = new BigDecimal("250.00");

        Order o = new Order(c, dt, total, OrderStatus.SHIPPED);

        assertEquals(c, o.getCustomer());
        assertEquals(dt, o.getOrderDate());
        assertEquals(total, o.getTotalAmount());
        assertEquals(OrderStatus.SHIPPED, o.getStatus());
    }

    @Test
    @DisplayName("Order setters update all fields")
    void order_setters() {
        Order o = new Order();
        Customer c = new Customer();
        LocalDateTime dt = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("45.00");
        List<OrderItem> items = new ArrayList<>();

        o.setId(7L);
        o.setCustomer(c);
        o.setOrderDate(dt);
        o.setTotalAmount(amount);
        o.setStatus(OrderStatus.CANCELLED);
        o.setOrderItems(items);

        assertEquals(7L, o.getId());
        assertEquals(c, o.getCustomer());
        assertEquals(dt, o.getOrderDate());
        assertEquals(amount, o.getTotalAmount());
        assertEquals(OrderStatus.CANCELLED, o.getStatus());
        assertTrue(o.getOrderItems().isEmpty());
    }

    // -----------------------------------------------------------------------
    // OrderItem entity
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("OrderItem default constructor creates empty item")
    void orderItem_defaultConstructor() {
        OrderItem item = new OrderItem();
        assertNull(item.getId());
        assertNull(item.getProductId());
    }

    @Test
    @DisplayName("OrderItem parameterized constructor sets all fields")
    void orderItem_parameterizedConstructor() {
        Order order = new Order();
        OrderItem item = new OrderItem(order, 42L, "Black Jeans", 2, new BigDecimal("49.99"));

        assertEquals(order, item.getOrder());
        assertEquals(42L, item.getProductId());
        assertEquals("Black Jeans", item.getProductName());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("49.99"), item.getPrice());
    }

    @Test
    @DisplayName("OrderItem setters update all fields")
    void orderItem_setters() {
        OrderItem item = new OrderItem();
        Order order = new Order();

        item.setId(3L);
        item.setOrder(order);
        item.setProductId(99L);
        item.setProductName("Green Hat");
        item.setQuantity(5);
        item.setPrice(new BigDecimal("19.99"));

        assertEquals(3L, item.getId());
        assertEquals(order, item.getOrder());
        assertEquals(99L, item.getProductId());
        assertEquals("Green Hat", item.getProductName());
        assertEquals(5, item.getQuantity());
        assertEquals(new BigDecimal("19.99"), item.getPrice());
    }

    // -----------------------------------------------------------------------
    // OrderStatus enum
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("OrderStatus enum has all expected values")
    void orderStatus_enumValues() {
        OrderStatus[] values = OrderStatus.values();
        assertTrue(values.length >= 5);
        assertEquals(OrderStatus.PENDING, OrderStatus.valueOf("PENDING"));
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.valueOf("CONFIRMED"));
        assertEquals(OrderStatus.SHIPPED, OrderStatus.valueOf("SHIPPED"));
        assertEquals(OrderStatus.DELIVERED, OrderStatus.valueOf("DELIVERED"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.valueOf("CANCELLED"));
    }
}
