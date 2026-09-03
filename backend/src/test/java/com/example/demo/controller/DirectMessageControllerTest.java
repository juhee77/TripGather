package com.example.demo.controller;

import com.example.demo.domain.DirectMessage;
import com.example.demo.domain.User;
import com.example.demo.dto.DMResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import com.example.demo.usecase.DirectMessageUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @MessageMapping 메서드는 MockMvc 로 도달할 수 없으므로 직접 호출로 검증한다.
 * 클라이언트가 보낸 senderEmail 을 신뢰하지 않는지가 핵심 검증 대상이다.
 */
@ExtendWith(MockitoExtension.class)
class DirectMessageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DirectMessageUseCase dmService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private DirectMessageController directMessageController;

    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(directMessageController).build();
        principal = mock(Principal.class);
        lenient().when(principal.getName()).thenReturn("me@test.com");
    }

    @Test
    @DisplayName("DM 전송 성공 - 발신자/수신자 양쪽 토픽과 SSE 알림으로 전파")
    void sendDM_Success() {
        // given
        DirectMessageController.DMRequest request =
                new DirectMessageController.DMRequest("spoofed@evil.com", "other@test.com", "안녕하세요");
        DMResponse response = DMResponse.builder()
                .id(1L).content("안녕하세요")
                .senderEmail("me@test.com").receiverEmail("other@test.com").build();
        given(dmService.sendDM("me@test.com", "other@test.com", "안녕하세요")).willReturn(response);

        // when
        directMessageController.sendDM(request, principal);

        // then
        verify(messagingTemplate).convertAndSend("/topic/dm/other@test.com", response);
        verify(messagingTemplate).convertAndSend("/topic/dm/me@test.com", response);
        verify(notificationService).send("other@test.com", "dm-received", response);
    }

    @Test
    @DisplayName("클라이언트가 보낸 senderEmail은 무시하고 인증 주체 이메일만 사용")
    void sendDM_IgnoresClientSuppliedSenderEmail() {
        // given
        DirectMessageController.DMRequest request =
                new DirectMessageController.DMRequest("spoofed@evil.com", "other@test.com", "안녕하세요");
        given(dmService.sendDM("me@test.com", "other@test.com", "안녕하세요"))
                .willReturn(DMResponse.builder().id(1L)
                        .senderEmail("me@test.com").receiverEmail("other@test.com").build());

        // when
        directMessageController.sendDM(request, principal);

        // then
        verify(dmService, never()).sendDM(org.mockito.ArgumentMatchers.eq("spoofed@evil.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("인증되지 않은 DM 전송은 무시")
    void sendDM_NoPrincipal_DoesNothing() {
        // given
        DirectMessageController.DMRequest request =
                new DirectMessageController.DMRequest(null, "other@test.com", "안녕하세요");

        // when
        directMessageController.sendDM(request, null);

        // then
        verify(dmService, never()).sendDM(anyString(), anyString(), anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    @DisplayName("DM 대화 이력 조회 API 성공")
    void getChatHistory_Success() throws Exception {
        // given
        User me = User.builder().id(1L).email("me@test.com").name("나").build();
        User other = User.builder().id(2L).email("other@test.com").name("상대").build();
        DirectMessage dm = DirectMessage.builder()
                .id(1L).sender(me).receiver(other).content("안녕하세요")
                .sentAt(LocalDateTime.now()).build();
        given(dmService.getChatHistory("me@test.com", "other@test.com")).willReturn(List.of(dm));

        // when & then
        mockMvc.perform(get("/api/dm/history/other@test.com").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("안녕하세요"))
                .andExpect(jsonPath("$[0].senderEmail").value("me@test.com"));
    }

    @Test
    @DisplayName("DM 대화 상대 목록 조회 API 성공")
    void getMyChatPartners_Success() throws Exception {
        // given
        User other = User.builder().id(2L).email("other@test.com").name("상대").build();
        given(dmService.getChatPartners("me@test.com")).willReturn(List.of(other));

        // when & then
        mockMvc.perform(get("/api/dm/partners").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("other@test.com"));
    }

    @Test
    @DisplayName("DM 읽음 처리 API는 상대방에게 읽음 알림을 전파")
    void markAsRead_NotifiesSender() throws Exception {
        // when & then
        mockMvc.perform(put("/api/dm/read/other@test.com").principal(principal))
                .andExpect(status().isOk());

        verify(dmService).markMessagesAsRead("me@test.com", "other@test.com");
        verify(messagingTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("/topic/dm/read/other@test.com"),
                (Object) any(DirectMessageController.ReadNotification.class));
    }
}
