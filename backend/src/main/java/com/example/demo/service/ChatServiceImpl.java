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

import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Collections;
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

        ChatMessage message = ChatMessage.of(content, user, gathering);

        com.example.demo.domain.ChatMessage saved = chatMessageRepository.save(message);
        return com.example.demo.dto.ChatMessageResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<com.example.demo.dto.ChatMessageResponse> getChatHistory(Long gatheringId) {
        return getChatHistory(gatheringId, null, DEFAULT_PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public List<com.example.demo.dto.ChatMessageResponse> getChatHistory(Long gatheringId, Long beforeId, int size) {
        if (gatheringId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "모임 ID가 올바르지 않습니다.");
        }
        int pageSize = Math.min(size <= 0 ? DEFAULT_PAGE_SIZE : size, MAX_PAGE_SIZE);
        PageRequest page = PageRequest.of(0, pageSize);

        // 최신순으로 한 페이지를 읽은 뒤, 화면 표시 순서(오래된 -> 최신)로 뒤집어 반환한다.
        List<ChatMessage> messages = (beforeId == null)
                ? chatMessageRepository.findLatestByGatheringId(gatheringId, page)
                : chatMessageRepository.findOlderByGatheringId(gatheringId, beforeId, page);

        List<ChatMessage> ascending = new ArrayList<>(messages);
        Collections.reverse(ascending);

        return ascending.stream()
                .map(com.example.demo.dto.ChatMessageResponse::from)
                .toList();
    }
}
