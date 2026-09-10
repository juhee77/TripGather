package com.example.demo.repository;

import com.example.demo.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 최신 메시지부터 한 페이지를 읽는다.
     * sender 를 fetch join 해 DTO 변환 시 발생하던 N+1 을 제거한다.
     */
    @Query("SELECT cm FROM ChatMessage cm JOIN FETCH cm.sender "
            + "WHERE cm.gathering.id = :gatheringId ORDER BY cm.id DESC")
    List<ChatMessage> findLatestByGatheringId(@Param("gatheringId") Long gatheringId, Pageable pageable);

    /**
     * 커서(beforeId) 이전의 과거 메시지를 한 페이지 읽는다. (위로 스크롤)
     */
    @Query("SELECT cm FROM ChatMessage cm JOIN FETCH cm.sender "
            + "WHERE cm.gathering.id = :gatheringId AND cm.id < :beforeId ORDER BY cm.id DESC")
    List<ChatMessage> findOlderByGatheringId(@Param("gatheringId") Long gatheringId,
                                             @Param("beforeId") Long beforeId,
                                             Pageable pageable);
}
