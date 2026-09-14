package com.example.demo.controller;

import com.example.demo.dto.ChatMessageResponse;
import com.example.demo.usecase.ChatUseCase;
import com.example.demo.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class ChatController {

    private final ChatUseCase chatService;
    private final NotificationService notificationService;
    private final com.example.demo.usecase.GatheringUseCase gatheringService;
    private final com.example.demo.usecase.GatheringMemberUseCase gatheringMemberService;

    // 클라이언트가 /app/chat/{gatheringId}/send 로 메시지를 보내면 호출됨
    @MessageMapping("/chat/{gatheringId}/send")
    @SendTo("/topic/chat/{gatheringId}")
    public ChatMessageResponse sendMessage(@DestinationVariable Long gatheringId, @org.springframework.messaging.handler.annotation.Payload ChatMessageRequest request, java.security.Principal principal) {
        // 발신자는 반드시 STOMP 세션의 인증 주체에서만 얻는다.
        // (클라이언트가 보낸 senderEmail 을 신뢰하면 타인 사칭이 가능하다)
        if (principal == null) {
            log.warn("[Chat] Rejected unauthenticated message for gathering {}", gatheringId);
            return null;
        }
        String email = principal.getName();

        // Membership check: Only approved members/host can send messages
        if (!gatheringMemberService.isAuthorizedMember(gatheringId, email)) {
            log.warn("[Chat] Unauthorized chat attempt by {} for gathering {}", email, gatheringId);
            return null; 
        }

        ChatMessageResponse response = chatService.saveMessage(gatheringId, email, request.getContent());
        
        // 실시간 알림 전송 (참여자들에게 개별 SSE 알림 - 배경 알림용).
        // 알림은 부가 기능이므로, 여기서 실패하더라도 아래 @SendTo 브로드캐스트는 반드시 수행되어야 한다.
        // (과거 이 호출에서 예외가 새어 메시지가 저장만 되고 화면에 안 뜨는 문제가 있었다)
        try {
            notificationService.sendToAllMembers(gatheringId, "chat-received", response);
        } catch (Exception e) {
            log.warn("[Chat] 알림 전송 실패 (브로드캐스트는 계속): gatheringId={}", gatheringId, e);
        }

        return response;
    }

    /**
     * 채팅 내역 조회. 기본은 최신 50건이며, before 커서로 과거 메시지를 이어서 읽는다.
     *
     * @param before 이 메시지 ID 보다 과거 메시지를 조회 (위로 스크롤). 생략 시 최신부터.
     * @param size   조회 개수. 서버에서 최대치로 제한된다.
     */
    @GetMapping("/api/chat/{gatheringId}/history")
    @ResponseBody
    public List<ChatMessageResponse> getChatHistory(@PathVariable Long gatheringId,
                                                    @org.springframework.web.bind.annotation.RequestParam(required = false) Long before,
                                                    @org.springframework.web.bind.annotation.RequestParam(defaultValue = "50") int size,
                                                    java.security.Principal principal) {
        com.example.demo.domain.Gathering gathering = gatheringService.getGathering(gatheringId);
        
        // Check privacy: if not public, only members or host can view
        if (!gathering.isChatPublic()) {
            String email = (principal != null) ? principal.getName() : null;
            if (!gatheringMemberService.isAuthorizedMember(gatheringId, email)) {
                return java.util.Collections.emptyList();
            }
        }

        return chatService.getChatHistory(gatheringId, before, size);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageRequest {
        private String content;
        /** 클라이언트 호환용 필드. 서버는 이 값을 신뢰하지 않고 인증 주체를 사용한다. */
        private String senderEmail;
    }
}
