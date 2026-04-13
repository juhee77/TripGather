package com.example.demo.repository;

import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {
    Optional<GatheringMember> findByGatheringIdAndUserId(Long gatheringId, Long userId);
    long countByGatheringIdAndStatus(Long gatheringId, MemberStatus status);
    boolean existsByGatheringIdAndUserEmailAndStatus(Long gatheringId, String email, MemberStatus status);
}
