package com.example.demo.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * STOMP CONNECT 시점의 인증 게이트. 여기서 토큰 검증이 뚫리면 이후 프레임의
 * 발신자를 신뢰할 수 없으므로, 거부되어야 할 경우를 중심으로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class StompHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private StompHandler stompHandler;

    private final MessageChannel channel = mock(MessageChannel.class);

    private StompHeaderAccessor accessor(StompCommand command, String authorizationHeader) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        // setUser 를 수행하려면 accessor 가 mutable 상태여야 한다.
        accessor.setLeaveMutable(true);
        if (authorizationHeader != null) {
            accessor.setNativeHeader("Authorization", authorizationHeader);
        }
        return accessor;
    }

    private Message<byte[]> message(StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    @DisplayName("유효한 토큰으로 CONNECT 시 인증 주체를 세션에 설정")
    void preSend_ValidToken_SetsUser() {
        // given
        StompHeaderAccessor accessor = accessor(StompCommand.CONNECT, "Bearer valid-token");
        given(jwtTokenProvider.validateToken("valid-token")).willReturn(true);
        given(jwtTokenProvider.getEmailFromToken("valid-token")).willReturn("user@test.com");

        // when
        Message<?> result = stompHandler.preSend(message(accessor), channel);

        // then
        assertThat(result).isNotNull();
        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Authorization 헤더 없이 CONNECT 시 연결 거부")
    void preSend_NoAuthorizationHeader_ThrowsException() {
        // given
        Message<byte[]> message = message(accessor(StompCommand.CONNECT, null));

        // when & then
        assertThatThrownBy(() -> stompHandler.preSend(message, channel))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessageContaining("STOMP 연결에 유효한 인증 토큰이 필요합니다.");
        verify(jwtTokenProvider, never()).validateToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Bearer 스킴이 아닌 헤더로 CONNECT 시 연결 거부")
    void preSend_NonBearerHeader_ThrowsException() {
        // given
        Message<byte[]> message = message(accessor(StompCommand.CONNECT, "Basic dXNlcjpwYXNz"));

        // when & then
        assertThatThrownBy(() -> stompHandler.preSend(message, channel))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessageContaining("STOMP 연결에 유효한 인증 토큰이 필요합니다.");
        verify(jwtTokenProvider, never()).validateToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 CONNECT 시 연결 거부")
    void preSend_InvalidToken_ThrowsException() {
        // given
        Message<byte[]> message = message(accessor(StompCommand.CONNECT, "Bearer invalid-token"));
        given(jwtTokenProvider.validateToken("invalid-token")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> stompHandler.preSend(message, channel))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessageContaining("STOMP 연결에 유효한 인증 토큰이 필요합니다.");
        verify(jwtTokenProvider, never()).getEmailFromToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("CONNECT가 아닌 프레임은 토큰 검증 없이 그대로 통과")
    void preSend_NonConnectCommand_PassesThrough() {
        // given
        StompHeaderAccessor accessor = accessor(StompCommand.SEND, null);
        Message<byte[]> message = message(accessor);

        // when
        Message<?> result = stompHandler.preSend(message, channel);

        // then
        assertThat(result).isSameAs(message);
        assertThat(accessor.getUser()).isNull();
    }

    @Test
    @DisplayName("STOMP 헤더가 없는 메시지는 그대로 통과")
    void preSend_NoStompAccessor_PassesThrough() {
        // given
        Message<String> message = new GenericMessage<>("payload");

        // when
        Message<?> result = stompHandler.preSend(message, channel);

        // then
        assertThat(result).isSameAs(message);
    }
}
