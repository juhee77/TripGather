package com.example.demo.usecase;

import com.example.demo.domain.DirectMessage;
import java.util.List;

public interface DirectMessageUseCase {
    DirectMessage sendDM(String senderEmail, String receiverEmail, String content);
    List<DirectMessage> getChatHistory(String email1, String email2);
    void markAsRead(Long dmId);
    void markMessagesAsRead(String myEmail, String otherUserEmail);
    List<com.example.demo.domain.User> getChatPartners(String myEmail);
}
