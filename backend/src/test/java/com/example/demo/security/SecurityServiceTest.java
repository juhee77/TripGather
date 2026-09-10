package com.example.demo.security;

import com.example.demo.domain.User;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityService securityService;

    @AfterEach
    void tearDown() {
        // SecurityContextHolder 는 ThreadLocal 이라 테스트 간 인증 상태가 새지 않도록 반드시 정리한다.
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, AuthorityUtils.NO_AUTHORITIES));
    }

    @Test
    @DisplayName("현재 인증된 사용자 이메일 조회 성공")
    void getCurrentUserEmail_Success() {
        // given
        authenticateAs("user@test.com");

        // when & then
        assertThat(securityService.getCurrentUserEmail()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("현재 인증된 사용자 엔티티 조회 성공")
    void getCurrentUser_Success() {
        // given
        authenticateAs("user@test.com");
        User user = User.builder().id(1L).email("user@test.com").name("홍길동").build();
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));

        // when
        User result = securityService.getCurrentUser();

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("인증 정보의 이메일에 해당하는 사용자가 없으면 예외 발생")
    void getCurrentUser_UserNotFound_ThrowsException() {
        // given
        authenticateAs("ghost@test.com");
        given(userRepository.findByEmail("ghost@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> securityService.getCurrentUser())
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("익명 인증 토큰인 경우 익명 사용자로 판별")
    void isAnonymous_AnonymousToken_ReturnsTrue() {
        // given
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        // when & then
        assertThat(securityService.isAnonymous()).isTrue();
    }

    @Test
    @DisplayName("이메일이 빈 문자열인 경우 익명 사용자로 판별")
    void isAnonymous_EmptyEmail_ReturnsTrue() {
        // given
        authenticateAs("");

        // when & then
        assertThat(securityService.isAnonymous()).isTrue();
    }

    @Test
    @DisplayName("정상 로그인 사용자는 익명이 아님")
    void isAnonymous_AuthenticatedUser_ReturnsFalse() {
        // given
        authenticateAs("user@test.com");

        // when & then
        assertThat(securityService.isAnonymous()).isFalse();
    }
}
