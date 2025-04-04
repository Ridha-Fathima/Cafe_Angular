package com.inn.cafe.cafe.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.cafe.cafe.POJO.Category;

import java.util.List;

public interface CategoryDao extends JpaRepository<Category, Integer> {
    List<Category> getAllCategory();
}