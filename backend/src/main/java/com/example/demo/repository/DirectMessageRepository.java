package com.example.demo.repository;

import com.example.demo.domain.DirectMessage;
import com.example.demo.domain.User;
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

    @Query("SELECT dm FROM DirectMessage dm WHERE dm.sender = :sender AND dm.receiver = :receiver AND dm.isRead = false")
    List<DirectMessage> findUnreadMessages(@Param("sender") User sender, @Param("receiver") User receiver);

    @Query("SELECT DISTINCT u FROM User u WHERE u IN " +
           "(SELECT dm.receiver FROM DirectMessage dm WHERE dm.sender.email = :email) OR " +
           "u IN (SELECT dm.sender FROM DirectMessage dm WHERE dm.receiver.email = :email)")
    List<User> findChatPartners(@Param("email") String email);

    List<DirectMessage> findByReceiverAndIsReadFalse(User receiver);
}
