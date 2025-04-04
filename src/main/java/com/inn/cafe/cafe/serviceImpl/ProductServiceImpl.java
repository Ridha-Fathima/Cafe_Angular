package com.inn.cafe.cafe.serviceImpl;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.inn.cafe.cafe.JWT.JwtFilter;
import com.inn.cafe.cafe.POJO.Category;
import com.inn.cafe.cafe.POJO.Product;
import com.inn.cafe.cafe.constents.CafeConstant;
import com.inn.cafe.cafe.dao.ProductDao;
import com.inn.cafe.cafe.service.ProductService;
import com.inn.cafe.cafe.utils.CafeUtils;
import com.inn.cafe.cafe.wrapper.ProductWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductDao productDao;
    private final JwtFilter jwtFilter;
    @Override
    public ResponseEntity<String> addProduct(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                if(this.validateProductMap(requestMap, false)){
                    productDao.save(this.getProductFromMap(requestMap, false));
                    return CafeUtils.getResponseEntity("Product was added successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity(CafeConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
            } else {
                return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProducts() {
        try {
            return new ResponseEntity<>(productDao.getAllProducts(),HttpStatus.OK);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                if(this.validateProductMap(requestMap, true)){
                    Optional<Product> productDaoById = productDao.findById(Integer.parseInt(requestMap.get("id")));
                    if(productDaoById.isPresent()){
                        Product productFromMap = this.getProductFromMap(requestMap, true);
                        productFromMap.setStatus(productDaoById.get().getStatus());
                        productDao.save(productFromMap);
                        return CafeUtils.getResponseEntity("Product was updated successfully", HttpStatus.OK);
                    } else return CafeUtils.getResponseEntity("Product with id " + requestMap.get("id") + " does not exists", HttpStatus.NOT_FOUND);
                } else return CafeUtils.getResponseEntity(CafeConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
            } else return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteProduct(Integer id) {
        try {
            if(jwtFilter.isAdmin()){
                Optional<Product> product = productDao.findById(id);
                if(product.isPresent()){
                    productDao.deleteById(id);
                    return CafeUtils.getResponseEntity("Product was deleted successfully", HttpStatus.OK);
                } else return CafeUtils.getResponseEntity("Product with id " + id + " does not exists", HttpStatus.NOT_FOUND);
            } else return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateStatus(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                Optional<Product> product = productDao.findById(Integer.parseInt(requestMap.get("id")));
                if(product.isPresent()){
                    productDao.updateProductStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    return CafeUtils.getResponseEntity("Product status was updated successfully", HttpStatus.OK);
                } else return CafeUtils.getResponseEntity("Product with id " + requestMap.get("id") + " does not exists", HttpStatus.NOT_FOUND);
            } else return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategory(Integer id) {
        try {
            return new ResponseEntity<>(productDao.getProductByCategory(id), HttpStatus.OK);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProductWrapper> getById(Integer id) {
        try {
            return new ResponseEntity<>(productDao.getProductById(id), HttpStatus.OK);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Product getProductFromMap(Map<String, String> requestMap, boolean isAdd) {
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));
        Product product = new Product();
        if(isAdd){
            product.setId(Integer.parseInt(requestMap.get("id")));
        } else {
            product.setStatus("true");
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));
        return product;
    }

    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey("name")){
            if(requestMap.containsKey("id") && validateId){
                return true;
            } else return !validateId;
        }
        return false;
    }
}