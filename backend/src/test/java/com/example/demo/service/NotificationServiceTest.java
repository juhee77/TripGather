package com.example.demo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("SseEmitter 구독 정상 생성 테스트")
    void subscribe_Success() {
        // given
        String email = "test@example.com";

        // when
        SseEmitter emitter = notificationService.subscribe(email);

        // then
        assertThat(emitter).isNotNull();
        assertThat(emitter.getTimeout()).isEqualTo(60L * 1000 * 60);
    }

    @Test
    @DisplayName("이벤트 전송 정상 동작 테스트")
    void send_Success() {
        // given
        String email = "test@example.com";
        notificationService.subscribe(email);

        // when & then
        assertDoesNotThrow(() -> notificationService.send(email, "testEvent", "Hello World"));
    }

    @Test
    @DisplayName("구독하지 않은 이메일로 전송 시 예외 미발생 (Silent fail)")
    void send_WhenNotSubscribed_SilentFail() {
        // given
        String email = "unknown@example.com";

        // when & then
        assertDoesNotThrow(() -> notificationService.send(email, "testEvent", "Hello World"));
    }
}
