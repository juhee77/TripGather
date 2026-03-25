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

    // 클라이언트가 /app/chat/{gatheringId}/send 로 메시지를 보내면 호출됨
    @MessageMapping("/chat/{gatheringId}/send")
    @SendTo("/topic/chat/{gatheringId}")
    public ChatMessageResponse sendMessage(@DestinationVariable Long gatheringId, ChatMessageRequest request) {
        com.example.demo.domain.ChatMessage saved = chatService.saveMessage(gatheringId, request.getSenderEmail(), request.getContent());
        
        ChatMessageResponse response = ChatMessageResponse.from(saved);

        // 실시간 알림 전송 (참여자들에게)
        Gathering gathering = saved.getGathering();
        if (gathering != null && gathering.getMembers() != null) {
            for (GatheringMember member : gathering.getMembers()) {
                String receiverEmail = member.getUser().getEmail();
                // 발신자 본인은 제외
                if (!receiverEmail.equals(request.getSenderEmail())) {
                    notificationService.send(receiverEmail, "chat-received", response);
                }
            }
            // 호스트에게도 알림 (호스트가 멤버에 포함되어 있는지 확인 필요)
            String hostEmail = gathering.getHost().getEmail();
            if (!hostEmail.equals(request.getSenderEmail())) {
                notificationService.send(hostEmail, "chat-received", response);
            }
        }

        return response;
    }

    @GetMapping("/api/chat/{gatheringId}/history")
    @ResponseBody
    public List<ChatMessageResponse> getChatHistory(@PathVariable Long gatheringId) {
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
