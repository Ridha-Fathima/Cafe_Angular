package com.inn.cafe.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.cafe.JWT.JwtFilter;
import com.inn.cafe.cafe.POJO.Category;
import com.inn.cafe.cafe.constents.CafeConstant;
import com.inn.cafe.cafe.dao.CategoryDao;
import com.inn.cafe.cafe.service.CategoryService;
import com.inn.cafe.cafe.utils.CafeUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryDao categoryDao;
    private final JwtFilter jwtFilter;
    @Override
    public ResponseEntity<String> addCategory(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                if(this.validateCategoryMap(requestMap, false)){
                    categoryDao.save(getCategoryFromMap(requestMap, false));
                    return CafeUtils.getResponseEntity("Category was added successfully", HttpStatus.OK);
                }
            }else {
                return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Category>> getCategories(String filterValue) {
        try {
            if(!Strings.isNullOrEmpty(filterValue) && filterValue.equalsIgnoreCase("true"))
                return new ResponseEntity<>(categoryDao.getAllCategory(), HttpStatus.OK);
            return new ResponseEntity<>(categoryDao.findAll(), HttpStatus.OK);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return new ResponseEntity<List<Category>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                if(validateCategoryMap(requestMap, true)){
                    Optional<Category> category = categoryDao.findById(Integer.parseInt(requestMap.get("id")));
                    if(!category.isEmpty()){
                        categoryDao.save(this.getCategoryFromMap(requestMap, true));
                        return CafeUtils.getResponseEntity("Category was updated successfully", HttpStatus.OK);
                    } else {
                        return CafeUtils.getResponseEntity("Category with id "+Integer.parseInt(requestMap.get("id")) +" does not exists" ,HttpStatus.BAD_REQUEST);
                    }
                }
                return CafeUtils.getResponseEntity(CafeConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateCategoryMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey("name")){
            if(requestMap.containsKey("id") && validateId){
                return true;
            }
            return !validateId;
        }
        return false;
    }
    private Category getCategoryFromMap(Map<String, String> requestMap, Boolean isAdd){
        Category category = new Category();
        if(isAdd){
            category.setId(Integer.parseInt(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }

}
