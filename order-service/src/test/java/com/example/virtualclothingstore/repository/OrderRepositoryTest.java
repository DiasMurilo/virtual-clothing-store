package com.example.virtualclothingstore.repository;

import com.example.virtualclothingstore.entity.Customer;
import com.example.virtualclothingstore.entity.Order;
import com.example.virtualclothingstore.entity.OrderItem;
import com.example.virtualclothingstore.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void saveOrder_andFindByCustomerId() {
        Customer cust = new Customer();
        cust.setFirstName("Jane");
        cust.setLastName("Smith");
        cust.setEmail("jane@example.com");
        cust.setCreatedAt(LocalDateTime.now());
        cust = customerRepository.save(cust);

        Order order = new Order();
        order.setCustomer(cust);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        order = orderRepository.save(order);
        orderRepository.flush();
        customerRepository.flush();
        assertThat(cust.getId()).isNotNull();
        assertThat(order.getId()).isNotNull();

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(1L);
        item.setProductName("Sock");
        item.setQuantity(1);
        item.setPrice(BigDecimal.valueOf(5));
        orderItemRepository.save(item);

        List<Order> list = orderRepository.findByCustomerId(cust.getId());
        System.out.println("DEBUG orders count: " + list.size());
        assertThat(list).hasSize(1);
        // item list may be lazily loaded; at least ensure it's not null
        assertThat(list.get(0).getOrderItems()).isNotNull();
    }
}