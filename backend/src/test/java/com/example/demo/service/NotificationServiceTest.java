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

    @Mock
    private com.example.demo.repository.GatheringRepository gatheringRepository;

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
    @DisplayName("구독 중인 사용자에게 알림을 전송한다")
    void send_ToSubscribedUser() throws IOException {
        // given
        SseEmitter emitter = mock(SseEmitter.class);
        emitters().put("user@test.com", emitter);

        // when
        notificationService.send("user@test.com", "dm-received", "hello");

        // then
        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("구독하지 않은 사용자에게 전송해도 예외가 발생하지 않는다")
    void send_ToUnsubscribedUser_DoesNothing() {
        assertDoesNotThrow(() -> notificationService.send("nobody@test.com", "dm-received", "hello"));
    }

    @Test
    @DisplayName("전송 중 IOException 이 나면 해당 구독을 정리한다")
    void send_IOException_RemovesEmitter() throws IOException {
        // given
        SseEmitter emitter = mock(SseEmitter.class);
        willThrow(new IOException("broken pipe")).given(emitter).send(any(SseEmitter.SseEventBuilder.class));
        emitters().put("user@test.com", emitter);

        // when
        notificationService.send("user@test.com", "dm-received", "hello");

        // then
        assertThat(emitters()).doesNotContainKey("user@test.com");
    }

    @Test
    @DisplayName("모임 알림은 호스트와 승인된 크루에게만 전송한다")
    void sendToAllMembers_HostAndApprovedOnly() throws IOException {
        // given
        given(gatheringRepository.findHostEmailById(10L)).willReturn(Optional.of("host@test.com"));
        given(gatheringMemberRepository.findApprovedMemberEmails(10L)).willReturn(List.of("approved@test.com"));

        SseEmitter hostEmitter = mock(SseEmitter.class);
        SseEmitter approvedEmitter = mock(SseEmitter.class);
        SseEmitter pendingEmitter = mock(SseEmitter.class);
        emitters().put("host@test.com", hostEmitter);
        emitters().put("approved@test.com", approvedEmitter);
        emitters().put("pending@test.com", pendingEmitter);

        // when
        notificationService.sendToAllMembers(10L, "chat-received", "hi");

        // then
        verify(hostEmitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(approvedEmitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(pendingEmitter, never()).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("수신자 조회가 실패해도 호출자에게 예외가 전파되지 않는다")
    void sendToAllMembers_RepositoryThrows_DoesNotPropagate() {
        // given: LAZY 프록시 접근 등으로 조회가 실패하는 상황
        given(gatheringRepository.findHostEmailById(10L))
                .willThrow(new org.hibernate.LazyInitializationException("no Session"));

        // when & then: 채팅 브로드캐스트가 이 예외 때문에 중단되면 안 된다
        assertDoesNotThrow(() -> notificationService.sendToAllMembers(10L, "chat-received", "hi"));
    }

    @Test
    @DisplayName("존재하지 않는 모임에 대한 알림은 조용히 무시된다")
    void sendToAllMembers_GatheringNotFound_DoesNothing() {
        // given
        given(gatheringRepository.findHostEmailById(999L)).willReturn(Optional.empty());
        given(gatheringMemberRepository.findApprovedMemberEmails(999L)).willReturn(List.of());

        // when & then
        assertDoesNotThrow(() -> notificationService.sendToAllMembers(999L, "chat-received", "hi"));
    }

    @Test
    @DisplayName("null 또는 공백 수신자 이메일로 알림 전송 시 아무 작업도 수행하지 않는다")
    void send_NullOrBlankReceiverEmail_DoesNothing() {
        assertDoesNotThrow(() -> notificationService.send(null, "test", "data"));
        assertDoesNotThrow(() -> notificationService.send("  ", "test", "data"));
    }

    @Test
    @DisplayName("null 모임 ID로 모임 멤버 전송 시 아무 작업도 수행하지 않는다")
    void sendToAllMembers_NullGatheringId_DoesNothing() {
        assertDoesNotThrow(() -> notificationService.sendToAllMembers(null, "test", "data"));
    }

    @Test
    @DisplayName("완료된 emitter 가 IllegalStateException 을 던져도 호출자에게 전파되지 않는다")
    void send_IllegalStateException_DoesNotPropagate() throws IOException {
        // given: 이미 완료된 SseEmitter 는 send() 시 IOException 이 아니라 IllegalStateException 을 던진다.
        SseEmitter emitter = mock(SseEmitter.class);
        willThrow(new IllegalStateException("ResponseBodyEmitter has already completed"))
                .given(emitter).send(any(SseEmitter.SseEventBuilder.class));
        emitters().put("user@test.com", emitter);

        // when & then: 알림 전송 실패가 본래 동작(채팅 브로드캐스트 등)을 깨뜨리면 안 된다.
        assertDoesNotThrow(() -> notificationService.send("user@test.com", "chat-received", "hi"));
        assertThat(emitters()).doesNotContainKey("user@test.com");
    }

    @Test
    @DisplayName("죽은 emitter 의 정리 콜백이 살아있는 새 emitter 를 제거하지 않는다")
    void removeEmitter_StaleEmitter_DoesNotEvictCurrentEmitter() {
        // given: 같은 사용자가 새로고침해 새 구독이 기존 구독을 대체한 상황
        SseEmitter stale = notificationService.subscribe("user@test.com");
        SseEmitter current = notificationService.subscribe("user@test.com");
        assertThat(emitters().get("user@test.com")).isSameAs(current);

        // when: 뒤늦게 죽은(이전) emitter 의 정리 콜백이 실행된다
        notificationService.removeEmitter("user@test.com", stale);

        // then: 현재 살아있는 구독은 그대로 남아 있어야 한다
        assertThat(emitters().get("user@test.com")).isSameAs(current);
    }

    @Test
    @DisplayName("현재 emitter 의 정리 콜백은 정상적으로 구독을 제거한다")
    void removeEmitter_CurrentEmitter_RemovesSubscription() {
        // given
        SseEmitter current = notificationService.subscribe("user@test.com");

        // when
        notificationService.removeEmitter("user@test.com", current);

        // then
        assertThat(emitters()).doesNotContainKey("user@test.com");
    }
}
