package com.example.demo.controller;

import com.example.demo.dto.ChatMessageResponse;
import com.example.demo.usecase.ChatUseCase;
import com.example.demo.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatService;
    private final NotificationService notificationService;
    private final com.example.demo.usecase.GatheringUseCase gatheringService;

    // 클라이언트가 /app/chat/{gatheringId}/send 로 메시지를 보내면 호출됨
    @MessageMapping("/chat/{gatheringId}/send")
    @SendTo("/topic/chat/{gatheringId}")
    public ChatMessageResponse sendMessage(@DestinationVariable Long gatheringId, ChatMessageRequest request, java.security.Principal principal) {
        // Membership check: Only approved members/host can send messages
        String email = (principal != null) ? principal.getName() : request.getSenderEmail();
        if (!gatheringService.isAuthorizedMember(gatheringId, email)) {
            // In a real app, we might throw an exception or send an error message back via a private topic
            // For now, we'll just not save/broadcast or allow it if it's public (but usually chat is for members)
            // Let's restrict to members only for safety.
            return null; 
        }

        com.example.demo.domain.ChatMessage saved = chatService.saveMessage(gatheringId, email, request.getContent());
        
        ChatMessageResponse response = ChatMessageResponse.from(saved);

        // 실시간 알림 전송 (참여자들에게)
        com.example.demo.domain.Gathering gathering = saved.getGathering();
        if (gathering != null && gathering.getMembers() != null) {
            for (com.example.demo.domain.GatheringMember member : gathering.getMembers()) {
                String receiverEmail = member.getUser().getEmail();
                if (!receiverEmail.equals(email)) {
                    notificationService.send(receiverEmail, "chat-received", response);
                }
            }
            String hostEmail = gathering.getHost().getEmail();
            if (!hostEmail.equals(email)) {
                notificationService.send(hostEmail, "chat-received", response);
            }
        }

        return response;
    }

    @GetMapping("/api/chat/{gatheringId}/history")
    @ResponseBody
    public List<ChatMessageResponse> getChatHistory(@PathVariable Long gatheringId, java.security.Principal principal) {
        com.example.demo.domain.Gathering gathering = gatheringService.getGathering(gatheringId);
        
        // Check privacy: if not public, only members or host can view
        if (!gathering.isChatPublic()) {
            String email = (principal != null) ? principal.getName() : null;
            if (!gatheringService.isAuthorizedMember(gatheringId, email)) {
                return java.util.Collections.emptyList();
            }
        }

        return chatService.getChatHistory(gatheringId).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Data
    public static class ChatMessageRequest {
        private String content;
        private String senderEmail;
    }
}
