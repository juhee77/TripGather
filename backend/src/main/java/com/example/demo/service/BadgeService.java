package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.dto.BadgeDto;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.StampRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeService {

    private final UserRepository userRepository;
    private final GatheringRepository gatheringRepository;
    private final StampRepository stampRepository;

    public List<BadgeDto> getUserBadges(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        List<BadgeDto> badges = new ArrayList<>();

        // 1. 초보 탐험가 (기본 언락)
        badges.add(BadgeDto.builder()
                .code("BEGINNER_EXPLORER")
                .name("첫 발걸음")
                .description("TripGather에 가입하고 첫 여행을 준비합니다.")
                .icon("🎒")
                .unlocked(true)
                .build());

        // 2. 열정적인 호스트 (호스팅한 모임 1개 이상)
        long hostedCount = gatheringRepository.findAll().stream()
                .filter(g -> g.getHost() != null && g.getHost().getId().equals(user.getId()))
                .count();
        badges.add(BadgeDto.builder()
                .code("PASSIONATE_HOST")
                .name("열정적인 호스트")
                .description("여행 모임을 1회 이상 직접 호스팅했습니다.")
                .icon("🚩")
                .unlocked(hostedCount >= 1)
                .build());

        // 3. 스탬프 수집가 (스탬프 1개 이상)
        long stampCount = stampRepository.countByUserId(user.getId());
        badges.add(BadgeDto.builder()
                .code("STAMP_COLLECTOR")
                .name("스탬프 수집가")
                .description("여행 체크인 스탬프를 1개 이상 수집했습니다.")
                .icon("🎟️")
                .unlocked(stampCount >= 1)
                .build());

        // 4. 포인트 부자 (1,000 포인트 이상)
        boolean hasPoints = user.getPoints() >= 1000;
        badges.add(BadgeDto.builder()
                .code("POINT_RICH")
                .name("포인트 리치")
                .description("1,000 포인트 이상을 보유하고 있습니다.")
                .icon("💎")
                .unlocked(hasPoints)
                .build());

        return badges;
    }
}
