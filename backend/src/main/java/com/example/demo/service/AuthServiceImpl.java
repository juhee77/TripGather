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
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

    /**
     * 재설정 토큰이 살아 있는 시간.
     *
     * 이 토큰 하나로 비밀번호를 바꿀 수 있으므로 짧게 둔다.
     * 가입 인증 토큰에는 만료가 없지만, 그쪽은 계정을 여는 힘이 없어 성격이 다르다.
     */
    private static final java.time.Duration RESET_TOKEN_TTL = java.time.Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.example.demo.security.LoginAttemptService loginAttemptService;
    private final EmailService emailService;

    /**
     * 회원가입
     */
    public AuthResponse signup(SignupRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이메일 정보가 올바르지 않습니다.");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호를 입력해주세요.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
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
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이메일 정보가 올바르지 않습니다.");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호를 입력해주세요.");
        }
        if (loginAttemptService.isBlocked(request.getEmail())) {
            throw new IllegalStateException("보안을 위해 계정이 잠시 잠겼습니다. 나중에 다시 시도해주세요.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    loginAttemptService.loginFailed(request.getEmail());
                    throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(request.getEmail());
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
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
        if (token == null || token.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "인증 토큰이 올바르지 않습니다.");
        }
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED_ACCESS));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public AuthResponse mockKakaoLogin() {
        String mockEmail = "kakao_mock@example.com";
        User user = userRepository.findByEmail(mockEmail)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .name("KakaoMock")
                            .email(mockEmail)
                            .password(passwordEncoder.encode("kakao_mock_pwd_123!"))
                            .provider("kakao")
                            .role("ROLE_USER")
                            .emailVerified(true)
                            .build();
                    return userRepository.save(newUser);
                });

        String token = jwtTokenProvider.generateAccessToken(user.getEmail());
        return AuthResponse.builder()
                .accessToken(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    /**
     * 재설정 메일을 보낸다.
     *
     * 가입되지 않은 이메일이어도 똑같이 성공으로 답한다. 응답이 달라지면
     * 그것만으로 "이 이메일이 가입되어 있는가" 를 확인하는 수단이 되기 때문이다.
     * SNS 로 가입한 계정은 비밀번호 자체가 없으므로 메일을 보내지 않는다.
     */
    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이메일 정보가 올바르지 않습니다.");
        }
        userRepository.findByEmail(email.trim())
                .filter(user -> "local".equalsIgnoreCase(user.getProvider()))
                .ifPresent(user -> {
                    String token = java.util.UUID.randomUUID().toString();
                    user.setResetToken(token);
                    user.setResetTokenExpiresAt(java.time.LocalDateTime.now().plus(RESET_TOKEN_TTL));
                    userRepository.save(user);
                    emailService.sendPasswordResetEmail(user.getEmail(), token);
                });
    }

    /**
     * 토큰을 확인하고 비밀번호를 바꾼다.
     *
     * 성공하면 토큰을 비워 한 번만 쓰이게 한다.
     */
    @Override
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        if (token == null || token.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "재설정 토큰이 올바르지 않습니다.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "새 비밀번호를 입력해주세요.");
        }

        User user = userRepository.findByResetToken(token.trim())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                        "유효하지 않거나 이미 사용된 링크입니다. 다시 요청해주세요."));

        if (user.getResetTokenExpiresAt() == null
                || user.getResetTokenExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            // 만료된 토큰은 비워 두어 다시 쓰이지 않게 한다.
            user.setResetToken(null);
            user.setResetTokenExpiresAt(null);
            userRepository.save(user);
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    "링크가 만료되었습니다. 다시 요청해주세요.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);
    }
}
