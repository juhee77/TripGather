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

    @Mock
    private ProfanityFilterService profanityFilterService;

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

        DirectMessage fakeMsg = DirectMessage.builder()
                .content("hi")
                .sender(sender)
                .receiver(receiver)
                .sentAt(java.time.LocalDateTime.now())
                .build();
        given(dmRepository.save(any(DirectMessage.class))).willReturn(fakeMsg);

        // when
        com.example.demo.dto.DMResponse result = dmService.sendDM("sender@example.com", "receiver@example.com", "hi");

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
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
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
    @DisplayName("채팅 내역 조회 시 존재하지 않는 사용자 이메일 전달 시 예외 발생")
    void getChatHistory_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findByEmail("u1@ex.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dmService.getChatHistory("u1@ex.com", "u2@ex.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("채팅 내역 조회 시 공백 또는 null 이메일 전달 시 예외 발생")
    void getChatHistory_EmptyEmail_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> dmService.getChatHistory("  ", "u2@ex.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("이메일 정보가 필요합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 DM 읽음 처리 시 예외 발생")
    void markAsRead_NotFound_ThrowsException() {
        // given
        given(dmRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dmService.markAsRead(99L))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
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
        given(dmRepository.findUnreadMessages(other, me)).willReturn(List.of(unreadDM));

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

        given(dmRepository.findChatPartners("me@ex.com")).willReturn(List.of(partner1, partner2));

        // when
        List<User> partners = dmService.getChatPartners("me@ex.com");

        // then
        assertThat(partners).hasSize(2).contains(partner1, partner2);
    }

    @Test
    @DisplayName("DM 메시지 내 비속어 포함 시 예외 발생")
    void sendDM_ProfanityContent_ThrowsException() {
        // given
        String content = "씨발 안녕";
        org.mockito.BDDMockito.willThrow(new com.example.demo.exception.CustomException(com.example.demo.exception.ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어가 포함되어 있습니다."))
                .given(profanityFilterService).validateText(content);

        // when & then
        assertThatThrownBy(() -> dmService.sendDM("sender@example.com", "receiver@example.com", content))
                .isInstanceOf(com.example.demo.exception.CustomException.class);
    }

    @Test
    @DisplayName("자기 자신에게 DM 전송 시 예외 발생")
    void sendDM_SelfDM_ThrowsException() {
        assertThatThrownBy(() -> dmService.sendDM("same@example.com", "same@example.com", "hello me"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("자기 자신에게는 메시지를 발송할 수 없습니다.");
    }

    @Test
    @DisplayName("공백 내용으로 DM 전송 시 예외 발생")
    void sendDM_EmptyContent_ThrowsException() {
        assertThatThrownBy(() -> dmService.sendDM("sender@example.com", "receiver@example.com", "   "))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("메시지 내용을 입력해주세요.");
    }

    @Test
    @DisplayName("자기 자신과의 대화방 읽음 처리 시 예외 발생")
    void markMessagesAsRead_SelfEmail_ThrowsException() {
        assertThatThrownBy(() -> dmService.markMessagesAsRead("same@example.com", "same@example.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("자기 자신과의 대화방은 처리할 수 없습니다.");
    }

    @Test
    @DisplayName("자기 자신과의 채팅 내역 조회 시 예외 발생")
    void getChatHistory_SelfEmail_ThrowsException() {
        assertThatThrownBy(() -> dmService.getChatHistory("same@example.com", "same@example.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("자기 자신과의 채팅 내역은 조회할 수 없습니다.");
    }

    @Test
    @DisplayName("채팅 상대 목록 조회 시 공백 또는 null 이메일 전달 시 예외 발생")
    void getChatPartners_EmptyEmail_ThrowsException() {
        assertThatThrownBy(() -> dmService.getChatPartners("   "))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("이메일 정보가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("메시지 읽음 처리 시 공백 또는 null 이메일 전달 시 예외 발생")
    void markMessagesAsRead_EmptyEmail_ThrowsException() {
        assertThatThrownBy(() -> dmService.markMessagesAsRead("   ", "partner@example.com"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("이메일 정보가 필요합니다.");
    }

    @Test
    @DisplayName("단건 메시지 읽음 처리 시 null dmId 전달 시 예외 발생")
    void markAsRead_NullDmId_ThrowsException() {
        assertThatThrownBy(() -> dmService.markAsRead(null))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("메시지 ID가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("메시지 발송 시 공백 또는 null 이메일 전달 시 예외 발생")
    void sendDM_EmptyEmail_ThrowsException() {
        assertThatThrownBy(() -> dmService.sendDM("   ", "receiver@example.com", "안녕"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("발신자 및 수신자 이메일 정보가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("페이지 조회 시 최신순 결과를 화면 표시 순서로 뒤집어 반환한다")
    void getChatHistoryPaged_ReturnsAscending() {
        // given
        User me = User.builder().id(1L).email("me@test.com").build();
        User other = User.builder().id(2L).email("other@test.com").build();
        given(userRepository.findByEmail("me@test.com")).willReturn(Optional.of(me));
        given(userRepository.findByEmail("other@test.com")).willReturn(Optional.of(other));

        DirectMessage newer = DirectMessage.builder().id(2L).content("두번째").sender(me).receiver(other).build();
        DirectMessage older = DirectMessage.builder().id(1L).content("첫번째").sender(me).receiver(other).build();
        given(dmRepository.findLatestChatHistory(
                org.mockito.ArgumentMatchers.eq(me),
                org.mockito.ArgumentMatchers.eq(other),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .willReturn(List.of(newer, older));

        // when
        List<DirectMessage> result = dmService.getChatHistory("me@test.com", "other@test.com", null, 50);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("첫번째");
        assertThat(result.get(1).getContent()).isEqualTo("두번째");
    }

    @Test
    @DisplayName("before 커서를 주면 해당 메시지 이전 구간을 조회한다")
    void getChatHistoryPaged_WithBeforeCursor() {
        // given
        User me = User.builder().id(1L).email("me@test.com").build();
        User other = User.builder().id(2L).email("other@test.com").build();
        given(userRepository.findByEmail("me@test.com")).willReturn(Optional.of(me));
        given(userRepository.findByEmail("other@test.com")).willReturn(Optional.of(other));
        given(dmRepository.findOlderChatHistory(
                org.mockito.ArgumentMatchers.eq(me),
                org.mockito.ArgumentMatchers.eq(other),
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .willReturn(List.of());

        // when
        List<DirectMessage> result = dmService.getChatHistory("me@test.com", "other@test.com", 10L, 50);

        // then
        assertThat(result).isEmpty();
        verify(dmRepository).findOlderChatHistory(
                org.mockito.ArgumentMatchers.eq(me),
                org.mockito.ArgumentMatchers.eq(other),
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("요청 size 가 상한을 넘으면 최대치로 제한된다")
    void getChatHistoryPaged_SizeCappedAtMax() {
        // given
        User me = User.builder().id(1L).email("me@test.com").build();
        User other = User.builder().id(2L).email("other@test.com").build();
        given(userRepository.findByEmail("me@test.com")).willReturn(Optional.of(me));
        given(userRepository.findByEmail("other@test.com")).willReturn(Optional.of(other));

        org.mockito.ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        given(dmRepository.findLatestChatHistory(
                org.mockito.ArgumentMatchers.eq(me),
                org.mockito.ArgumentMatchers.eq(other),
                captor.capture()))
                .willReturn(List.of());

        // when
        dmService.getChatHistory("me@test.com", "other@test.com", null, 100000);

        // then
        assertThat(captor.getValue().getPageSize())
                .isEqualTo(com.example.demo.usecase.DirectMessageUseCase.MAX_PAGE_SIZE);
    }

    @Test
    @DisplayName("페이지 조회 시 공백 이메일이면 예외가 발생한다")
    void getChatHistoryPaged_BlankEmail_ThrowsException() {
        assertThatThrownBy(() -> dmService.getChatHistory("  ", "other@test.com", null, 50))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("이메일 정보가 필요합니다.");
    }

    @Test
    @DisplayName("페이지 조회 시 자기 자신과의 대화는 조회할 수 없다")
    void getChatHistoryPaged_SelfChat_ThrowsException() {
        assertThatThrownBy(() -> dmService.getChatHistory("me@test.com", "me@test.com", null, 50))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("자기 자신과의 채팅 내역은 조회할 수 없습니다.");
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "sender=''{0}'', receiver=''{1}''")
    @org.junit.jupiter.params.provider.CsvSource(value = {
            "NULL, other@test.com",
            "'   ', other@test.com",
            "me@test.com, NULL",
            "me@test.com, '   '"
    }, nullValues = "NULL")
    @DisplayName("DM 발송 시 발신자/수신자 이메일이 비어 있으면 예외 발생")
    void sendDM_BlankEmails_ThrowsException(String sender, String receiver) {
        assertThatThrownBy(() -> dmService.sendDM(sender, receiver, "안녕하세요"))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("발신자 및 수신자 이메일 정보가 올바르지 않습니다.");
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "email1=''{0}'', email2=''{1}''")
    @org.junit.jupiter.params.provider.CsvSource(value = {
            "NULL, other@test.com",
            "'   ', other@test.com",
            "me@test.com, NULL",
            "me@test.com, '   '"
    }, nullValues = "NULL")
    @DisplayName("DM 대화 이력 조회 시 이메일이 비어 있으면 예외 발생")
    void getChatHistory_BlankEmails_ThrowsException(String email1, String email2) {
        assertThatThrownBy(() -> dmService.getChatHistory(email1, email2))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("이메일 정보가 필요합니다.");
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "current=''{0}'', partner=''{1}''")
    @org.junit.jupiter.params.provider.CsvSource(value = {
            "NULL, other@test.com",
            "'   ', other@test.com",
            "me@test.com, NULL",
            "me@test.com, '   '"
    }, nullValues = "NULL")
    @DisplayName("DM 읽음 처리 시 이메일이 비어 있으면 예외 발생")
    void markMessagesAsRead_BlankEmails_ThrowsException(String current, String partner) {
        assertThatThrownBy(() -> dmService.markMessagesAsRead(current, partner))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("이메일 정보가 필요합니다.");
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "email=''{0}''")
    @org.junit.jupiter.params.provider.CsvSource(value = {"NULL", "'   '"}, nullValues = "NULL")
    @DisplayName("DM 대화 상대 목록 조회 시 이메일이 비어 있으면 예외 발생")
    void getChatPartners_BlankEmail_ThrowsException(String email) {
        assertThatThrownBy(() -> dmService.getChatPartners(email))
                .isInstanceOf(com.example.demo.exception.CustomException.class)
                .hasMessageContaining("이메일 정보가 올바르지 않습니다.");
    }
}
