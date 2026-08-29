package com.example.demo.service;

import com.example.demo.exception.CustomException;
import com.example.demo.repository.GatheringMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private GatheringMemberRepository gatheringMemberRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("SSE 구독 성공 테스트")
    void subscribe_Success() {
        // when
        SseEmitter emitter = notificationService.subscribe("user@test.com");

        // then
        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("SSE 구독 시 공백 또는 null 이메일 전달 시 예외 발생")
    void subscribe_EmptyEmail_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> notificationService.subscribe("   "))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이메일 정보가 올바르지 않습니다.");
    }
}
