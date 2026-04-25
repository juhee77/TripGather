package com.example.demo.usecase;

import com.example.demo.domain.ChatMessage;
import java.util.List;

public interface ChatUseCase {
    com.example.demo.dto.ChatMessageResponse saveMessage(Long gatheringId, String email, String content);
    java.util.List<com.example.demo.domain.ChatMessage> getChatHistory(Long gatheringId);
}
