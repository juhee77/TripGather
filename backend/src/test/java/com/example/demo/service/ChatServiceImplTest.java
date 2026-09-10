package com.example.demo.service;

import com.example.demo.domain.ChatMessage;
import com.example.demo.domain.Gathering;
import com.example.demo.domain.User;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.GatheringRepository;
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
class ChatServiceImplTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    @DisplayName("채팅 메시지 저장 성공 테스트")
    void saveMessage_Success() {
        // given
        Gathering gathering = Gathering.builder().id(1L).build();
        User user = User.builder().email("test@example.com").build();
        
        given(gatheringRepository.findById(1L)).willReturn(Optional.of(gathering));
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        
        ChatMessage savedMessage = ChatMessage.builder().content("hello").sender(user).gathering(gathering).build();
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);

        // when
        com.example.demo.dto.ChatMessageResponse result = chatService.saveMessage(1L, "test@example.com", "hello");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("hello");
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("모임이 없을 때 채팅 저장 예외 발생")
    void saveMessage_GatheringNotFound_ThrowsException() {
        // given
        given(gatheringRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> chatService.saveMessage(1L, "test@example.com", "hello"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("찾을 수 없습니다.");
    }

    @Test
    @DisplayName("채팅 히스토리 조회 성공 테스트")
    void getChatHistory_Success() {
        // given
        User sender = User.builder().name("tester").email("test@example.com").build();
        // 저장소는 최신순으로 내려주고, 서비스가 화면 표시 순서(오래된 -> 최신)로 뒤집는다.
        ChatMessage newer = ChatMessage.builder().id(2L).content("msg2").sender(sender).build();
        ChatMessage older = ChatMessage.builder().id(1L).content("msg1").sender(sender).build();
        given(chatMessageRepository.findLatestByGatheringId(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .willReturn(List.of(newer, older));

        // when
        List<com.example.demo.dto.ChatMessageResponse> history = chatService.getChatHistory(1L);

        // then
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getContent()).isEqualTo("msg1");
        assertThat(history.get(1).getContent()).isEqualTo("msg2");
    }

    @Test
    @DisplayName("before 커서를 주면 해당 메시지 이전 구간을 조회한다")
    void getChatHistory_WithBeforeCursor() {
        // given
        User sender = User.builder().name("tester").email("test@example.com").build();
        ChatMessage older = ChatMessage.builder().id(5L).content("old").sender(sender).build();
        given(chatMessageRepository.findOlderByGatheringId(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .willReturn(List.of(older));

        // when
        List<com.example.demo.dto.ChatMessageResponse> history = chatService.getChatHistory(1L, 10L, 50);

        // then
        assertThat(history).hasSize(1);
        verify(chatMessageRepository).findOlderByGatheringId(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("요청 size 가 상한을 넘으면 최대치로 제한된다")
    void getChatHistory_SizeCappedAtMax() {
        // given
        org.mockito.ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        given(chatMessageRepository.findLatestByGatheringId(
                org.mockito.ArgumentMatchers.eq(1L), captor.capture()))
                .willReturn(List.of());

        // when
        chatService.getChatHistory(1L, null, 100000);

        // then
        assertThat(captor.getValue().getPageSize())
                .isEqualTo(com.example.demo.usecase.ChatUseCase.MAX_PAGE_SIZE);
    }

    @Test
    @DisplayName("모임 ID 가 null 이면 예외가 발생한다")
    void getChatHistory_NullGatheringId_ThrowsException() {
        assertThatThrownBy(() -> chatService.getChatHistory(null, null, 50))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("모임 ID가 올바르지 않습니다.");
    }
}
