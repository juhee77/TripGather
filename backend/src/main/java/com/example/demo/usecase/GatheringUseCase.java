package com.example.demo.usecase;

import com.example.demo.domain.Gathering;
import java.util.List;

public interface GatheringUseCase {
    List<Gathering> getAllGatherings(String location);
    Gathering createGathering(Gathering gathering);
    Gathering joinGathering(Long id);
    void approveMember(Long gatheringId, Long userId);
    void rejectMember(Long gatheringId, Long userId);
    Gathering updateGathering(Long id, Gathering updateData);
    void deleteGathering(Long id);
    List<Gathering> getJoinedGatherings();
    List<Gathering> getHostedGatherings();
}
