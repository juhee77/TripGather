package com.tripgather.usecase;

import com.tripgather.dto.AuthRequest.LoginRequest;
import com.tripgather.dto.AuthRequest.SignupRequest;
import com.tripgather.dto.AuthResponse;

public interface AuthUseCase {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
    void verifyEmail(String token);
    AuthResponse mockKakaoLogin();
}
