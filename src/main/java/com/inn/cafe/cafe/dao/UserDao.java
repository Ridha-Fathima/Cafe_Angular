package com.inn.cafe.cafe.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.inn.cafe.cafe.POJO.User;
import com.inn.cafe.cafe.wrapper.UserWrapper;





public interface UserDao extends JpaRepository<User, Integer> {

    User findByEmailId(@Param("email") String email);

     List<UserWrapper> getAllUser();
     List<String> getAllAdmin();
    @Transactional
    @Modifying
     Integer updateStatus(@Param("status") String status, @Param("id") Integer id);
     User findByEmail(String email);

}
