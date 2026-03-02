package com.example.virtualclothingstore.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.virtualclothingstore.entity.Order;
import com.example.virtualclothingstore.entity.OrderStatus;
import com.example.virtualclothingstore.entity.Customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void saveAndFindByCustomerId() {
        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.TEN);
        Customer cust = new Customer();
        cust.setId(1L);
        order.setCustomer(cust);
        orderRepository.save(order);

        List<Order> found = orderRepository.findByCustomerId(1L);
        assertThat(found).hasSize(1);
    }
}
