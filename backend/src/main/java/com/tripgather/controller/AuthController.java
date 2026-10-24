package com.tripgather.controller;

import jakarta.validation.Valid;

import com.tripgather.dto.AuthRequest.LoginRequest;
import com.tripgather.dto.AuthRequest.SignupRequest;
import com.tripgather.dto.AuthResponse;
import com.tripgather.usecase.AuthUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    public org.springframework.web.servlet.view.RedirectView verify(@RequestParam String token) {
        authService.verifyEmail(token);
        return new org.springframework.web.servlet.view.RedirectView(frontendUrl + "/login?verified=true");
    }

    @GetMapping("/mock/kakao")
    public ResponseEntity<AuthResponse> mockKakaoLogin() {
        return ResponseEntity.ok(authService.mockKakaoLogin());
    }
}
