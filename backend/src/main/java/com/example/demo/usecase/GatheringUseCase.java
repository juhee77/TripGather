package com.example.demo.usecase;

import com.example.demo.domain.Gathering;
import java.util.List;

public interface GatheringUseCase {
    List<Gathering> getAllGatherings(String location);
    List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly);
    Gathering createGathering(Gathering gathering);
    Gathering updateGathering(Long id, Gathering updateData);
    void deleteGathering(Long id);
    List<Gathering> getHostedGatherings();
    Gathering getGathering(Long id);
    void likeGathering(Long id);
    boolean isLikedByUser(Long gatheringId, String email);
}
