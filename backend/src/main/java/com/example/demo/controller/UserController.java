package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.dto.UserResponse;
import com.example.demo.usecase.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userService;

    /**
     * 현재 로그인한 유저 정보. 인증 미구현 시 기본 유저(id=1) 반환.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {
        try {
            return ResponseEntity.ok(UserResponse.from(userService.getCurrentUser()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.from(userService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream()
                .map(UserResponse::from)
                .collect(java.util.stream.Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody User user) {
        return ResponseEntity.ok(UserResponse.from(userService.createUser(user)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long id, @RequestBody User update) {
        return ResponseEntity.ok(UserResponse.from(userService.updateProfile(id, update)));
    }
}
