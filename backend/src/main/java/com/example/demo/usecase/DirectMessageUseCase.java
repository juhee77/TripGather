package com.example.demo.usecase;

import com.example.demo.domain.DirectMessage;
import java.util.List;

public interface DirectMessageUseCase {
    com.example.demo.dto.DMResponse sendDM(String senderEmail, String receiverEmail, String content);
    /** 한 번에 반환하는 기본 메시지 수 */
    int DEFAULT_PAGE_SIZE = 50;
    /** 클라이언트가 요청할 수 있는 최대 메시지 수 */
    int MAX_PAGE_SIZE = 100;

    java.util.List<com.example.demo.domain.DirectMessage> getChatHistory(String email1, String email2);

    /**
     * @param beforeId 이 ID 보다 과거의 메시지를 조회한다. null 이면 최신부터.
     * @param size     반환 개수. {@link #MAX_PAGE_SIZE} 로 제한된다.
     */
    java.util.List<com.example.demo.domain.DirectMessage> getChatHistory(String email1, String email2, Long beforeId, int size);
    void markAsRead(Long dmId);
    void markMessagesAsRead(String myEmail, String otherUserEmail);
    java.util.List<com.example.demo.domain.User> getChatPartners(String myEmail);
}
