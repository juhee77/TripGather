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
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.usecase.ChatUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatUseCase {

    private final ChatMessageRepository chatMessageRepository;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;

    @Transactional
    public com.example.demo.dto.ChatMessageResponse saveMessage(Long gatheringId, String email, String content) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChatMessage message = ChatMessage.create(content, user, gathering);

        com.example.demo.domain.ChatMessage saved = chatMessageRepository.save(message);
        return com.example.demo.dto.ChatMessageResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<com.example.demo.dto.ChatMessageResponse> getChatHistory(Long gatheringId) {
        return chatMessageRepository.findByGatheringIdOrderBySentAtAsc(gatheringId)
                .stream()
                .map(com.example.demo.dto.ChatMessageResponse::from)
                .toList();
    }
}
