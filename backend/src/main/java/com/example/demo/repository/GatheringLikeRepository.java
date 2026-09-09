package com.example.demo.repository;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringLike;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GatheringLikeRepository extends JpaRepository<GatheringLike, Long> {
    Optional<GatheringLike> findByUserAndGathering(User user, Gathering gathering);
    boolean existsByUserAndGathering(User user, Gathering gathering);
    long countByGathering(Gathering gathering);

    /** 피드 렌더링용: 사용자가 좋아요한 모임 ID 를 한 번에 읽는다. */
    @org.springframework.data.jpa.repository.Query(
            "SELECT gl.gathering.id FROM GatheringLike gl WHERE gl.user.id = :userId")
    java.util.List<Long> findLikedGatheringIdsByUserId(
            @org.springframework.data.repository.query.Param("userId") Long userId);
}
