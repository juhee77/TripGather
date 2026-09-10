package com.example.demo.security.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 카카오/네이버는 사용자 정보 응답 구조가 서로 달라(카카오는 kakao_account/properties 중첩,
 * 네이버는 response 래핑) 각 매핑 규칙과 누락 필드 방어를 함께 검증한다.
 */
class OAuth2UserInfoTest {

    @Test
    @DisplayName("카카오 사용자 정보 매핑 성공")
    void kakaoUserInfo_MapsAllFields() {
        // given
        Map<String, Object> attributes = Map.of(
                "id", 1234567890L,
                "kakao_account", Map.of("email", "kakao@test.com"),
                "properties", Map.of("nickname", "홍길동", "profile_image", "https://img.kakao.com/1.jpg")
        );

        // when
        KakaoUserInfo userInfo = new KakaoUserInfo(attributes);

        // then
        assertThat(userInfo.getProvider()).isEqualTo("kakao");
        assertThat(userInfo.getProviderId()).isEqualTo("1234567890");
        assertThat(userInfo.getEmail()).isEqualTo("kakao@test.com");
        assertThat(userInfo.getName()).isEqualTo("홍길동");
        assertThat(userInfo.getProfileImage()).isEqualTo("https://img.kakao.com/1.jpg");
    }

    @Test
    @DisplayName("카카오 사용자 정보에 kakao_account가 없으면 이메일은 null 반환")
    void kakaoUserInfo_NoKakaoAccount_ReturnsNullEmail() {
        // given
        KakaoUserInfo userInfo = new KakaoUserInfo(Map.of("id", 1L));

        // when & then
        assertThat(userInfo.getEmail()).isNull();
    }

    @Test
    @DisplayName("카카오 사용자 정보에 properties가 없으면 이름과 프로필 이미지는 null 반환")
    void kakaoUserInfo_NoProperties_ReturnsNullNameAndImage() {
        // given
        KakaoUserInfo userInfo = new KakaoUserInfo(Map.of("id", 1L));

        // when & then
        assertThat(userInfo.getName()).isNull();
        assertThat(userInfo.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("카카오 계정에 이메일 제공 동의가 없으면 이메일은 null 반환")
    void kakaoUserInfo_NoEmailConsent_ReturnsNullEmail() {
        // given
        Map<String, Object> attributes = Map.of(
                "id", 1L,
                "kakao_account", Map.of("profile_nickname_needs_agreement", false)
        );

        // when
        KakaoUserInfo userInfo = new KakaoUserInfo(attributes);

        // then
        assertThat(userInfo.getEmail()).isNull();
    }

    @Test
    @DisplayName("네이버 사용자 정보 매핑 성공 - response 래핑 해제")
    void naverUserInfo_UnwrapsResponse() {
        // given
        Map<String, Object> attributes = Map.of(
                "resultcode", "00",
                "response", Map.of(
                        "id", "naver-id-1",
                        "email", "naver@test.com",
                        "name", "김철수",
                        "profile_image", "https://img.naver.com/1.jpg"
                )
        );

        // when
        NaverUserInfo userInfo = new NaverUserInfo(attributes);

        // then
        assertThat(userInfo.getProvider()).isEqualTo("naver");
        assertThat(userInfo.getProviderId()).isEqualTo("naver-id-1");
        assertThat(userInfo.getEmail()).isEqualTo("naver@test.com");
        assertThat(userInfo.getName()).isEqualTo("김철수");
        assertThat(userInfo.getProfileImage()).isEqualTo("https://img.naver.com/1.jpg");
    }

    @Test
    @DisplayName("네이버 응답에 선택 항목이 빠져 있으면 해당 값은 null 반환")
    void naverUserInfo_MissingOptionalFields_ReturnsNull() {
        // given
        Map<String, Object> attributes = Map.of("response", Map.of("id", "naver-id-1"));

        // when
        NaverUserInfo userInfo = new NaverUserInfo(attributes);

        // then
        assertThat(userInfo.getProviderId()).isEqualTo("naver-id-1");
        assertThat(userInfo.getEmail()).isNull();
        assertThat(userInfo.getName()).isNull();
        assertThat(userInfo.getProfileImage()).isNull();
    }
}
