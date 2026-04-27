package com.example.demo.service;

import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.User;
import com.example.demo.repository.GatheringMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private GatheringMemberRepository gatheringMemberRepository;

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
    @DisplayName("모임 멤버에게 타겟 알림 전송 성공")
    void sendToAllMembers_Success() {
        // given
        User u1 = User.builder().email("u1@ex.com").build();
        GatheringMember m1 = GatheringMember.builder().user(u1).status(MemberStatus.APPROVED).build();
        given(gatheringMemberRepository.findByGatheringId(1L)).willReturn(List.of(m1));
        
        notificationService.subscribe("u1@ex.com");

        // when & then
        assertDoesNotThrow(() -> notificationService.sendToAllMembers(1L, "broadcast", "Hi All"));
    }
}
