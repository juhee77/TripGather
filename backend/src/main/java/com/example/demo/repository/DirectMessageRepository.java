package com.example.demo.repository;

import com.example.demo.domain.DirectMessage;
import com.example.demo.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    @Query("SELECT dm FROM DirectMessage dm WHERE " +
           "(dm.sender = :user1 AND dm.receiver = :user2) OR " +
           "(dm.sender = :user2 AND dm.receiver = :user1) " +
           "ORDER BY dm.sentAt ASC")
    List<DirectMessage> findChatHistory(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 최신 메시지부터 한 페이지를 읽는다.
     * sender/receiver 를 fetch join 해 DTO 변환 시 발생하던 N+1 을 제거한다.
     */
    @Query("SELECT dm FROM DirectMessage dm JOIN FETCH dm.sender JOIN FETCH dm.receiver WHERE " +
           "(dm.sender = :user1 AND dm.receiver = :user2) OR " +
           "(dm.sender = :user2 AND dm.receiver = :user1) " +
           "ORDER BY dm.id DESC")
    List<DirectMessage> findLatestChatHistory(@Param("user1") User user1, @Param("user2") User user2, Pageable pageable);

    /** 커서(beforeId) 이전의 과거 메시지를 한 페이지 읽는다. */
    @Query("SELECT dm FROM DirectMessage dm JOIN FETCH dm.sender JOIN FETCH dm.receiver WHERE " +
           "((dm.sender = :user1 AND dm.receiver = :user2) OR " +
           "(dm.sender = :user2 AND dm.receiver = :user1)) AND dm.id < :beforeId " +
           "ORDER BY dm.id DESC")
    List<DirectMessage> findOlderChatHistory(@Param("user1") User user1, @Param("user2") User user2,
                                             @Param("beforeId") Long beforeId, Pageable pageable);

    @Query("SELECT dm FROM DirectMessage dm WHERE dm.sender = :sender AND dm.receiver = :receiver AND dm.isRead = false")
    List<DirectMessage> findUnreadMessages(@Param("sender") User sender, @Param("receiver") User receiver);

    @Query("SELECT DISTINCT u FROM User u WHERE u IN " +
           "(SELECT dm.receiver FROM DirectMessage dm WHERE dm.sender.email = :email) OR " +
           "u IN (SELECT dm.sender FROM DirectMessage dm WHERE dm.receiver.email = :email)")
    List<User> findChatPartners(@Param("email") String email);

    List<DirectMessage> findByReceiverAndIsReadFalse(User receiver);
}
