package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT == accessor.getCommand()) {
            // CONNECT 시점에 토큰을 검증하고, 실패하면 연결 자체를 거부한다.
            // (인증되지 않은 세션을 허용하면 이후 프레임에서 발신자를 신뢰할 수 없다)
            String email = resolveEmail(accessor.getFirstNativeHeader("Authorization"));
            if (email == null) {
                log.warn("[STOMP] Rejected CONNECT: missing or invalid token");
                throw new MessageDeliveryException("STOMP 연결에 유효한 인증 토큰이 필요합니다.");
            }

            log.debug("[STOMP] Authenticated user: {}", email);
            accessor.setUser(new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList()));
        }

        return message;
    }

    private String resolveEmail(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        if (!jwtTokenProvider.validateToken(token)) {
            return null;
        }
        return jwtTokenProvider.getEmailFromToken(token);
    }
}
