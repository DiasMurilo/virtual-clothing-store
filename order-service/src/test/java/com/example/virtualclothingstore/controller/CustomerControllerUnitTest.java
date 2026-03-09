package com.example.virtualclothingstore.controller;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @Test
    @DisplayName("getAllCustomers returns list from service")
    void getAllCustomers_returnsList() {
        when(customerService.getAllCustomerDTOs()).thenReturn(Collections.emptyList());

        assertEquals(Collections.emptyList(), controller.getAllCustomers());
        verify(customerService).getAllCustomerDTOs();
    }

    @Test
    @DisplayName("getCustomerById returns 200 when found")
    void getCustomerById_whenFound_returnsOk() {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(1L);
        when(customerService.getCustomerDTOById(1L)).thenReturn(Optional.of(dto));

        ResponseEntity<CustomerDTO> resp = controller.getCustomerById(1L);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(dto, resp.getBody());
    }

    @Test
    @DisplayName("getCustomerById throws when missing")
    void getCustomerById_whenMissing_throws() {
        when(customerService.getCustomerDTOById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> controller.getCustomerById(2L));
    }

    @Test
    @DisplayName("deleteCustomer returns 404 when absent")
    void deleteCustomer_whenNotFound_returns404() {
        when(customerService.getCustomerById(5L)).thenReturn(Optional.empty());
        ResponseEntity<Void> resp = controller.deleteCustomer(5L);
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    @DisplayName("deleteCustomer returns 204 when present")
    void deleteCustomer_whenPresent_returns204() {
        Customer c = new Customer();
        c.setId(1L);
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(c));

        ResponseEntity<Void> resp = controller.deleteCustomer(1L);
        assertEquals(204, resp.getStatusCodeValue());
    }
}
