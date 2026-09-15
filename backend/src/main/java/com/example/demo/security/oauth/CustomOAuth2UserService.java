package com.example.demo.security.oauth;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        OAuth2UserInfo oAuth2UserInfo = null;
        if (registrationId.equals("kakao")) {
            oAuth2UserInfo = new KakaoUserInfo(attributes);
        } else if (registrationId.equals("naver")) {
            oAuth2UserInfo = new NaverUserInfo(attributes);
        }

        if (oAuth2UserInfo == null) {
            // OAuth2AuthenticationException(String) 은 인자를 에러 코드로만 취급해 getMessage() 가 null 이 된다.
            // 어떤 제공자로 시도했는지 로그에 남기려면 error 와 message 를 분리해 전달해야 한다.
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"),
                    "Unsupported provider: " + registrationId);
        }

        String provider = oAuth2UserInfo.getProvider();
        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();

        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, providerId);
        
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // 필요 시 정보 업데이트
            user.setName(name);
            userRepository.save(user);
        } else {
            user = User.builder()
                    .name(name)
                    .email(email)
                    .provider(provider)
                    .providerId(providerId)
                    .role("ROLE_USER")
                    .profileImageUrl(oAuth2UserInfo.getProfileImage())
                    .build();
            userRepository.save(user);
        }

        return oAuth2User;
    }
}
