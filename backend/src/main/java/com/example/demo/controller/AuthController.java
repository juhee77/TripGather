package com.example.demo.controller;

import jakarta.validation.Valid;

import com.example.demo.dto.AuthRequest.LoginRequest;
import com.example.demo.dto.AuthRequest.SignupRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.usecase.AuthUseCase;
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

    /**
     * 재설정 메일 요청.
     *
     * 가입 여부와 무관하게 항상 성공으로 답한다. 응답이 갈리면 그것만으로
     * 특정 이메일의 가입 여부를 알아내는 수단이 된다.
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody com.example.demo.dto.AuthRequest.PasswordResetRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(
            @Valid @RequestBody com.example.demo.dto.AuthRequest.PasswordResetConfirm request) {
        authService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.noContent().build();
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
