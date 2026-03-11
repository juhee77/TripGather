package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    /**
     * 임시 유저 정보를 반환하는 엔드포인트.
     * 향후 DB 연동 및 Security Context 연결을 통해 고도화할 수 있습니다.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyProfile() {
        Map<String, Object> mockUser = new HashMap<>();
        mockUser.put("id", 1L);
        mockUser.put("name", "Jihyun (지현)");
        mockUser.put("bio", "여행을 좋아하는 사람");
        mockUser.put("points", 1500);
        return ResponseEntity.ok(mockUser);
    }
}
