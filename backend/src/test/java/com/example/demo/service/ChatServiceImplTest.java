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
        ChatMessage result = chatService.saveMessage(1L, "test@example.com", "hello");

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
        given(chatMessageRepository.findByGatheringIdOrderBySentAtAsc(1L)).willReturn(List.of(new ChatMessage(), new ChatMessage()));

        // when
        List<ChatMessage> history = chatService.getChatHistory(1L);

        // then
        assertThat(history).hasSize(2);
        verify(chatMessageRepository).findByGatheringIdOrderBySentAtAsc(1L);
    }
}
