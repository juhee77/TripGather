package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.User;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatheringService {
    
    private final GatheringRepository gatheringRepository;
    private final UserService userService;
    private final com.example.demo.repository.UserRepository userRepository;

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
        
        // 현재 로그인한 유저 정보 가져오기 (SecurityContextHolder 사용 권장)
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!gathering.getMembers().contains(user)) {
            if (gathering.getMembers().size() < gathering.getMaxJoining()) {
                gathering.getMembers().add(user);
                user.getJoinedGatherings().add(gathering);
                gathering.setCurrentJoining(gathering.getMembers().size());
            } else {
                throw new IllegalStateException("모임 정원이 초과되었습니다.");
            }
        }
        
        return gathering;
    }
}
