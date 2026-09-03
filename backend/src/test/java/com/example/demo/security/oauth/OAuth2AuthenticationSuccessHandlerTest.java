package com.example.demo.security.oauth;

import com.example.demo.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(successHandler, "frontendUrl", "http://localhost:5173");
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    private Authentication authenticationWith(Map<String, Object> attributes) {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2User.getAttributes()).willReturn(attributes);
        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(oAuth2User);
        return authentication;
    }

    @Test
    @DisplayName("카카오 인증 성공 시 kakao_account 이메일로 토큰을 발급해 프론트엔드로 리다이렉트")
    void onAuthenticationSuccess_Kakao_RedirectsWithToken() throws Exception {
        // given
        Authentication authentication = authenticationWith(Map.of(
                "id", 1L,
                "kakao_account", Map.of("email", "kakao@test.com")
        ));
        given(tokenProvider.generateAccessToken("kakao@test.com")).willReturn("kakao-jwt");

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:5173/oauth2/redirect?token=kakao-jwt");
    }

    @Test
    @DisplayName("네이버 인증 성공 시 response 이메일로 토큰을 발급해 프론트엔드로 리다이렉트")
    void onAuthenticationSuccess_Naver_RedirectsWithToken() throws Exception {
        // given
        Authentication authentication = authenticationWith(Map.of(
                "response", Map.of("id", "naver-id-1", "email", "naver@test.com")
        ));
        given(tokenProvider.generateAccessToken("naver@test.com")).willReturn("naver-jwt");

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:5173/oauth2/redirect?token=naver-jwt");
    }

    @Test
    @DisplayName("알 수 없는 응답 구조인 경우 이메일 없이 토큰 발급을 시도")
    void onAuthenticationSuccess_UnknownAttributes_UsesNullEmail() throws Exception {
        // given
        Authentication authentication = authenticationWith(Map.of("sub", "google-id-1"));
        given(tokenProvider.generateAccessToken(null)).willReturn("fallback-jwt");

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:5173/oauth2/redirect?token=fallback-jwt");
    }
}
