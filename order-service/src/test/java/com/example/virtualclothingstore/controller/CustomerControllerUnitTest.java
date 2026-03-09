package com.example.virtualclothingstore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.example.virtualclothingstore.dto.CustomerDTO;
import com.example.virtualclothingstore.entity.Customer;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.service.CustomerService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController Unit Tests")
class CustomerControllerUnitTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController controller;

    private CustomerDTO dto;
    private Customer entity;

    @BeforeEach
    void setUp() {
        dto = new CustomerDTO();
        dto.setId(1L);
        dto.setFirstName("John");
        entity = new Customer();
        entity.setId(1L);
        entity.setFirstName("John");
    }

    @Test
    @DisplayName("getAllCustomers returns list")
    void getAllCustomers_returnsList() {
        when(customerService.getAllCustomerDTOs()).thenReturn(List.of(dto));
        var result = controller.getAllCustomers();
        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    @DisplayName("getCustomerById found")
    void getCustomerById_found() {
        when(customerService.getCustomerDTOById(1L)).thenReturn(Optional.of(dto));
        ResponseEntity<CustomerDTO> resp = controller.getCustomerById(1L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dto, resp.getBody());
    }

    @Test
    @DisplayName("getCustomerById missing")
    void getCustomerById_missing() {
        when(customerService.getCustomerDTOById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> controller.getCustomerById(2L));
    }

    @Test
    @DisplayName("createCustomer maps and returns")
    void createCustomer_mapsAndReturns() {
        when(customerService.fromDTO(dto)).thenReturn(entity);
        when(customerService.saveCustomer(entity)).thenReturn(entity);
        when(customerService.toDTO(entity)).thenReturn(dto);
        CustomerDTO result = controller.createCustomer(dto);
        assertEquals(dto, result);
        verify(customerService).fromDTO(dto);
        verify(customerService).saveCustomer(entity);
    }

    @Test
    @DisplayName("updateCustomer exists")
    void updateCustomer_exists() {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(entity));
        Customer converted = new Customer();
        when(customerService.fromDTO(dto)).thenReturn(converted);
        converted.setId(1L);
        when(customerService.saveCustomer(converted)).thenReturn(converted);
        when(customerService.toDTO(converted)).thenReturn(dto);

        ResponseEntity<CustomerDTO> resp = controller.updateCustomer(1L, dto);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dto, resp.getBody());
    }

    @Test
    @DisplayName("updateCustomer missing")
    void updateCustomer_missing() {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> controller.updateCustomer(1L, dto));
    }

    @Test
    @DisplayName("deleteCustomer not found returns 404")
    void deleteCustomer_notFound() {
        when(customerService.getCustomerById(5L)).thenReturn(Optional.empty());
        ResponseEntity<Void> resp = controller.deleteCustomer(5L);
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("deleteCustomer present returns 204")
    void deleteCustomer_present() {
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(entity));
        ResponseEntity<Void> resp = controller.deleteCustomer(1L);
        assertEquals(204, resp.getStatusCodeValue());
    }
}
