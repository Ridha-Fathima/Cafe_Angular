package com.inn.cafe.cafe.service;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface DashboardService {
    public ResponseEntity<Map<String, Object>> getCount();
}
