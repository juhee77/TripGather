package com.example.demo.repository;

import com.example.demo.domain.GatheringPost;
import com.example.demo.domain.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GatheringPostRepository extends JpaRepository<GatheringPost, Long> {
    List<GatheringPost> findByGatheringOrderByCreatedAtDesc(Gathering gathering);
}
