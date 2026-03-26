package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.AuthRequest.LoginRequest;
import com.example.demo.dto.AuthRequest.SignupRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.demo.usecase.AuthUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.example.demo.security.LoginAttemptService loginAttemptService;
    private final EmailService emailService;

    /**
     * 회원가입
     */
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        String verificationToken = java.util.UUID.randomUUID().toString();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider("local")
                .role("ROLE_USER")
                .emailVerified(false)
                .verificationToken(verificationToken)
                .build();

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return AuthResponse.builder()
                .accessToken("") // No token until verified
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    /**
     * 로그인
     */
    public AuthResponse login(LoginRequest request) {
        if (loginAttemptService.isBlocked(request.getEmail())) {
            throw new IllegalStateException("보안을 위해 계정이 잠시 잠겼습니다. 나중에 다시 시도해주세요.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    loginAttemptService.loginFailed(request.getEmail());
                    throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(request.getEmail());
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
        }

        loginAttemptService.loginSucceeded(request.getEmail());
        String token = jwtTokenProvider.generateAccessToken(user.getEmail());
        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 토큰입니다."));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }
}
