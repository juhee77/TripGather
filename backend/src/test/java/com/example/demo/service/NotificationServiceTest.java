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
        User host = User.builder().id(1L).email("host@test.com").build();
        User approved = User.builder().id(2L).email("approved@test.com").build();
        User pending = User.builder().id(3L).email("pending@test.com").build();
        Gathering gathering = Gathering.builder().id(10L).host(host).build();

        given(gatheringMemberRepository.findById(10L))
                .willReturn(Optional.of(GatheringMember.builder().gathering(gathering).user(approved).build()));
        given(gatheringMemberRepository.findByGatheringId(10L)).willReturn(List.of(
                GatheringMember.builder().gathering(gathering).user(approved).status(MemberStatus.APPROVED).build(),
                GatheringMember.builder().gathering(gathering).user(pending).status(MemberStatus.PENDING).build()
        ));

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
    @DisplayName("존재하지 않는 모임에 대한 알림은 조용히 무시된다")
    void sendToAllMembers_GatheringNotFound_DoesNothing() {
        // given
        given(gatheringMemberRepository.findById(999L)).willReturn(Optional.empty());
        given(gatheringMemberRepository.findByGatheringId(999L)).willReturn(List.of());

        // when & then
        assertDoesNotThrow(() -> notificationService.sendToAllMembers(999L, "chat-received", "hi"));
    }
}
