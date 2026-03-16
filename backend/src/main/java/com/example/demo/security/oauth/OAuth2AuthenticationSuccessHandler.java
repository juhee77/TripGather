package com.example.demo.security.oauth;

import com.example.demo.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // registrationId 파악 (kakao, naver 등)
        // loadUser에서 이미 처리되었으므로 attributes에서 이메일을 추출하거나 provider 정보를 토대로 JWT 생성
        // 여기서는 이메일 기반으로 토큰 생성 (OAuth2User attributes 구조가 업체마다 다르므로 주의)
        String email = null;
        if (oAuth2User.getAttributes().containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttributes().get("kakao_account");
            email = (String) kakaoAccount.get("email");
        } else if (oAuth2User.getAttributes().containsKey("response")) {
             Map<String, Object> res = (Map<String, Object>) oAuth2User.getAttributes().get("response");
             email = (String) res.get("email");
        }

        String token = tokenProvider.generateAccessToken(email);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
