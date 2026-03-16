package com.example.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "vms7065vms7065vms7065vms7065vms7065vms7065vms7065vms7065vms7065vms7065";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret);
    }

    @Test
    @DisplayName("토큰 생성 및 검증 성공")
    void generateAndValidateToken() {
        // given
        String email = "test@test.com";

        // when
        String token = jwtTokenProvider.generateAccessToken(email);
        boolean isValid = jwtTokenProvider.validateToken(token);
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        // then
        assertThat(token).isNotBlank();
        assertThat(isValid).isTrue();
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("잘못된 토큰 검증 실패")
    void validateInvalidToken() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }
}
