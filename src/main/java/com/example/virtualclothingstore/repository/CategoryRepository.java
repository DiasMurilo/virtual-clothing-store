package com.example.virtualclothingstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.virtualclothingstore.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}