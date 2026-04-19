package com.example.demo.dto;

import com.example.demo.domain.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Long id;
    private String content;
    private String senderName;
    private String senderEmail;
    private String sentAt;

    public static ChatMessageResponse from(ChatMessage message) {
        if (message == null) return null;
        return ChatMessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderName(message.getSender().getName())
                .senderEmail(message.getSender().getEmail())
                .sentAt(message.getSentAt() != null ? message.getSentAt().toString() : java.time.LocalDateTime.now().toString())
                .build();
    }
}
