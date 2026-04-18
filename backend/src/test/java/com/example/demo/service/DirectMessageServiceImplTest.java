package com.example.demo.service;

import com.example.demo.domain.DirectMessage;
import com.example.demo.domain.User;
import com.example.demo.repository.DirectMessageRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceImplTest {

    @Mock
    private DirectMessageRepository dmRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DirectMessageServiceImpl dmService;

    @Test
    @DisplayName("DM 전송 성공 테스트")
    void sendDM_Success() {
        // given
        User sender = User.builder().email("sender@example.com").build();
        User receiver = User.builder().email("receiver@example.com").build();

        given(userRepository.findByEmail("sender@example.com")).willReturn(Optional.of(sender));
        given(userRepository.findByEmail("receiver@example.com")).willReturn(Optional.of(receiver));

        DirectMessage fakeMsg = DirectMessage.builder().content("hi").build();
        given(dmRepository.save(any(DirectMessage.class))).willReturn(fakeMsg);

        // when
        DirectMessage result = dmService.sendDM("sender@example.com", "receiver@example.com", "hi");

        // then
        assertThat(result.getContent()).isEqualTo("hi");
        verify(dmRepository).save(any(DirectMessage.class));
    }

    @Test
    @DisplayName("발신자를 찾을 수 없을 때 전송 예외 발생")
    void sendDM_SenderNotFound_ThrowsException() {
        // given
        given(userRepository.findByEmail("sender@example.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dmService.sendDM("sender@example.com", "receiver@example.com", "hi"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sender not found");
    }

    @Test
    @DisplayName("채팅 내역 조회 성공 테스트")
    void getChatHistory_Success() {
        // given
        User user1 = User.builder().email("u1@ex.com").build();
        User user2 = User.builder().email("u2@ex.com").build();

        given(userRepository.findByEmail("u1@ex.com")).willReturn(Optional.of(user1));
        given(userRepository.findByEmail("u2@ex.com")).willReturn(Optional.of(user2));
        given(dmRepository.findChatHistory(user1, user2)).willReturn(List.of(new DirectMessage()));

        // when
        List<DirectMessage> history = dmService.getChatHistory("u1@ex.com", "u2@ex.com");

        // then
        assertThat(history).hasSize(1);
    }

    @Test
    @DisplayName("DM 단건 읽음 처리 테스트")
    void markAsRead_Success() {
        // given
        DirectMessage dm = DirectMessage.builder().isRead(false).build();
        given(dmRepository.findById(1L)).willReturn(Optional.of(dm));

        // when
        dmService.markAsRead(1L);

        // then
        assertThat(dm.isRead()).isTrue();
    }

    @Test
    @DisplayName("채팅 상대방과의 모든 알림 읽음 처리 테스트")
    void markMessagesAsRead_Success() {
        // given
        User me = User.builder().email("me@ex.com").build();
        User other = User.builder().email("other@ex.com").build();

        given(userRepository.findByEmail("me@ex.com")).willReturn(Optional.of(me));
        given(userRepository.findByEmail("other@ex.com")).willReturn(Optional.of(other));

        DirectMessage unreadDM = DirectMessage.builder().sender(other).receiver(me).isRead(false).build();
        given(dmRepository.findByReceiverAndIsReadFalse(me)).willReturn(List.of(unreadDM));

        // when
        dmService.markMessagesAsRead("me@ex.com", "other@ex.com");

        // then
        assertThat(unreadDM.isRead()).isTrue();
    }

    @Test
    @DisplayName("채팅 파트너 목록 조회 (발신 및 수신 모두 포함)")
    void getChatPartners_Success() {
        // given
        User partner1 = User.builder().id(2L).build();
        User partner2 = User.builder().id(3L).build();

        given(dmRepository.findReceiversBySenderEmail("me@ex.com")).willReturn(List.of(partner1));
        given(dmRepository.findSendersByReceiverEmail("me@ex.com")).willReturn(List.of(partner2));

        // when
        List<User> partners = dmService.getChatPartners("me@ex.com");

        // then
        assertThat(partners).hasSize(2).contains(partner1, partner2);
    }
}
