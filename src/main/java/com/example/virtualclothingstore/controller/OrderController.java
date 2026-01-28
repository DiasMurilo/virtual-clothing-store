package com.example.virtualclothingstore.controller;

import com.example.virtualclothingstore.dto.OrderDTO;
import com.example.virtualclothingstore.dto.OrderItemDTO;
import com.example.virtualclothingstore.entity.Order;
import com.example.virtualclothingstore.entity.OrderItem;
import com.example.virtualclothingstore.exception.BadRequestException;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Page<OrderDTO> getAllOrders(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (startDate != null && endDate != null) {
            return orderService.getOrderDTOsByDateRange(startDate, endDate, pageable);
        }
        return orderService.getAllOrderDTOs(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO orderDTO = orderService.getOrderDTOById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return ResponseEntity.ok(orderDTO);
    }

    @GetMapping("/customer/{customerId}")
    public Page<OrderDTO> getOrdersByCustomer(@PathVariable Long customerId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderService.getOrderDTOsByCustomerId(customerId, pageable);
    }

    @PostMapping
    public OrderDTO createOrder(@RequestParam Long customerId, @RequestBody List<OrderItem> items) {
        try {
            Order saved = orderService.createOrder(customerId, items);
            return orderService.toDTO(saved);
        } catch (RuntimeException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @PostMapping("/{orderId}/products")
    public ResponseEntity<Void> addProductToOrder(@PathVariable Long orderId,
                                                  @RequestParam Long productId,
                                                  @RequestParam Integer quantity) {
        try {
            orderService.addProductToOrder(orderId, productId, quantity);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @DeleteMapping("/{orderId}/products/{productId}")
    public ResponseEntity<Void> removeProductFromOrder(@PathVariable Long orderId, @PathVariable Long productId) {
        try {
            orderService.removeProductFromOrder(orderId, productId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @RequestBody OrderDTO orderDTO) {
        if (!orderService.getOrderById(id).isPresent()) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        Order order = orderService.fromDTO(orderDTO);
        order.setId(id);
        Order saved = orderService.saveOrder(order);
        return ResponseEntity.ok(orderService.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        if (!orderService.getOrderById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}