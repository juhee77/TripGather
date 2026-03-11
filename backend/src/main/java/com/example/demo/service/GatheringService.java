package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.repository.GatheringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatheringService {
    
    private final GatheringRepository gatheringRepository;

    @Transactional(readOnly = true)
    public List<Gathering> getAllGatherings() {
        return gatheringRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Gathering createGathering(Gathering gathering) {
        return gatheringRepository.save(gathering);
    }

    @Transactional
    public Gathering joinGathering(Long id) {
        Gathering gathering = gatheringRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid gathering ID"));
        
        if (gathering.getCurrentJoining() < gathering.getMaxJoining()) {
            gathering.setCurrentJoining(gathering.getCurrentJoining() + 1);
        }
        return gathering;
    }
}
