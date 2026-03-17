package com.example.demo.service;

import com.example.demo.domain.ChatMessage;
import com.example.demo.domain.Gathering;
import com.example.demo.domain.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessage saveMessage(Long gatheringId, String email, String content) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid gathering ID"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ChatMessage message = ChatMessage.builder()
                .content(content)
                .sender(user)
                .gathering(gathering)
                .build();

        return chatMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getChatHistory(Long gatheringId) {
        return chatMessageRepository.findByGatheringIdOrderBySentAtAsc(gatheringId);
    }
}
