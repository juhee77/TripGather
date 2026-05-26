package com.example.demo.repository;

import com.example.demo.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByGatheringIdOrderByCreatedAtAsc(Long gatheringId);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Comment c SET c.deleted = true WHERE c.id = :id")
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Long id);
}
