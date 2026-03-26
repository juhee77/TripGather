package com.example.demo.controller;

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
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthUseCase authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    public org.springframework.web.servlet.view.RedirectView verify(@RequestParam String token) {
        authService.verifyEmail(token);
        return new org.springframework.web.servlet.view.RedirectView("http://localhost:5173/login?verified=true");
    }
}
