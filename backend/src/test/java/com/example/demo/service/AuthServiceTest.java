package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.AuthRequest.LoginRequest;
import com.example.demo.dto.AuthRequest.SignupRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.demo.service.AuthServiceImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() {
        // given
        SignupRequest request = new SignupRequest();
        request.setName("테스트");
        request.setEmail("test@test.com");
        request.setPassword("password");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encoded_password");
        given(jwtTokenProvider.generateAccessToken(request.getEmail())).willReturn("test_token");

        // when
        AuthResponse response = authService.signup(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("test_token");
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void signup_Fail_DuplicateEmail() {
        // given
        SignupRequest request = new SignupRequest();
        request.setEmail("duplicate@test.com");

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> authService.signup(request));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        User user = User.builder()
                .email("test@test.com")
                .password("encoded_password")
                .name("테스트")
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(user.getEmail())).willReturn("test_token");

        // when
        AuthResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("test_token");
        assertThat(response.getName()).isEqualTo("테스트");
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
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }
}
