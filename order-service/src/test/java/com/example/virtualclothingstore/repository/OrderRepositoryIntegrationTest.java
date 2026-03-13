package com.example.virtualclothingstore.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.virtualclothingstore.entity.Customer;
import com.example.virtualclothingstore.entity.Order;
import com.example.virtualclothingstore.entity.OrderStatus;

@DataJpaTest
@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer testCustomer;
    private Order testOrder1;
    private Order testOrder2;

    @BeforeEach
    void setUp() {
        // Create and persist test customer
        testCustomer = new Customer("Alice", "Wonder", "alice@example.com", "1112223333");
        entityManager.persist(testCustomer);

        // Create test orders
        testOrder1 = new Order();
        testOrder1.setCustomer(testCustomer);
        testOrder1.setOrderDate(LocalDateTime.of(2026, 3, 1, 10, 0));
        testOrder1.setStatus(OrderStatus.PENDING);
        testOrder1.setTotalAmount(new BigDecimal("150.00"));

        testOrder2 = new Order();
        testOrder2.setCustomer(testCustomer);
        testOrder2.setOrderDate(LocalDateTime.of(2026, 3, 5, 14, 30));
        testOrder2.setStatus(OrderStatus.DELIVERED);
        testOrder2.setTotalAmount(new BigDecimal("250.00"));

        entityManager.persist(testOrder1);
        entityManager.persist(testOrder2);
        entityManager.flush();
    }

    @Test
    @DisplayName("should save and retrieve order")
    void saveAndRetrieve_persistsOrder() {
        // Arrange
        Order newOrder = new Order();
        newOrder.setCustomer(testCustomer);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus(OrderStatus.PENDING);
        newOrder.setTotalAmount(new BigDecimal("99.99"));

        // Act
        Order saved = orderRepository.save(newOrder);
        Optional<Order> retrieved = orderRepository.findById(saved.getId());

        // Assert
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(retrieved.get().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("findByCustomerId should return customer's orders")
    void findByCustomerId_returnsCustomerOrders() {
        // Act
        List<Order> orders = orderRepository.findByCustomerId(testCustomer.getId());

        // Assert
        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o -> o.getCustomer().getId().equals(testCustomer.getId()));
    }

    @Test
    @DisplayName("findByCustomerId with pagination should return paged results")
    void findByCustomerId_withPagination_returnsPagedOrders() {
        // Arrange
        PageRequest pageRequest = PageRequest.of(0, 1);

        // Act
        Page<Order> page = orderRepository.findByCustomerId(testCustomer.getId(), pageRequest);

        // Assert
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByOrderDateBetween should filter by date range")
    void findByOrderDateBetween_filtersCorrectly() {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 3, 23, 59);

        // Act
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderDate()).isEqualTo(testOrder1.getOrderDate());
    }

    @Test
    @DisplayName("findByOrderDateAfter should filter orders after date")
    void findByOrderDateAfter_filtersCorrectly() {
        // Arrange
        LocalDateTime cutoff = LocalDateTime.of(2026, 3, 3, 0, 0);

        // Act
        List<Order> orders = orderRepository.findByOrderDateAfter(cutoff);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(testOrder2.getId());
    }

    @Test
    @DisplayName("findByOrderDateBefore should filter orders before date")
    void findByOrderDateBefore_filtersCorrectly() {
        // Arrange
        LocalDateTime cutoff = LocalDateTime.of(2026, 3, 3, 0, 0);

        // Act
        List<Order> orders = orderRepository.findByOrderDateBefore(cutoff);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(testOrder1.getId());
    }

    @Test
    @DisplayName("delete should remove order from database")
    void delete_removesOrder() {
        // Arrange
        Long orderId = testOrder1.getId();

        // Act
        orderRepository.deleteById(orderId);
        Optional<Order> deleted = orderRepository.findById(orderId);

        // Assert
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all orders")
    void findAll_returnsAllOrders() {
        // Act
        List<Order> allOrders = orderRepository.findAll();

        // Assert
        assertThat(allOrders).hasSizeGreaterThanOrEqualTo(2);
    }
}
