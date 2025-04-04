package com.inn.cafe.cafe.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.inn.cafe.cafe.wrapper.UserWrapper;

@RestController
public interface UserService {

    ResponseEntity<String> signUp(Map<String,String> requestMap);

    ResponseEntity<String> login(Map<String, String> requestMap);

    public ResponseEntity<List<UserWrapper>> getAllUsers();

    public ResponseEntity<String> update(Map<String, String> requestMap);
  
    public ResponseEntity<String> checkToken();

    public ResponseEntity<String> changePassword(Map<String, String> requestMap);

    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap);
    

}
