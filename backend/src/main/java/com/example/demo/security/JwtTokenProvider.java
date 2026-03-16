package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 발급 / 검증 유틸리티
 * - accessToken: 24시간
 * - refreshToken: 7일
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private static final long ACCESS_TOKEN_MS  = 1000L * 60 * 60 * 24;     // 24h
    private static final long REFRESH_TOKEN_MS = 1000L * 60 * 60 * 24 * 7; // 7d

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String email) {
        return buildToken(email, ACCESS_TOKEN_MS);
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, REFRESH_TOKEN_MS);
    }

    private String buildToken(String subject, long expiryMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(secretKey)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
