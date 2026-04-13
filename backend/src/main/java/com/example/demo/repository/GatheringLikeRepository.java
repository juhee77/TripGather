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
}
