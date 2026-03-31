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

    @Query("SELECT DISTINCT CASE WHEN dm.sender.email = :email THEN dm.receiver ELSE dm.sender END " +
           "FROM DirectMessage dm WHERE dm.sender.email = :email OR dm.receiver.email = :email")
    List<User> findChatPartners(@Param("email") String email);

    List<DirectMessage> findByReceiverAndIsReadFalse(User receiver);
}
