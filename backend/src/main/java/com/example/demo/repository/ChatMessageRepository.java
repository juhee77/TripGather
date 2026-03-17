package com.example.demo.repository;

import com.example.demo.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByGatheringIdOrderBySentAtAsc(Long gatheringId);
}
