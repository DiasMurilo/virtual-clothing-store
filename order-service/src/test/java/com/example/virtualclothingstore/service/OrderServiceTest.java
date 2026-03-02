package com.example.virtualclothingstore.service;

import com.example.virtualclothingstore.dto.OrderDTO;
import com.example.virtualclothingstore.dto.OrderItemDTO;
import com.example.virtualclothingstore.dto.ProductDTO;
import com.example.virtualclothingstore.entity.Customer;
import com.example.virtualclothingstore.entity.Order;
import com.example.virtualclothingstore.entity.OrderItem;
import com.example.virtualclothingstore.entity.OrderStatus;
import com.example.virtualclothingstore.service.CatalogClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    private OrderService orderService;

    // simple in-memory stub implementations
    static class InMemoryOrderRepo implements com.example.virtualclothingstore.repository.OrderRepository {
        private Map<Long, Order> store = new HashMap<>();
        @Override public Optional<Order> findById(Long id) { return Optional.ofNullable(store.get(id)); }
        @Override public Order save(Order o) { if (o.getId()==null) o.setId((long) (store.size()+1)); store.put(o.getId(), o); return o; }
        @Override public List<Order> findAll() { return new ArrayList<>(store.values()); }
        // unneeded methods can throw UnsupportedOperationException
        @Override public List<Order> findByCustomerId(Long customerId) { throw new UnsupportedOperationException(); }
        @Override public List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate) { throw new UnsupportedOperationException(); }
        @Override public <S extends Order> List<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException(); }
        @Override public void deleteById(Long aLong) { throw new UnsupportedOperationException(); }
        @Override public void delete(Order entity) { throw new UnsupportedOperationException(); }
        @Override public long count() { return store.size(); }
        @Override public boolean existsById(Long aLong) { return store.containsKey(aLong); }
    }

    static class InMemoryItemRepo implements com.example.virtualclothingstore.repository.OrderItemRepository {
        private List<OrderItem> items = new ArrayList<>();
        @Override public OrderItem save(OrderItem i) { items.add(i); return i; }
        @Override public <S extends OrderItem> List<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException(); }
        @Override public Optional<OrderItem> findById(Long aLong) { return Optional.empty(); }
        @Override public boolean existsById(Long aLong) { return false; }
        @Override public List<OrderItem> findAll() { return items; }
        @Override public void deleteById(Long aLong) { throw new UnsupportedOperationException(); }
        @Override public void delete(OrderItem entity) { throw new UnsupportedOperationException(); }
        @Override public long count() { return items.size(); }
    }

    static class StubCustomerService extends CustomerService {
        private Optional<Customer> result;
        public StubCustomerService(Optional<Customer> r) { this.result = r; }
        @Override public Optional<Customer> getCustomerById(Long id) { return result; }
    }

    static class StubCatalogClient implements CatalogClient {
        private ProductDTO product;
        public StubCatalogClient(ProductDTO p) { this.product = p; }
        @Override public ProductDTO getProductById(Long id) { return product; }
    }

    private Customer sampleCustomer;
    private OrderItem sampleItem;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer();
        sampleCustomer.setId(1L);
        sampleCustomer.setFirstName("John");
        sampleCustomer.setLastName("Doe");

        sampleItem = new OrderItem();
        sampleItem.setId(1L);
        sampleItem.setProductId(123L);
        sampleItem.setProductName("T-shirt");
        sampleItem.setQuantity(2);
        sampleItem.setPrice(BigDecimal.valueOf(19.99));

        orderService = new OrderService();
        try {
            java.lang.reflect.Field f;
            f = OrderService.class.getDeclaredField("orderRepository");
            f.setAccessible(true);
            f.set(orderService, new InMemoryOrderRepo());
            f = OrderService.class.getDeclaredField("orderItemRepository");
            f.setAccessible(true);
            f.set(orderService, new InMemoryItemRepo());
            f = OrderService.class.getDeclaredField("customerService");
            f.setAccessible(true);
            f.set(orderService, new StubCustomerService(Optional.of(sampleCustomer)));
            f = OrderService.class.getDeclaredField("catalogClient");
            f.setAccessible(true);
            f.set(orderService, new StubCatalogClient(null));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void toDto_shouldConvertOrderProperly() {
        Order order = new Order();
        order.setId(10L);
        order.setCustomer(sampleCustomer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(39.98));
        order.setOrderItems(List.of(sampleItem));

        OrderDTO dto = orderService.toDTO(order);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getCustomerName()).contains("John");
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getTotalAmount()).isEqualByComparingTo("39.98");
    }

    @Test
    void fromDto_shouldBuildOrderAndFetchCustomerAndProducts() {
        OrderDTO dto = new OrderDTO();
        dto.setId(20L);
        dto.setCustomerId(1L);
        dto.setStatus(OrderStatus.SHIPPED.name());
        dto.setOrderDate(LocalDateTime.now());

        OrderItemDTO itemDto = new OrderItemDTO();
        itemDto.setProductId(123L);
        itemDto.setQuantity(1);
        dto.setItems(List.of(itemDto));

        ProductDTO productDto = new ProductDTO();
        productDto.setId(123L);
        productDto.setName("T-shirt");
        productDto.setPrice(BigDecimal.valueOf(19.99));
        try {
            java.lang.reflect.Field f = OrderService.class.getDeclaredField("catalogClient");
            f.setAccessible(true);
            f.set(orderService, new StubCatalogClient(productDto));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        Order order = orderService.fromDTO(dto);

        assertThat(order).isNotNull();
        assertThat(order.getCustomer()).isEqualTo(sampleCustomer);
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("19.99");
    }

    // additional tests could exercise business logic using in-memory stubs
}
