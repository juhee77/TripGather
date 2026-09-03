package com.example.demo.security.oauth;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestOperations;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * DefaultOAuth2UserService.loadUser 는 실제 사용자 정보 엔드포인트를 호출하므로,
 * setRestOperations 로 HTTP 호출만 대체해 소셜 로그인 분기 로직 자체를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestOperations restOperations;

    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        customOAuth2UserService = new CustomOAuth2UserService(userRepository);
        customOAuth2UserService.setRestOperations(restOperations);
    }

    private ClientRegistration registration(String registrationId, String userNameAttributeName) {
        return ClientRegistration.withRegistrationId(registrationId)
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/" + registrationId)
                .authorizationUri("https://example.com/oauth/authorize")
                .tokenUri("https://example.com/oauth/token")
                .userInfoUri("https://example.com/v2/user/me")
                .userNameAttributeName(userNameAttributeName)
                .build();
    }

    private OAuth2UserRequest userRequest(ClientRegistration registration) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                Instant.now(),
                Instant.now().plusSeconds(3600));
        return new OAuth2UserRequest(registration, accessToken);
    }

    @SuppressWarnings("unchecked")
    private void givenUserInfoResponse(Map<String, Object> attributes) {
        given(restOperations.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity<>(attributes, HttpStatus.OK));
    }

    @Test
    @DisplayName("카카오 최초 로그인 시 신규 유저를 ROLE_USER로 저장")
    void loadUser_KakaoFirstLogin_CreatesUser() {
        // given
        Map<String, Object> attributes = Map.of(
                "id", 1234567890L,
                "kakao_account", Map.of("email", "kakao@test.com"),
                "properties", Map.of("nickname", "홍길동", "profile_image", "https://img.kakao.com/1.jpg")
        );
        givenUserInfoResponse(attributes);
        given(userRepository.findByProviderAndProviderId("kakao", "1234567890")).willReturn(Optional.empty());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest(registration("kakao", "id")));

        // then
        assertThat(result.getAttributes()).containsEntry("id", 1234567890L);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getProvider()).isEqualTo("kakao");
        assertThat(saved.getProviderId()).isEqualTo("1234567890");
        assertThat(saved.getEmail()).isEqualTo("kakao@test.com");
        assertThat(saved.getName()).isEqualTo("홍길동");
        assertThat(saved.getRole()).isEqualTo("ROLE_USER");
        assertThat(saved.getProfileImageUrl()).isEqualTo("https://img.kakao.com/1.jpg");
    }

    @Test
    @DisplayName("네이버 재로그인 시 기존 유저의 이름만 갱신하고 신규 생성하지 않음")
    void loadUser_NaverExistingUser_UpdatesName() {
        // given
        Map<String, Object> attributes = Map.of(
                "response", Map.of(
                        "id", "naver-id-1",
                        "email", "naver@test.com",
                        "name", "변경된이름"
                )
        );
        givenUserInfoResponse(attributes);

        User existing = User.builder()
                .id(1L)
                .name("이전이름")
                .email("naver@test.com")
                .provider("naver")
                .providerId("naver-id-1")
                .role("ROLE_USER")
                .build();
        given(userRepository.findByProviderAndProviderId("naver", "naver-id-1")).willReturn(Optional.of(existing));

        // when
        customOAuth2UserService.loadUser(userRequest(registration("naver", "response")));

        // then
        assertThat(existing.getName()).isEqualTo("변경된이름");
        assertThat(existing.getId()).isEqualTo(1L);
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("지원하지 않는 소셜 제공자로 로그인 시 예외 발생 및 유저 미저장")
    void loadUser_UnsupportedProvider_ThrowsException() {
        // given
        givenUserInfoResponse(Map.of("sub", "google-id-1", "email", "google@test.com"));

        // when
        Throwable thrown = catchThrowable(
                () -> customOAuth2UserService.loadUser(userRequest(registration("google", "sub"))));

        // then
        assertThat(thrown).isInstanceOf(OAuth2AuthenticationException.class);
        // OAuth2AuthenticationException(String) 은 인자를 "메시지"가 아닌 "에러 코드"로 취급하므로
        // getMessage() 는 null 이고 진단 문자열은 OAuth2Error 의 errorCode 에 담긴다.
        OAuth2Error error = ((OAuth2AuthenticationException) thrown).getError();
        assertThat(error.getErrorCode()).isEqualTo("Unsupported provider: google");
        assertThat(thrown).hasMessage(null);
        verify(userRepository, never()).save(any(User.class));
    }
}
