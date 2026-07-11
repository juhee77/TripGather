package com.example.demo.service;

import com.example.demo.domain.Stamp;
import com.example.demo.dto.StampResponse;
import com.example.demo.repository.StampRepository;
import com.example.demo.security.SecurityService;
import com.example.demo.usecase.StampUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StampService implements StampUseCase {

    private final StampRepository stampRepository;
    private final SecurityService securityService;
    private final PointService pointService;

    @Override
    @Transactional(readOnly = true)
    public List<StampResponse> getMyStamps() {
        String email = securityService.getCurrentUserEmail();
        List<Stamp> stamps = stampRepository.findByUserEmailOrderByCompletedAtDesc(email);
        return stamps.stream()
                .map(StampResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void awardStamp(Long userId, Long gatheringId, String title, String stampImageUrl) {
        // Delegate to pointService to ensure user's stampsCount is atomically updated via pessimistic lock
        pointService.addPoints(userId, 0, 1, title, gatheringId, stampImageUrl);
    }
}
