package com.inn.cafe.cafe.restImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.inn.cafe.cafe.POJO.Category;
import com.inn.cafe.cafe.constents.CafeConstant;
import com.inn.cafe.cafe.rest.CategoryRest;
import com.inn.cafe.cafe.service.CategoryService;



@RestController

public class CategoryRestImpl implements CategoryRest {

    @Autowired
    CategoryService categoryService;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> request) {
        try {
            return categoryService.addCategory(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return com.inn.cafe.cafe.utils.CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
 
    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try {
            return categoryService.getCategories(filterValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> request) {
        try {
            return categoryService.updateCategory(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return com.inn.cafe.cafe.utils.CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
        

 


}