package com.example.demo.repository;

import com.example.demo.domain.GatheringPost;
import com.example.demo.domain.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@org.springframework.stereotype.Repository
public interface GatheringPostRepository extends JpaRepository<GatheringPost, Long> {
    List<GatheringPost> findByGatheringOrderByCreatedAtDesc(Gathering gathering);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE GatheringPost gp SET gp.deleted = true WHERE gp.id = :id")
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Long id);
}
