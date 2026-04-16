package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.AuthRequest.LoginRequest;
import com.example.demo.dto.AuthRequest.SignupRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.security.LoginAttemptService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private EmailService emailService;

    @Mock
    private PointService pointService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("회원가입 성공 - 인증 메일 발송 확인 및 토큰 미발급")
    void signup_Success() {
        // given
        SignupRequest request = new SignupRequest();
        request.setName("테스트");
        request.setEmail("test@test.com");
        request.setPassword("password");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encoded_password");

        // when
        AuthResponse response = authService.signup(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo(""); // No token until verified
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void signup_Fail_DuplicateEmail() {
        // given
        SignupRequest request = new SignupRequest();
        request.setEmail("duplicate@test.com");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        assertThrows(CustomException.class, () -> authService.signup(request));
    }

    @Test
    @DisplayName("로그인 성공 - 인증된 사용자")
    void login_Success() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("encoded_password")
                .name("테스트")
                .emailVerified(true) // Should be verified to login
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(user.getEmail())).willReturn("test_token");

        // when
        AuthResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("test_token");
        assertThat(response.getName()).isEqualTo("테스트");
        verify(loginAttemptService).loginSucceeded(request.getEmail());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_Fail_WrongPassword() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrong");

        User user = User.builder()
                .email("test@test.com")
                .password("encoded_password")
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        // when & then
        assertThrows(CustomException.class, () -> authService.login(request));
        verify(loginAttemptService).loginFailed(request.getEmail());
    }

    @Test
    @DisplayName("로그인 실패 - 계정 잠금")
    void login_Fail_Blocked() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("blocked@test.com");
        request.setPassword("password");

        given(loginAttemptService.isBlocked(request.getEmail())).willReturn(true);

        // when & then
        assertThrows(IllegalStateException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 미인증")
    void login_Fail_EmailNotVerified() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("notverified@test.com");
        request.setPassword("password");

        User user = User.builder()
                .email("notverified@test.com")
                .password("encoded_password")
                .emailVerified(false)
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);

        // when & then
        assertThrows(IllegalStateException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void login_Fail_UserNotFound() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@test.com");
        request.setPassword("password");

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> authService.login(request));
        verify(loginAttemptService).loginFailed(request.getEmail());
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_Success() {
        // given
        String token = "valid-token";
        User user = User.builder().email("test@test.com").emailVerified(false).verificationToken(token).build();

        given(userRepository.findByVerificationToken(token)).willReturn(Optional.of(user));

        // when
        authService.verifyEmail(token);

        // then
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getVerificationToken()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("이메일 인증 실패 - 잘못된 토큰")
    void verifyEmail_Fail_InvalidToken() {
        // given
        String token = "invalid-token";
        given(userRepository.findByVerificationToken(token)).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> authService.verifyEmail(token));
    }

    @Test
    @DisplayName("카카오 모의 로그인 - 기존 사용자")
    void mockKakaoLogin_ExistingUser() {
        // given
        String email = "kakao_mock@example.com";
        User user = User.builder().id(1L).email(email).name("KakaoMock").build();

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(jwtTokenProvider.generateAccessToken(email)).willReturn("mock_token");

        // when
        AuthResponse response = authService.mockKakaoLogin();

        // then
        assertThat(response.getAccessToken()).isEqualTo("mock_token");
        assertThat(response.getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("카카오 모의 로그인 - 신규 사용자 생성")
    void mockKakaoLogin_NewUser() {
        // given
        String email = "kakao_mock@example.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encoded_pwd");
        given(userRepository.save(any(User.class))).willAnswer(i -> {
            User u = i.getArgument(0);
            return u;
        });
        given(jwtTokenProvider.generateAccessToken(email)).willReturn("mock_token");

        // when
        AuthResponse response = authService.mockKakaoLogin();

        // then
        assertThat(response.getAccessToken()).isEqualTo("mock_token");
        verify(userRepository).save(any(User.class));
    }
}
