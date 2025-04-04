package com.inn.cafe.cafe.restImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.inn.cafe.cafe.rest.DashboardRest;
import com.inn.cafe.cafe.service.DashboardService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DashBoardRestImpl implements DashboardRest {
    private final DashboardService dashboardService;
    @Override
    public ResponseEntity<Map<String, Object>> getCount() {
        try {
            return dashboardService.getCount();
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return new ResponseEntity<>(new HashMap<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}