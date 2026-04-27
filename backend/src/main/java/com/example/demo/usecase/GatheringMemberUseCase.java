package com.example.demo.usecase;

import com.example.demo.domain.Gathering;
import java.util.List;

public interface GatheringMemberUseCase {
    Gathering joinGathering(Long id);
    void approveMember(Long gatheringId, Long userId);
    void rejectMember(Long gatheringId, Long userId);
    void leaveGathering(Long id);
    void inviteMember(Long gatheringId, Long userId);
    boolean isAuthorizedMember(Long gatheringId, String email);
    List<Gathering> getJoinedGatherings();
}
