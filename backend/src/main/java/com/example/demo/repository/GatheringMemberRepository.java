package com.example.demo.repository;

import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {
    Optional<GatheringMember> findByGatheringIdAndUserId(Long gatheringId, Long userId);
    java.util.List<GatheringMember> findByGatheringId(Long gatheringId);

    /**
     * 알림 수신자(승인된 크루) 이메일만 조회한다.
     * 엔티티를 들고 오면 LAZY 프록시를 트랜잭션 밖에서 건드리게 되어
     * LazyInitializationException 이 나고, user 마다 추가 쿼리(N+1)도 발생한다.
     */
    @org.springframework.data.jpa.repository.Query(
            "SELECT m.user.email FROM GatheringMember m "
            + "WHERE m.gathering.id = :gatheringId AND m.status = com.example.demo.domain.MemberStatus.APPROVED "
            + "AND m.user.email IS NOT NULL")
    java.util.List<String> findApprovedMemberEmails(
            @org.springframework.data.repository.query.Param("gatheringId") Long gatheringId);
    long countByGatheringIdAndStatus(Long gatheringId, MemberStatus status);
    boolean existsByGatheringIdAndUserEmailAndStatus(Long gatheringId, String email, MemberStatus status);
}
