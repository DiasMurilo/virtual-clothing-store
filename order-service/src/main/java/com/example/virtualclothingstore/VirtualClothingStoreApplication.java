package com.example.virtualclothingstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VirtualClothingStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(VirtualClothingStoreApplication.class, args);
    }

}