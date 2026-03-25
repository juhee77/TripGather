package com.example.demo.usecase;

import com.example.demo.domain.ChatMessage;
import java.util.List;

public interface ChatUseCase {
    ChatMessage saveMessage(Long gatheringId, String email, String content);
    List<ChatMessage> getChatHistory(Long gatheringId);
}
