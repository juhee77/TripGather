package com.example.demo.service;

import com.example.demo.domain.DirectMessage;
import com.example.demo.domain.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.DirectMessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.usecase.DirectMessageUseCase;
import com.example.demo.dto.DMResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectMessageServiceImpl implements DirectMessageUseCase {

    private final DirectMessageRepository dmRepository;
    private final UserRepository userRepository;
    private final ProfanityFilterService profanityFilterService;

    @Override
    @Transactional
    public DMResponse sendDM(String senderEmail, String receiverEmail, String content) {
        if (senderEmail != null && senderEmail.equals(receiverEmail)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "자기 자신에게는 메시지를 발송할 수 없습니다.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "메시지 내용을 입력해주세요.");
        }

        profanityFilterService.validateText(content);
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        DirectMessage dm = DirectMessage.of(sender, receiver, content);

        DirectMessage saved = dmRepository.save(dm);
        return DMResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirectMessage> getChatHistory(String email1, String email2) {
        User user1 = userRepository.findByEmail(email1)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User user2 = userRepository.findByEmail(email2)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return dmRepository.findChatHistory(user1, user2);
    }

    @Override
    @Transactional
    public void markAsRead(Long dmId) {
        DirectMessage dm = dmRepository.findById(dmId)
                .orElseThrow(() -> new CustomException(ErrorCode.DM_NOT_FOUND)); 
        dm.setRead(true);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String currentEmail, String partnerEmail) {
        if (currentEmail != null && currentEmail.equals(partnerEmail)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "자기 자신과의 대화방은 처리할 수 없습니다.");
        }
        User me = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User other = userRepository.findByEmail(partnerEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        List<DirectMessage> unread = dmRepository.findUnreadMessages(other, me);
        unread.forEach(dm -> dm.setRead(true));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getChatPartners(String myEmail) {
        return dmRepository.findChatPartners(myEmail);
    }
}
