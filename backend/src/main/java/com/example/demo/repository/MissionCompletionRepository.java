package com.example.demo.repository;

import com.example.demo.domain.MissionCompletion;
import com.example.demo.domain.MissionCompletionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MissionCompletionRepository extends JpaRepository<MissionCompletion, Long> {

    Optional<MissionCompletion> findByMissionIdAndUserId(Long missionId, Long userId);

    /**
     * 미션 목록 화면은 미션마다 인증 현황을 함께 보여준다.
     * 미션 개수만큼 조회가 나가지 않도록 한 번에 긁어온 뒤 메모리에서 묶는다.
     */
    List<MissionCompletion> findByMissionIdIn(List<Long> missionIds);

    List<MissionCompletion> findByMissionIdInAndStatusOrderByCreatedAtAsc(
            List<Long> missionIds, MissionCompletionStatus status);

    List<MissionCompletion> findByMissionIdOrderByCreatedAtAsc(Long missionId);
}
