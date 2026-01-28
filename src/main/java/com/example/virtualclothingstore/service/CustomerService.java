package com.example.virtualclothingstore.service;

import com.example.virtualclothingstore.dto.CustomerDTO;
import com.example.virtualclothingstore.entity.Customer;
import com.example.virtualclothingstore.exception.ResourceNotFoundException;
import com.example.virtualclothingstore.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public List<CustomerDTO> getAllCustomerDTOs() {
        return getAllCustomers().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Optional<CustomerDTO> getCustomerDTOById(Long id) {
        return getCustomerById(id).map(this::toDTO);
    }

    private CustomerDTO toDTO(Customer customer) {
        return new CustomerDTO(customer.getId(), customer.getFirstName(), customer.getLastName(),
                customer.getEmail(), customer.getPhone(), customer.getCreatedAt());
    }

    public Customer fromDTO(CustomerDTO dto) {
        Customer customer = new Customer(dto.getFirstName(), dto.getLastName(), dto.getEmail(), dto.getPhone());
        customer.setId(dto.getId());
        return customer;
    }