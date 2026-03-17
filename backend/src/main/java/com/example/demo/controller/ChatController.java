package com.example.demo.controller;

import com.example.demo.domain.ChatMessage;
import com.example.demo.service.ChatService;
import lombok.Data;
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
public class ChatController {

    private final ChatService chatService;

    // 클라이언트가 /app/chat/{gatheringId}/send 로 메시지를 보내면 호출됨
    @MessageMapping("/chat/{gatheringId}/send")
    @SendTo("/topic/chat/{gatheringId}")
    public ChatMessageDTO sendMessage(@DestinationVariable Long gatheringId, ChatMessageRequest request) {
        ChatMessage saved = chatService.saveMessage(gatheringId, request.getSenderEmail(), request.getContent());
        
        return new ChatMessageDTO(
                saved.getId(),
                saved.getContent(),
                saved.getSender().getName(),
                saved.getSender().getEmail(),
                saved.getSentAt().toString()
        );
    }

    @GetMapping("/api/chat/{gatheringId}/history")
    @ResponseBody
    public List<ChatMessageDTO> getChatHistory(@PathVariable Long gatheringId) {
        return chatService.getChatHistory(gatheringId).stream()
                .map(m -> new ChatMessageDTO(
                        m.getId(),
                        m.getContent(),
                        m.getSender().getName(),
                        m.getSender().getEmail(),
                        m.getSentAt().toString()
                )).toList();
    }

    @Data
    public static class ChatMessageRequest {
        private String content;
        private String senderEmail;
    }

    @Data
    @lombok.AllArgsConstructor
    public static class ChatMessageDTO {
        private Long id;
        private String content;
        private String senderName;
        private String senderEmail;
        private String sentAt;
    }
}
