package com.example.demo.security;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        // SecurityContextHolder 는 ThreadLocal 이라 테스트 간 인증 상태가 새지 않도록 반드시 정리한다.
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Bearer 토큰이면 SecurityContext에 인증 객체를 주입하고 다음 필터로 전달")
    void doFilterInternal_ValidToken_SetsAuthentication() throws Exception {
        // given
        request.addHeader("Authorization", "Bearer valid-token");
        User user = User.builder().id(1L).email("user@test.com").role("ROLE_USER").build();
        given(jwtTokenProvider.validateToken("valid-token")).willReturn(true);
        given(jwtTokenProvider.getEmailFromToken("valid-token")).willReturn("user@test.com");
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(user);
        assertThat(authentication.getDetails()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 없이 다음 필터로 전달")
    void doFilterInternal_NoHeader_SkipsAuthentication() throws Exception {
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer 스킴이 아닌 Authorization 헤더는 무시하고 다음 필터로 전달")
    void doFilterInternal_NonBearerHeader_SkipsAuthentication() throws Exception {
        // given
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 인증 없이 다음 필터로 전달")
    void doFilterInternal_InvalidToken_SkipsAuthentication() throws Exception {
        // given
        request.addHeader("Authorization", "Bearer invalid-token");
        given(jwtTokenProvider.validateToken("invalid-token")).willReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰은 유효하지만 해당 사용자가 없으면 인증 없이 다음 필터로 전달")
    void doFilterInternal_UserNotFound_SkipsAuthentication() throws Exception {
        // given
        request.addHeader("Authorization", "Bearer valid-token");
        given(jwtTokenProvider.validateToken("valid-token")).willReturn(true);
        given(jwtTokenProvider.getEmailFromToken("valid-token")).willReturn("ghost@test.com");
        given(userRepository.findByEmail("ghost@test.com")).willReturn(Optional.empty());

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("이미 인증된 요청이면 기존 인증 객체를 덮어쓰지 않음")
    void doFilterInternal_AlreadyAuthenticated_KeepsExistingAuthentication() throws Exception {
        // given
        request.addHeader("Authorization", "Bearer valid-token");
        Authentication existing = new UsernamePasswordAuthenticationToken(
                "existing@test.com", null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(existing);

        User user = User.builder().id(1L).email("user@test.com").role("ROLE_USER").build();
        given(jwtTokenProvider.validateToken("valid-token")).willReturn(true);
        given(jwtTokenProvider.getEmailFromToken("valid-token")).willReturn("user@test.com");
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
        verify(filterChain).doFilter(request, response);
    }
}
