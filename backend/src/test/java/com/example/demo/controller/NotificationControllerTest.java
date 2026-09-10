package com.example.demo.controller;

import com.example.demo.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * SseEmitter 반환은 MockMvc 상에서 비동기 디스패치를 유발해 검증이 불투명해지므로,
 * 위임만 수행하는 이 컨트롤러는 메서드를 직접 호출해 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    @DisplayName("SSE 구독 API는 인증 주체의 이메일로 구독을 위임")
    void subscribe_DelegatesWithPrincipalEmail() {
        // given
        Principal principal = mock(Principal.class);
        given(principal.getName()).willReturn("user@example.com");
        SseEmitter emitter = new SseEmitter();
        given(notificationService.subscribe("user@example.com")).willReturn(emitter);

        // when
        SseEmitter result = notificationController.subscribe(principal);

        // then
        assertThat(result).isSameAs(emitter);
    }
}
