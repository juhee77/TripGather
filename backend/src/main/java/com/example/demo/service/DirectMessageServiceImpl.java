package com.example.demo.service;

import com.example.demo.domain.DirectMessage;
import com.example.demo.domain.User;
import com.example.demo.repository.DirectMessageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.usecase.DirectMessageUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectMessageServiceImpl implements DirectMessageUseCase {

    private final DirectMessageRepository dmRepository;
    private final UserRepository userRepository;

    @Transactional
    public DirectMessage sendDM(String senderEmail, String receiverEmail, String content) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        DirectMessage dm = DirectMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();

        return dmRepository.save(dm);
    }

    public List<DirectMessage> getChatHistory(String email1, String email2) {
        User user1 = userRepository.findByEmail(email1)
                .orElseThrow(() -> new RuntimeException("User not found: " + email1));
        User user2 = userRepository.findByEmail(email2)
                .orElseThrow(() -> new RuntimeException("User not found: " + email2));

        return dmRepository.findChatHistory(user1, user2);
    }

    @Transactional
    public void markAsRead(Long dmId) {
        dmRepository.findById(dmId).ifPresent(dm -> dm.setRead(true));
    }

    @Transactional
    public void markMessagesAsRead(String myEmail, String otherUserEmail) {
        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + myEmail));
        User other = userRepository.findByEmail(otherUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + otherUserEmail));

        List<DirectMessage> unreadMessages = dmRepository.findByReceiverAndIsReadFalse(me);
        unreadMessages.forEach(dm -> {
            if (dm.getSender().equals(other)) {
                dm.setRead(true);
            }
        });
    }
}
