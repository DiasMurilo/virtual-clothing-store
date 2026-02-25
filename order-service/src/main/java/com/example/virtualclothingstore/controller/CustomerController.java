package com.example.virtualclothingstore.controller;

import com.example.virtualclothingstore.dto.CustomerDTO;
import com.example.virtualclothingstore.entity.Customer;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<CustomerDTO> getAllCustomers() {
        return customerService.getAllCustomerDTOs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        CustomerDTO customerDTO = customerService.getCustomerDTOById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        return ResponseEntity.ok(customerDTO);
    }

    @PostMapping
    public CustomerDTO createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        Customer customer = customerService.fromDTO(customerDTO);
        Customer saved = customerService.saveCustomer(customer);
        return customerService.toDTO(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) {
        if (!customerService.getCustomerById(id).isPresent()) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        Customer customer = customerService.fromDTO(customerDTO);
        customer.setId(id);
        Customer saved = customerService.saveCustomer(customer);
        return ResponseEntity.ok(customerService.toDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        if (!customerService.getCustomerById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}