package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.AuthRequest.LoginRequest;
import com.example.demo.dto.AuthRequest.SignupRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider("local")
                .role("ROLE_USER")
                .build();

        userRepository.save(user);

        String token = jwtTokenProvider.generateAccessToken(user.getEmail());
        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    /**
     * 로그인
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtTokenProvider.generateAccessToken(user.getEmail());
        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
