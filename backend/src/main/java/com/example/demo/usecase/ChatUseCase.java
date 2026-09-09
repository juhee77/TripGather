package com.example.demo.usecase;

public interface ChatUseCase {

    /** 한 번에 반환하는 기본 메시지 수 */
    int DEFAULT_PAGE_SIZE = 50;
    /** 클라이언트가 요청할 수 있는 최대 메시지 수 */
    int MAX_PAGE_SIZE = 100;

    com.example.demo.dto.ChatMessageResponse saveMessage(Long gatheringId, String email, String content);

    /** 최신 메시지 기본 페이지를 반환한다. */
    java.util.List<com.example.demo.dto.ChatMessageResponse> getChatHistory(Long gatheringId);

    /**
     * @param beforeId 이 ID 보다 과거의 메시지를 조회한다. null 이면 최신부터.
     * @param size     반환 개수. {@link #MAX_PAGE_SIZE} 로 제한된다.
     */
    java.util.List<com.example.demo.dto.ChatMessageResponse> getChatHistory(Long gatheringId, Long beforeId, int size);
}
