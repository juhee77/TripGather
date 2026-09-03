package com.example.demo.controller;

import com.example.demo.domain.Gathering;
import com.example.demo.dto.ChatMessageResponse;
import com.example.demo.usecase.ChatUseCase;
import com.example.demo.usecase.GatheringMemberUseCase;
import com.example.demo.usecase.GatheringUseCase;
import com.example.demo.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @MessageMapping 메서드는 MockMvc 로 도달할 수 없으므로 직접 호출로 검증한다.
 * 발신자 사칭 방지(인증 주체만 신뢰)와 비승인 멤버 차단이 핵심 검증 대상이다.
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatUseCase chatService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private GatheringUseCase gatheringService;
    @Mock
    private GatheringMemberUseCase gatheringMemberService;

    @InjectMocks
    private ChatController chatController;

    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
        principal = mock(Principal.class);
        lenient().when(principal.getName()).thenReturn("member@test.com");
    }

    @Test
    @DisplayName("승인된 멤버의 채팅 전송 성공 - 인증 주체 이메일로 저장하고 전체 알림 발송")
    void sendMessage_AuthorizedMember_Success() {
        // given
        ChatController.ChatMessageRequest request =
                new ChatController.ChatMessageRequest("안녕하세요", "spoofed@evil.com");
        ChatMessageResponse saved = ChatMessageResponse.builder()
                .id(1L).content("안녕하세요").senderEmail("member@test.com").build();
        given(gatheringMemberService.isAuthorizedMember(1L, "member@test.com")).willReturn(true);
        given(chatService.saveMessage(1L, "member@test.com", "안녕하세요")).willReturn(saved);

        // when
        ChatMessageResponse result = chatController.sendMessage(1L, request, principal);

        // then
        assertThat(result).isSameAs(saved);
        verify(notificationService).sendToAllMembers(1L, "chat-received", saved);
    }

    @Test
    @DisplayName("클라이언트가 보낸 senderEmail은 무시하고 인증 주체 이메일만 사용")
    void sendMessage_IgnoresClientSuppliedSenderEmail() {
        // given
        ChatController.ChatMessageRequest request =
                new ChatController.ChatMessageRequest("안녕하세요", "spoofed@evil.com");
        given(gatheringMemberService.isAuthorizedMember(1L, "member@test.com")).willReturn(true);
        given(chatService.saveMessage(1L, "member@test.com", "안녕하세요"))
                .willReturn(ChatMessageResponse.builder().id(1L).build());

        // when
        chatController.sendMessage(1L, request, principal);

        // then
        verify(chatService, never()).saveMessage(anyLong(), org.mockito.ArgumentMatchers.eq("spoofed@evil.com"), anyString());
    }

    @Test
    @DisplayName("인증되지 않은 채팅 전송은 null 반환하고 저장하지 않음")
    void sendMessage_NoPrincipal_ReturnsNull() {
        // given
        ChatController.ChatMessageRequest request =
                new ChatController.ChatMessageRequest("안녕하세요", null);

        // when
        ChatMessageResponse result = chatController.sendMessage(1L, request, null);

        // then
        assertThat(result).isNull();
        verify(chatService, never()).saveMessage(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("승인되지 않은 멤버의 채팅 전송은 null 반환하고 저장하지 않음")
    void sendMessage_UnauthorizedMember_ReturnsNull() {
        // given
        ChatController.ChatMessageRequest request =
                new ChatController.ChatMessageRequest("안녕하세요", null);
        given(gatheringMemberService.isAuthorizedMember(1L, "member@test.com")).willReturn(false);

        // when
        ChatMessageResponse result = chatController.sendMessage(1L, request, principal);

        // then
        assertThat(result).isNull();
        verify(chatService, never()).saveMessage(anyLong(), anyString(), anyString());
        verify(notificationService, never()).sendToAllMembers(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("공개 채팅방 이력은 비회원도 조회 가능")
    void getChatHistory_PublicChat_AccessibleToAnyone() throws Exception {
        // given
        Gathering gathering = Gathering.builder().id(1L).isChatPublic(true).build();
        given(gatheringService.getGathering(1L)).willReturn(gathering);
        given(chatService.getChatHistory(1L)).willReturn(List.of(
                ChatMessageResponse.builder().id(1L).content("안녕하세요").build()));

        // when & then
        mockMvc.perform(get("/api/chat/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("안녕하세요"));
    }

    @Test
    @DisplayName("비공개 채팅방 이력은 승인된 멤버만 조회 가능")
    void getChatHistory_PrivateChat_AuthorizedMember_ReturnsHistory() throws Exception {
        // given
        Gathering gathering = Gathering.builder().id(1L).isChatPublic(false).build();
        given(gatheringService.getGathering(1L)).willReturn(gathering);
        given(gatheringMemberService.isAuthorizedMember(1L, "member@test.com")).willReturn(true);
        given(chatService.getChatHistory(1L)).willReturn(List.of(
                ChatMessageResponse.builder().id(1L).content("비공개 대화").build()));

        // when & then
        mockMvc.perform(get("/api/chat/1/history").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("비공개 대화"));
    }

    @Test
    @DisplayName("비공개 채팅방 이력을 비회원이 조회하면 빈 목록 반환")
    void getChatHistory_PrivateChat_Unauthorized_ReturnsEmpty() throws Exception {
        // given
        Gathering gathering = Gathering.builder().id(1L).isChatPublic(false).build();
        given(gatheringService.getGathering(1L)).willReturn(gathering);
        given(gatheringMemberService.isAuthorizedMember(1L, null)).willReturn(false);

        // when & then
        mockMvc.perform(get("/api/chat/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        verify(chatService, never()).getChatHistory(anyLong());
    }
}
