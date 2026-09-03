package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.User;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.GatheringMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

    @SuppressWarnings("unchecked")
    private Map<String, SseEmitter> emitters() {
        return (Map<String, SseEmitter>) ReflectionTestUtils.getField(notificationService, "emitters");
    }

    @Test
    @DisplayName("구독 중인 사용자에게 알림 전송 성공")
    void send_SubscribedUser_Success() throws Exception {
        // given
        SseEmitter emitter = mock(SseEmitter.class);
        emitters().put("user@test.com", emitter);

        // when
        notificationService.send("user@test.com", "notification", "새 알림");

        // then
        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
        assertThat(emitters()).containsKey("user@test.com");
    }

    @Test
    @DisplayName("구독하지 않은 사용자에게 알림 전송 시 아무 동작도 하지 않음")
    void send_NotSubscribedUser_DoesNothing() {
        // when & then
        assertDoesNotThrow(() -> notificationService.send("ghost@test.com", "notification", "새 알림"));
        assertThat(emitters()).doesNotContainKey("ghost@test.com");
    }

    @Test
    @DisplayName("알림 전송 중 IOException 발생 시 해당 emitter 제거")
    void send_IOException_RemovesEmitter() throws Exception {
        // given
        SseEmitter emitter = mock(SseEmitter.class);
        willThrow(new IOException("연결 끊김")).given(emitter).send(any(SseEmitter.SseEventBuilder.class));
        emitters().put("user@test.com", emitter);

        // when
        notificationService.send("user@test.com", "notification", "새 알림");

        // then
        assertThat(emitters()).doesNotContainKey("user@test.com");
    }

    @Test
    @DisplayName("모임 전체 알림 전송 시 호스트와 승인된 멤버에게만 전송")
    void sendToAllMembers_OnlyHostAndApprovedMembers() throws Exception {
        // given
        Long gatheringId = 1L;
        User host = User.builder().id(1L).email("host@test.com").build();
        User approved = User.builder().id(2L).email("approved@test.com").build();
        User pending = User.builder().id(3L).email("pending@test.com").build();
        Gathering gathering = Gathering.builder().id(gatheringId).host(host).build();

        GatheringMember hostMember = GatheringMember.builder()
                .id(gatheringId).gathering(gathering).user(host).status(MemberStatus.APPROVED).build();
        GatheringMember approvedMember = GatheringMember.builder()
                .id(11L).gathering(gathering).user(approved).status(MemberStatus.APPROVED).build();
        GatheringMember pendingMember = GatheringMember.builder()
                .id(12L).gathering(gathering).user(pending).status(MemberStatus.PENDING).build();

        given(gatheringMemberRepository.findById(gatheringId)).willReturn(Optional.of(hostMember));
        given(gatheringMemberRepository.findByGatheringId(gatheringId))
                .willReturn(List.of(approvedMember, pendingMember));

        SseEmitter hostEmitter = mock(SseEmitter.class);
        SseEmitter approvedEmitter = mock(SseEmitter.class);
        SseEmitter pendingEmitter = mock(SseEmitter.class);
        emitters().put("host@test.com", hostEmitter);
        emitters().put("approved@test.com", approvedEmitter);
        emitters().put("pending@test.com", pendingEmitter);

        // when
        notificationService.sendToAllMembers(gatheringId, "gathering", "모임 공지");

        // then
        verify(hostEmitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(approvedEmitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(pendingEmitter, never()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("존재하지 않는 모임 ID로 전체 알림 전송 시 호스트 전송 없이 정상 종료")
    void sendToAllMembers_GatheringNotFound_SkipsHostNotification() {
        // given
        Long gatheringId = 99L;
        given(gatheringMemberRepository.findById(gatheringId)).willReturn(Optional.empty());
        given(gatheringMemberRepository.findByGatheringId(gatheringId)).willReturn(List.of());

        // when & then
        assertDoesNotThrow(() -> notificationService.sendToAllMembers(gatheringId, "gathering", "모임 공지"));
    }
}
