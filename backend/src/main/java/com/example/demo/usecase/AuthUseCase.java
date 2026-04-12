package com.example.demo.usecase;

import com.example.demo.dto.AuthRequest.LoginRequest;
import com.example.demo.dto.AuthRequest.SignupRequest;
import com.example.demo.dto.AuthResponse;

public interface AuthUseCase {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
    void verifyEmail(String token);
    AuthResponse mockKakaoLogin();
}
