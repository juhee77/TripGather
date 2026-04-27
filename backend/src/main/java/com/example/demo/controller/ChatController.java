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
        log.info("[Chat] Received message for gathering {}: {}", gatheringId, request.getContent());
        
        // Membership check: Only approved members/host can send messages
        String email = (principal != null) ? principal.getName() : request.getSenderEmail();
        log.info("[Chat] Sender email: {}, Principal: {}", email, (principal != null ? principal.getName() : "NULL"));

        if (!gatheringMemberService.isAuthorizedMember(gatheringId, email)) {
            log.warn("[Chat] Unauthorized chat attempt by {} for gathering {}", email, gatheringId);
            return null; 
        }

        ChatMessageResponse response = chatService.saveMessage(gatheringId, email, request.getContent());
        
        // 실시간 알림 전송 (참여자들에게 개별 SSE 알림 - 배경 알림용)
        notificationService.sendToAllMembers(gatheringId, "chat-received", response);

        return response;
    }

    @GetMapping("/api/chat/{gatheringId}/history")
    @ResponseBody
    public List<ChatMessageResponse> getChatHistory(@PathVariable Long gatheringId, java.security.Principal principal) {
        com.example.demo.domain.Gathering gathering = gatheringService.getGathering(gatheringId);
        
        // Check privacy: if not public, only members or host can view
        if (!gathering.isChatPublic()) {
            String email = (principal != null) ? principal.getName() : null;
            if (!gatheringMemberService.isAuthorizedMember(gatheringId, email)) {
                return java.util.Collections.emptyList();
            }
        }

        return chatService.getChatHistory(gatheringId).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageRequest {
        private String content;
        private String senderEmail;
    }
}
