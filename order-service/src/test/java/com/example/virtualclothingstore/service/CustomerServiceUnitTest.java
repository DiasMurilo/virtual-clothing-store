package com.example.virtualclothingstore.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.virtualclothingstore.dto.CustomerDTO;
import com.example.virtualclothingstore.entity.Customer;
import com.example.virtualclothingstore.repository.CustomerRepository;

/**
 * Unit tests for CustomerService.
 * Demonstrates proper use of mocks and constructor injection.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceUnitTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer("Jane", "Smith", "jane@example.com", "9876543210");
        testCustomer.setId(1L);
        testCustomer.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("getAllCustomers should return all customers from repository")
    void getAllCustomers_returnsAllCustomers() {
        // Arrange
        List<Customer> expectedCustomers = Arrays.asList(testCustomer);
        when(customerRepository.findAll()).thenReturn(expectedCustomers);

        // Act
        List<Customer> result = customerService.getAllCustomers();

        // Assert
        assertEquals(expectedCustomers, result);
        verify(customerRepository).findAll();
    }

    @Test
    @DisplayName("getCustomerById should return customer when exists")
    void getCustomerById_whenExists_returnsCustomer() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // Act
        Optional<Customer> result = customerService.getCustomerById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testCustomer, result.get());
        verify(customerRepository).findById(1L);
    }

    @Test
    @DisplayName("getCustomerById should return empty when not found")
    void getCustomerById_whenNotExists_returnsEmpty() {
        // Arrange
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Customer> result = customerService.getCustomerById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(customerRepository).findById(99L);
    }

    @Test
    @DisplayName("saveCustomer should persist and return customer")
    void saveCustomer_persistsCustomer() {
        // Arrange
        when(customerRepository.save(testCustomer)).thenReturn(testCustomer);

        // Act
        Customer result = customerService.saveCustomer(testCustomer);

        // Assert
        assertEquals(testCustomer, result);
        verify(customerRepository).save(testCustomer);
    }

    @Test
    @DisplayName("deleteCustomer should delegate to repository")
    void deleteCustomer_deletesCustomer() {
        // Arrange
        doNothing().when(customerRepository).deleteById(1L);

        // Act
        customerService.deleteCustomer(1L);

        // Assert
        verify(customerRepository).deleteById(1L);
    }

    @Test
    @DisplayName("toDTO should correctly map customer to DTO")
    void toDTO_mapsCorrectly() {
        // Act
        CustomerDTO result = customerService.toDTO(testCustomer);

        // Assert
        assertNotNull(result);
        assertEquals(testCustomer.getId(), result.getId());
        assertEquals(testCustomer.getFirstName(), result.getFirstName());
        assertEquals(testCustomer.getLastName(), result.getLastName());
        assertEquals(testCustomer.getEmail(), result.getEmail());
        assertEquals(testCustomer.getPhone(), result.getPhone());
    }

    @Test
    @DisplayName("fromDTO should correctly map DTO to customer entity")
    void fromDTO_mapsCorrectly() {
        // Arrange
        CustomerDTO dto = new CustomerDTO(2L, "Alice", "Johnson", "alice@example.com", "5555555555", LocalDateTime.now());

        // Act
        Customer result = customerService.fromDTO(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getFirstName(), result.getFirstName());
        assertEquals(dto.getLastName(), result.getLastName());
        assertEquals(dto.getEmail(), result.getEmail());
        assertEquals(dto.getPhone(), result.getPhone());
    }

    @Test
    @DisplayName("getAllCustomerDTOs should map all customers to DTOs")
    void getAllCustomerDTOs_mapsAllCustomers() {
        // Arrange
        List<Customer> customers = Arrays.asList(testCustomer);
        when(customerRepository.findAll()).thenReturn(customers);

        // Act
        List<CustomerDTO> result = customerService.getAllCustomerDTOs();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testCustomer.getId(), result.get(0).getId());
        verify(customerRepository).findAll();
    }

    @Test
    @DisplayName("getCustomerDTOById returns DTO when customer exists")
    void getCustomerDTOById_whenExists_returnsDto() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        Optional<CustomerDTO> result = customerService.getCustomerDTOById(1L);

        assertTrue(result.isPresent());
        assertEquals(testCustomer.getId(), result.get().getId());
        assertEquals(testCustomer.getEmail(), result.get().getEmail());
    }

    @Test
    @DisplayName("getCustomerDTOById returns empty when customer not found")
    void getCustomerDTOById_whenNotFound_returnsEmpty() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<CustomerDTO> result = customerService.getCustomerDTOById(99L);

        assertFalse(result.isPresent());
    }
}
