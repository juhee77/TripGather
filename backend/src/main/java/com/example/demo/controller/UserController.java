package com.example.demo.controller;

import jakarta.validation.Valid;

import com.example.demo.domain.User;
import com.example.demo.dto.UserResponse;
import com.example.demo.usecase.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

    /**
     * 단건 프로필. 호스트·크루를 표시하려고 비회원에게도 열어 둔 자리라
     * 이메일과 권한이 빠진 공개용 범위만 돌려준다.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponse.publicView(userService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody com.example.demo.dto.UserRequest request) {
        return ResponseEntity.ok(UserResponse.from(userService.createUser(request.toEntity())));
    }

    /**
     * 프로필 수정. 본인 계정만 수정할 수 있다.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long id, @Valid @RequestBody com.example.demo.dto.UserRequest request) {
        User currentUser = userService.getCurrentUser();
        if (!currentUser.getId().equals(id)) {
            throw new com.example.demo.exception.CustomException(
                    com.example.demo.exception.ErrorCode.FORBIDDEN_ACTION);
        }
        return ResponseEntity.ok(UserResponse.from(userService.updateProfile(id, request.toEntity())));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawMyProfile() {
        try {
            User currentUser = userService.getCurrentUser();
            userService.deactivateUser(currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
