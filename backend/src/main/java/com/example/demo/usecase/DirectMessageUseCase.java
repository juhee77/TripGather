package com.example.demo.usecase;

import com.example.demo.domain.DirectMessage;
import java.util.List;

public interface DirectMessageUseCase {
    com.example.demo.dto.DMResponse sendDM(String senderEmail, String receiverEmail, String content);
    java.util.List<com.example.demo.domain.DirectMessage> getChatHistory(String email1, String email2);
    void markAsRead(Long dmId);
    void markMessagesAsRead(String myEmail, String otherUserEmail);
    java.util.List<com.example.demo.domain.User> getChatPartners(String myEmail);
}
