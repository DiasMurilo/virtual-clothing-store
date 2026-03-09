package com.example.virtualclothingstore.service;

import com.example.virtualclothingstore.dto.OrderDTO;
import com.example.virtualclothingstore.dto.OrderItemDTO;
import com.example.virtualclothingstore.entity.*;
import com.example.virtualclothingstore.dto.ProductDTO;
import com.example.virtualclothingstore.service.CatalogClient;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.repository.OrderRepository;
import com.example.virtualclothingstore.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CatalogClient catalogClient;  // remote product service

    // Feign client has its own fallback; no local annotation required

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    public Order createOrder(Long customerId, List<OrderItem> items) {
        Optional<Customer> customerOpt = customerService.getCustomerById(customerId);
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Customer not found");
        }
        Customer customer = customerOpt.get();

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(items);

        // Calculate total
        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        // Save order first
        Order savedOrder = orderRepository.save(order);

        // Set order in items and save
        for (OrderItem item : items) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        return savedOrder;
    }

    public void addProductToOrder(Long orderId, Long productId, Integer quantity) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        Order order = orderOpt.get();
        ProductDTO productDto = catalogClient.getProductById(productId);
        if (productDto == null) {
            throw new RuntimeException("Product not found");
        }
        Product product = dtoToProduct(productDto);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setQuantity(quantity);
        item.setPrice(product.getPrice());

        orderItemRepository.save(item);

        // Recalculate total
        BigDecimal total = order.getOrderItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        orderRepository.save(order);
    }

    public void removeProductFromOrder(Long orderId, Long productId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        Order order = orderOpt.get();

        order.getOrderItems().removeIf(item -> item.getProductId() != null && item.getProductId().equals(productId));
        orderRepository.save(order);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    // DTO conversion methods
    public OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
        dto.setCustomerName(order.getCustomer() != null ? order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() : null);
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setItems(order.getOrderItems().stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    public OrderItemDTO toOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }

    private Product dtoToProduct(ProductDTO dto) {
        if (dto == null) return null;
        return new Product(dto.getId(), dto.getName(), dto.getPrice());
    }

    public Order fromDTO(OrderDTO dto) {
        Order order = new Order();
        order.setId(dto.getId());
        order.setOrderDate(dto.getOrderDate());
        if (dto.getStatus() != null) {
            order.setStatus(OrderStatus.valueOf(dto.getStatus()));
        }
        order.setTotalAmount(dto.getTotalAmount());

        // Set customer
        if (dto.getCustomerId() != null) {
            Customer customer = customerService.getCustomerById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            order.setCustomer(customer);
        }

        // Set order items
        if (dto.getItems() != null) {
            for (OrderItemDTO itemDTO : dto.getItems()) {
                ProductDTO productDto = catalogClient.getProductById(itemDTO.getProductId());
                if (productDto == null) {
                    throw new ResourceNotFoundException("Product not found");
                }
                Product product = dtoToProduct(productDto);
                OrderItem item = new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(product.getPrice());
                item.setOrder(order);
                order.getOrderItems().add(item);
            }
        }

        // Calculate total if not set
        if (order.getTotalAmount() == null) {
            BigDecimal total = order.getOrderItems().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(total);
        }

        return order;
    }

    public OrderItem fromOrderItemDTO(OrderItemDTO dto) {
        OrderItem item = new OrderItem();
        item.setId(dto.getId());
        item.setQuantity(dto.getQuantity());
        item.setPrice(dto.getPrice());
        // Note: Order and Product will be set by the controller/service layer
        return item;
    }

    public List<OrderDTO> getAllOrderDTOs() {
        return getAllOrders().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<OrderDTO> getAllOrderDTOs(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::toDTO);
    }

    public Optional<OrderDTO> getOrderDTOById(Long id) {
        return getOrderById(id).map(this::toDTO);
    }

    public List<OrderDTO> getOrderDTOsByCustomerId(Long customerId) {
        return getOrdersByCustomerId(customerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<OrderDTO> getOrderDTOsByCustomerId(Long customerId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByCustomerId(customerId, pageable);
        return orders.map(this::toDTO);
    }

    public List<OrderDTO> getOrderDTOsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return getOrdersByDateRange(startDate, endDate).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<OrderDTO> getOrderDTOsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate, pageable);
        return orders.map(this::toDTO);
    }

}