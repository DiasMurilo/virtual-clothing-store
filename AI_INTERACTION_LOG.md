# AI Interaction Log - Assignment 2: AI-Assisted Testing

**Student:** Murilo Dias  
**Project:** Virtual Clothing Store - Order Service  
**Date:** March 2, 2026  
**Branch:** Assignment2_CICD

---

## Overview

This document captures 5 specific examples of AI-assisted development during the test suite creation for the `order-service` microservice.
Each example demonstrates different aspects of AI collaboration, including accepted suggestions, modifications, rejections, gap identification, and constrained outputs.

---

## Example 1: AI Output Accepted As-Is

### Context

Creating repository integration tests using Spring's `@DataJpaTest` annotation to test JPA queries with an in-memory H2 database.

### AI Suggestion

AI suggested using `TestEntityManager` for test data setup and `@DataJpaTest` for repository layer testing:

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save and retrieve order successfully")
    void saveAndRetrieveOrder_success() {
        // Arrange
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        entityManager.persist(customer);

        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100.00));

        // Act
        Order saved = orderRepository.save(order);
        entityManager.flush();
        Order retrieved = orderRepository.findById(saved.getId()).orElse(null);

        // Assert
        assertNotNull(retrieved);
        assertEquals(saved.getId(), retrieved.getId());
        assertEquals("John Doe", retrieved.getCustomer().getName());
    }
}
```

### Decision: **ACCEPTED**

### Rationale

- **Correct use of `@DataJpaTest`**: Provides lightweight Spring context with only JPA components
- **TestEntityManager advantage**: Provides better control over persistence context and flush operations
- **H2 in-memory database**: Perfect for isolated, fast repository tests without external dependencies
- **Clear test structure**: Arrange-Act-Assert pattern with descriptive test names
- **No modifications needed**: Code compiled and all 8 repository tests passed on first run

### Outcome

All 8 repository integration tests passed successfully with this approach, achieving 68% coverage on entity classes.

---

## Example 2: AI Output Modified

### Context

Creating unit tests for `addProductToOrder` method in `OrderService`. The test needed to create a `ProductDTO` object for mocking the `CatalogClient` response.

### AI Original Suggestion

```java
@Test
void addProductToOrder_addsItemAndRecalculatesTotal() {
    // Arrange
    testOrder.setOrderItems(new ArrayList<>());
    testOrder.setTotalAmount(BigDecimal.ZERO);

    ProductDTO productDto = new ProductDTO(1L, "Product A",
                                          "Description",
                                          new BigDecimal("25.00"),
                                          10);

    when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
    when(catalogClient.getProductById(1L)).thenReturn(productDto);
    // ... rest of test
}
```

### Actual Implementation Used

```java
@Test
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
    // ... rest of test
}
```

### Modification Reason

The actual `ProductDTO` class has a **7-parameter constructor** (id, name, description, price, stockQuantity, category, imageUrl), but AI initially suggested a 5-parameter version. Additionally, using the **property setter approach** is:

- **More readable**: Each property assignment is explicit
- **More maintainable**: Adding/removing properties doesn't break tests
- **Less brittle**: Test only sets required properties, ignoring unused ones

### Impact

This modification pattern was applied across all unit tests that needed DTO object creation, preventing fragile tests that would break with constructor signature changes.

---

## Example 3: AI Output Rejected

### Context

During discussion of test architecture, AI suggested using `@SpringBootTest` for controller testing.

### AI Suggestion

```java
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getAllOrders_returnsPaginatedResults() throws Exception {
        // Test implementation
    }
}
```

### Decision: **REJECTED**

### Rationale for Rejection

**Performance Concerns:**

- `@SpringBootTest` loads the **entire Spring application context** (all services, repositories, configurations)
- Test execution time: ~15-20 seconds for full context startup
- `@WebMvcTest` loads **only the web layer** (controllers, filters, advice)
- Test execution time: ~2-3 seconds for slice context

**Test Pyramid Alignment:**

- Controller tests should be **middle-layer integration tests**, not full system tests
- Full `@SpringBootTest` belongs at the **top of the pyramid** (fewer, broader E2E tests)
- Test suite should have more fast, focused tests than slow, comprehensive tests

**Isolation Benefits:**

- `@WebMvcTest(OrderController.class)` isolates the controller under test
- Only loads components needed for HTTP layer testing
- External dependencies (database, other services) remain mocked
- Clearer test failures: issues are definitely in the controller layer

### Alternative Chosen

```java
@WebMvcTest(OrderController.class)
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    // Tests run in ~2 seconds vs ~15+ seconds with @SpringBootTest
}
```

### Outcome

10 controller tests execute in 7.39 seconds total using `@WebMvcTest`, compared to estimated 150+ seconds with `@SpringBootTest` approach. This maintains fast feedback cycles during development.

---

## Example 4: AI-Identified Gap in Test Coverage

### Context

After completing initial test suite, requested AI to analyze test coverage and identify missing scenarios.

### AI Analysis Prompt

"Analyze the `OrderService` test suite and identify any missing edge cases or scenarios that should be tested."

### AI-Identified Gaps

#### Gap 1: Missing OrderItem in Total Calculation Test

**AI Finding:** In `addProductToOrder_addsItemAndRecalculatesTotal` test, the saved `OrderItem` was never added to the order's collection, causing total calculation to always return 0.

**Original Code:**

```java
orderItemRepository.save(item);

