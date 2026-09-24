package com.example.demo.repository;

import com.example.demo.domain.Gathering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GatheringRepository extends JpaRepository<Gathering, Long>, GatheringRepositoryCustom {
    List<Gathering> findAllByOrderByCreatedAtDesc();
    List<Gathering> findAllByLocationContainingIgnoreCaseOrderByCreatedAtDesc(String location);
    List<Gathering> findByHostEmailOrderByCreatedAtDesc(String email);
    List<Gathering> findTop5ByDeletedFalseOrderByLikeCountDescCreatedAtDesc();

    long countByHostId(Long hostId);

    /**
     * 정원 확인·갱신을 위해 모임 행을 잠그고 읽는다.
     *
     * 정원 검사와 currentJoining 갱신 사이에 다른 트랜잭션이 끼어들면
     * 두 요청이 같은 승인자 수를 읽고 둘 다 통과해 정원을 넘길 수 있다.
     * 포인트 적립이 UserRepository 에서 같은 방식으로 보호되고 있다.
     */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT g FROM Gathering g WHERE g.id = :id")
    java.util.Optional<Gathering> findByIdWithPessimisticLock(
            @org.springframework.data.repository.query.Param("id") Long id);

    /**
     * 이 여정을 링크한 모임의 호스트이거나 승인된 크루인지 검사한다.
     * 비공개 여정이라도 자신이 참여 중인 모임에 걸려 있으면 볼 수 있어야 한다.
     */
    @org.springframework.data.jpa.repository.Query(
            "SELECT COUNT(g) > 0 FROM Gathering g LEFT JOIN g.members m "
            + "WHERE g.linkedItinerary.id = :itineraryId AND ("
            + "  g.host.email = :email "
            + "  OR (m.user.email = :email AND m.status = com.example.demo.domain.MemberStatus.APPROVED)"
            + ")")
    boolean isVisibleThroughGathering(
            @org.springframework.data.repository.query.Param("itineraryId") Long itineraryId,
            @org.springframework.data.repository.query.Param("email") String email);

    /** 알림 수신자(호스트) 이메일만 조회한다. LAZY 프록시 접근을 피하기 위한 프로젝션. */
    @org.springframework.data.jpa.repository.Query(
            "SELECT g.host.email FROM Gathering g WHERE g.id = :gatheringId AND g.host.email IS NOT NULL")
    java.util.Optional<String> findHostEmailById(
            @org.springframework.data.repository.query.Param("gatheringId") Long gatheringId);
    
    @org.springframework.data.jpa.repository.Query("SELECT g FROM Gathering g JOIN g.members m WHERE m.user.email = :email AND m.status = 'APPROVED' ORDER BY g.createdAt DESC")
    List<Gathering> findJoinedGatherings(String email);

    @org.springframework.data.jpa.repository.Query("SELECT gl.gathering FROM GatheringLike gl WHERE gl.user.email = :email AND gl.gathering.deleted = false ORDER BY gl.id DESC")
    List<Gathering> findLikedGatheringsByEmail(String email);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Gathering g SET g.deleted = true WHERE g.id = :id")
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Long id);
}
