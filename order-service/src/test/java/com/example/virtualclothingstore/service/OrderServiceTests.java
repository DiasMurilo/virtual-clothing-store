package com.example.virtualclothingstore.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import com.example.virtualclothingstore.entity.Order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getAllOrders_delegatesToRepository() {
        List<Order> sample = List.of(new Order());
        when(orderRepository.findAll()).thenReturn(sample);

        List<Order> result = orderService.getAllOrders();

        assertSame(sample, result);
        verify(orderRepository).findAll();
    }
}