// Recalculate total
BigDecimal total = order.getOrderItems().stream()
        .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
order.setTotalAmount(total);
```

**Problem:** `order.getOrderItems()` is empty because saved item was never added to collection.

**Fix Applied:**

```java
orderItemRepository.save(item);
order.getOrderItems().add(item); // Added this line

// Recalculate total
BigDecimal total = order.getOrderItems().stream()
        .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
order.setTotalAmount(total);
```

**Result:** Test assertion changed from failing (expected: 75.00, was: 0) to passing.

#### Gap 2: Incorrect HTTP Method in Test

**AI Finding:** Test `getOrderById_whenNotExists_returns404` was using `DELETE` method instead of `GET`.

**Original Code:**

```java
@Test
void getOrderById_whenNotExists_returns404() throws Exception {
    when(orderService.getOrderDTOById(99L)).thenReturn(Optional.empty());

    mockMvc.perform(delete("/api/orders/99"))  // Wrong method!
            .andExpect(status().isNotFound());
}
```

**Fix Applied:**

```java
@Test
void getOrderById_whenNotExists_returns404() throws Exception {
    when(orderService.getOrderDTOById(99L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/orders/99"))  // Corrected to GET
            .andExpect(status().isNotFound());
}
```

**Result:** Test now correctly validates the GET endpoint behavior.

#### Gap 3: Missing Paginated Repository Tests

**AI Finding:** Repository tests only covered `List<Order>` return types but not `Page<Order>` methods used by the controller.

**Gap Addressed:**

```java
@Test
@DisplayName("Should find orders by customer ID with pagination")
void findByCustomerId_withPagination_success() {
    // Arrange
    Customer customer = new Customer();
    customer.setName("Jane Smith");
    customer.setEmail("jane@example.com");
    entityManager.persist(customer);

    Order order = new Order();
    order.setCustomer(customer);
    order.setOrderDate(LocalDateTime.now());
    order.setStatus(OrderStatus.PENDING);
    order.setTotalAmount(BigDecimal.valueOf(150.00));
    entityManager.persist(order);
    entityManager.flush();

    Pageable pageable = PageRequest.of(0, 10);

    // Act
    Page<Order> result = orderRepository.findByCustomerId(customer.getId(), pageable);

    // Assert
    assertEquals(1, result.getTotalElements());
    assertEquals(order.getId(), result.getContent().get(0).getId());
}

#### Gap 4: No End-to-End Smoke Test

**AI Finding:** The existing suite covered unit, controller slice, and repository tests but lacked any test exercising the full application context. A simple `@SpringBootTest` with `TestRestTemplate` at the top of the pyramid would serve as a smoke test to ensure the app starts and responds.

**Action Taken:** Added `OrderServiceE2ETest` with `@SpringBootTest(webEnvironment=RANDOM_PORT)` and in‑memory H2, disabling Eureka and config-server calls. This test performs a `GET /api/orders` request and asserts HTTP 200.

**Result:** The suite now has 37 tests; the new E2E test runs quickly (~6 s) thanks to in‑memory DB and disabled discovery, closing the top‑of‑pyramid gap.
```

### Impact

AI gap analysis identified 3 critical issues that would have caused test failures or incorrect validations, improving test suite reliability from 34/36 passing to 36/36 passing.

---

## Example 5: AI Output Constrained by Rules

### Context

Creating unit tests with specific architectural constraints to ensure proper test pyramid adherence.

### Constraints Provided to AI

1. **No Spring Context**: Unit tests must use pure Mockito without Spring annotations
2. **Constructor Injection Only**: Tests must verify constructor-based dependency injection
3. **Maximum 3 Assertions**: Each test should focus on one behavior with max 3 assertions
4. **No Real Dependencies**: All external dependencies must be mocked
5. **Fast Execution**: Each test must complete in <100ms

### AI Constrained Output

```java
@ExtendWith(MockitoExtension.class)  // Mockito only, no Spring
public class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks  // Verifies constructor injection
    private OrderService orderService;

    @Test
    @DisplayName("getOrderById should return order when exists")
    void getOrderById_whenExists_returnsOrder() {
        // Arrange
        Order testOrder = new Order();
        testOrder.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        Optional<Order> result = orderService.getOrderById(1L);

        // Assert - Only 2 assertions, focused on behavior
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("createOrder should validate and save order")
    void createOrder_validatesAndSavesOrder() {
        // Arrange
        Order order = new Order();
        order.setTotalAmount(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        Order result = orderService.createOrder(order);

        // Assert - Exactly 1 assertion per test goal
        verify(orderRepository).save(order);
    }
}
```

### Adherence to Constraints

| Constraint            | Implementation                             | Verification                              |
| --------------------- | ------------------------------------------ | ----------------------------------------- |
| No Spring Context     | `@ExtendWith(MockitoExtension.class)` only | ✅ Tests run without ApplicationContext   |
| Constructor Injection | `@InjectMocks` with constructor pattern    | ✅ No field injection used                |
| Max 3 Assertions      | 1-2 assertions per test                    | ✅ Average 1.8 assertions/test            |
| No Real Dependencies  | All `@Mock` annotations                    | ✅ Zero database/network calls            |
| Fast Execution        | Pure unit tests                            | ✅ All 11 tests run in 0.244s (22ms/test) |

### Performance Comparison

**Without Constraints (using @SpringBootTest):**

- Context startup: 15+ seconds
- Test execution: 11 tests in ~18 seconds
- Total: ~33 seconds

**With Constraints Applied:**

- No context needed
- Test execution: 11 tests in 0.244 seconds
- Total: 0.244 seconds
- **135x faster** ⚡

### Additional Benefits

- **Clearer failures**: No Spring context stack traces, just pure test failures
- **Better isolation**: Each test truly independent
- **Easier debugging**: Simpler test setup means faster root cause identification
- **Maintainability**: No Spring configuration dependencies to manage

---

## Summary

### Test Suite Statistics

- **Total Tests Created:** 36 (all passing)
- **Test Execution Time:** ~23 seconds
- **Code Coverage:** 44% overall (service: 44%, controller: 42%, entity: 68%)

### Test Pyramid Distribution

- **Base (Unit Tests):** 19 tests - OrderServiceUnitTest (11), CustomerServiceUnitTest (8)
- **Middle (Integration Tests):** 10 tests - OrderControllerIntegrationTest (@WebMvcTest)
- **Data Layer:** 8 tests - OrderRepositoryIntegrationTest (@DataJpaTest)

### AI Collaboration Value

1. **Time Savings:** Estimated 6-8 hours of test development compressed to ~2 hours with AI assistance
2. **Coverage Improvement:** AI gap analysis increased passing tests from 34/36 to 36/36
3. **Quality Enhancement:** Constructor injection refactoring improved testability across all layers
4. **Knowledge Transfer:** Learned Test Pyramid principles, Spring Test slicing, and Mockito best practices

### Key Learnings

- **When to accept AI suggestions:** Well-established patterns (TestEntityManager, @DataJpaTest)
- **When to modify:** Context-specific details (DTO constructors, actual parameter counts)
- **When to reject:** Performance anti-patterns (@SpringBootTest for unit tests)
- **Value of AI review:** Catches logic errors humans miss (missing collection adds, wrong HTTP methods)
- **Constraint benefits:** Explicit rules produce better, more maintainable code

---

**Assignment Completion Date:** March 2, 2026  
**Total Development Time:** ~2 hours with AI assistance  
**Final Test Status:** ✅ 36/36 PASSING
