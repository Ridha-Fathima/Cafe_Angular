package com.inn.cafe.cafe.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.inn.cafe.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.cafe.JWT.JwtFilter;
import com.inn.cafe.cafe.JWT.JwtUtil;
import com.inn.cafe.cafe.POJO.User;
import com.inn.cafe.cafe.constents.CafeConstant;
import com.inn.cafe.cafe.dao.UserDao;
import com.inn.cafe.cafe.service.UserService;
import com.inn.cafe.cafe.utils.CafeUtils;
import com.inn.cafe.cafe.utils.EmailUtils;
import com.inn.cafe.cafe.wrapper.UserWrapper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final AuthenticationManager authenticationManager;
    private final CustomerUserDetailsService customerUserDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final EmailUtils emailUtils;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Inside signup {}", requestMap);
        try {
            if (validateSignupMap(requestMap)) {
                User user = userDao.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userDao.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);

                } else {
                    return CafeUtils.getResponseEntity("Email already exist.", HttpStatus.BAD_REQUEST);
                }

            } else {
                return CafeUtils.getResponseEntity(CafeConstant.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }

        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    private boolean validateSignupMap(Map<String, String> requestMap) {

        if (requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email") && requestMap.containsKey("password")) {
            return true;
        }
        return false;

    }

    private User getUserFromMap(Map<String, String> requeMap) {
        User user = new User();
        user.setName(requeMap.get("name")); // since we are using data annotation we are not required to define getter
                                            // and setter here
        user.setContactNumber(requeMap.get("contactNumber"));
        user.setEmail(requeMap.get("email"));

        user.setPassword(passwordEncoder.encode(requeMap.get("password")));

        user.setStatus("false");
        user.setRole("user");
        return user;

    }

    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            String email = requestMap.get("email");
            String password = requestMap.get("password");

            log.info("Attempting login for email: {}", email);

            // Fetch user from database
            User user = userDao.findByEmailId(email);

            if (user == null) {
                log.error("User not found in database: {}", email);
                return new ResponseEntity<>("{\"message\":\"User not found.\"}", HttpStatus.BAD_REQUEST);
            }

            // Check if password matches the hashed password in DB
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.error("Password mismatch for user: {}", email);
                return new ResponseEntity<>("{\"message\":\"Bad credentials.\"}", HttpStatus.BAD_REQUEST);
            }

            // If password matches, authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            if (authentication.isAuthenticated()) {
                log.info("User authenticated successfully: {}", email);

                if ("true".equalsIgnoreCase(user.getStatus())) {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    return new ResponseEntity<>("{\"token\":\"" + token + "\"}", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("{\"message\":\"Wait for admin approval.\"}", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (BadCredentialsException ex) {
            log.error("Invalid credentials: {}", ex.getMessage());
            return new ResponseEntity<>("{\"message\":\"Bad credentials.\"}", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage());
        }

        return new ResponseEntity<>("{\"message\":\"An error occurred.\"}", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers() {
        try {
            if (JwtFilter.isAdmin())
                return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);
            else
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
public ResponseEntity<String> update(Map<String, String> requestMap) {
    try {
        if (JwtFilter.isAdmin()) {
            String idStr = requestMap.get("id");
            if (idStr == null || idStr.isEmpty()) {
                return CafeUtils.getResponseEntity("User ID is missing", HttpStatus.BAD_REQUEST);
            }
 
            Integer userId;
            try {
                userId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                return CafeUtils.getResponseEntity("Invalid user ID format", HttpStatus.BAD_REQUEST);
            }
 
            // Find the user by ID
            Optional<User> user = userDao.findById(userId);
            if (user.isPresent()) {
                String status = requestMap.get("status");
                if (status == null || status.isEmpty()) {
                    return CafeUtils.getResponseEntity("Status is missing", HttpStatus.BAD_REQUEST);
                }
 
                userDao.updateStatus(status, userId);
 
                try {
                    this.sendMailToAllAdmin(status, user.get().getEmail(), userDao.getAllAdmin());
                } catch (Exception e) {
                    e.printStackTrace();
                    return CafeUtils.getResponseEntity("User status updated, but failed to send email.", HttpStatus.INTERNAL_SERVER_ERROR);
                }
 
                return CafeUtils.getResponseEntity("User status updated successfully", HttpStatus.OK);
            } else {
                return CafeUtils.getResponseEntity("User ID does not exist", HttpStatus.NO_CONTENT);
            }
        } else {
            return CafeUtils.getResponseEntity(CafeConstant.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
        }
    } catch (Exception exception) {
        exception.printStackTrace();
        return CafeUtils.getResponseEntity("Error: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(JwtFilter.getCurrentUser());
        if (status != null && status.equalsIgnoreCase("true"))
            emailUtils.sendSimpleMessage(JwtFilter.getCurrentUser(), "Account Approved",
                    "User:- " + user + "\n is approved by \nADMIN:- " + JwtFilter.getCurrentUser(), allAdmin);
        else
            emailUtils.sendSimpleMessage(JwtFilter.getCurrentUser(), "Account Disabled",
                    "User:- " + user + "\n is disabled by \nADMIN:- " + JwtFilter.getCurrentUser(), allAdmin);
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return CafeUtils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmail(JwtFilter.getCurrentUser());
            if(user != null){
                if(user.getPassword().equalsIgnoreCase(requestMap.get("oldPassword"))){
                    user.setPassword(requestMap.get("newPassword"));
                    userDao.save(user);
                    return CafeUtils.getResponseEntity("Password has been updated successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Old password is not true", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception){
            exception.printStackTrace();
        }
    return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmail(requestMap.get("email"));
            if(!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail()))
                emailUtils.forgotMail(user.getEmail(), "Credentials by Cafe Management System", user.getPassword());
            return CafeUtils.getResponseEntity("Check your email for credentials", HttpStatus.BAD_GATEWAY);
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstant.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
