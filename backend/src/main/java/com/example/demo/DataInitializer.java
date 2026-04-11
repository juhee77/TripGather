package com.example.demo;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepo, 
            GatheringRepository gatheringRepo, 
            ItineraryRepository itineraryRepo, 
            GatheringMemberRepository memberRepo,
            ChatMessageRepository chatRepo,
            UserMissionRepository missionRepo,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        
        return args -> {
            if (userRepo.count() == 0) {
                userRepo.save(User.builder()
                        .name("Jihyun (지현)")
                        .email("jihyun@test.com")
                        .password(passwordEncoder.encode("pass1234"))
                        .bio("여행을 좋아하는 person")
                        .provider("local")
                        .role("ROLE_USER")
                        .profileImageUrl(null)
                        .points(1500)
                        .emailVerified(true)
                        .build());
                userRepo.save(User.builder()
                        .name("Alex")
                        .email("alex@test.com")
                        .password(passwordEncoder.encode("pass1234"))
                        .bio("러닝과 맛집 탐방을 좋아해요")
                        .provider("local")
                        .role("ROLE_USER")
                        .profileImageUrl(null)
                        .points(800)
                        .emailVerified(true)
                        .build());
            }

            User jihyun = userRepo.findByEmail("jihyun@test.com").get();
            User alex = userRepo.findByEmail("alex@test.com").get();

            if (gatheringRepo.count() == 0) {
                Gathering g1 = gatheringRepo.save(Gathering.builder()
                        .title("Weekend Trip to Busan! (부산 주말 여행! 🌊)")
                        .host(jihyun)
                        .location("Busan Station (부산역)")
                        .lat(35.1154)
                        .lng(129.0422)
                        .dates("Aug 17-18")
                        .currentJoining(1)
                        .maxJoining(6)
                        .bgImageUrl("https://images.unsplash.com/photo-1546872957-3f746681498b?auto=format&fit=crop&q=80&w=600")
                        .build());

                Gathering g2 = gatheringRepo.save(Gathering.builder()
                        .title("오늘 저녁 한강 러닝 뛸 사람~ 🏃‍♂️")
                        .host(alex)
                        .location("Banpo Hangang Park (반포 한강공원)")
                        .lat(37.5122)
                        .lng(126.9970)
                        .dates("Today 20:00")
                        .currentJoining(1)
                        .maxJoining(4)
                        .bgImageUrl("")
                        .build());

                Gathering g3 = gatheringRepo.save(Gathering.builder()
                        .title("강남 맛집 탐방 🍜")
                        .host(alex)
                        .location("Gangnam Station (강남역)")
                        .lat(37.4979)
                        .lng(127.0276)
                        .dates("Sat 18:00")
                        .currentJoining(2)
                        .maxJoining(5)
                        .category("밥/카페")
                        .bgImageUrl("https://images.unsplash.com/photo-1517457373958-b7bdd4587205?auto=format&fit=crop&q=80&w=600")
                        .build());
                
                // 멤버 관계 추가
                memberRepo.save(GatheringMember.builder().gathering(g1).user(jihyun).status(MemberStatus.APPROVED).requestedAt(LocalDateTime.now()).build());
                memberRepo.save(GatheringMember.builder().gathering(g1).user(alex).status(MemberStatus.PENDING).requestedAt(LocalDateTime.now()).build());

                memberRepo.save(GatheringMember.builder().gathering(g2).user(alex).status(MemberStatus.APPROVED).requestedAt(LocalDateTime.now()).build());
                memberRepo.save(GatheringMember.builder().gathering(g2).user(jihyun).status(MemberStatus.PENDING).requestedAt(LocalDateTime.now()).build());

                memberRepo.save(GatheringMember.builder().gathering(g3).user(alex).status(MemberStatus.APPROVED).requestedAt(LocalDateTime.now()).build());
                memberRepo.save(GatheringMember.builder().gathering(g3).user(jihyun).status(MemberStatus.APPROVED).requestedAt(LocalDateTime.now()).build());

                // 채팅 메시지 
                chatRepo.save(ChatMessage.builder().gathering(g3).sender(alex).content("안녕하세요! 강남역 어디서 뵐까요?").sentAt(LocalDateTime.now().minusMinutes(30)).build());
                chatRepo.save(ChatMessage.builder().gathering(g3).sender(jihyun).content("이번 주 11번 출구 어떠세요?").sentAt(LocalDateTime.now().minusMinutes(20)).build());
                chatRepo.save(ChatMessage.builder().gathering(g3).sender(alex).content("좋습니다 ㅎㅎ 6시까지 봬요").sentAt(LocalDateTime.now().minusMinutes(5)).build());
            }

            if (itineraryRepo.count() == 0) {
                // 서울 2박 3일 여행
                RoutePoint seoul_day1_p1 = RoutePoint.builder()
                        .dayNumber(1).dayLabel("Day 1 · 도착의 날")
                        .sequenceOrder(1).label("N서울타워")
                        .lat(37.5512).lng(126.9882).build();
                RoutePoint seoul_day1_p2 = RoutePoint.builder()
                        .dayNumber(1).dayLabel("Day 1 · 도착의 날")
                        .sequenceOrder(2).label("경복궁")
                        .lat(37.5796).lng(126.9770).build();
                RoutePoint seoul_day1_p3 = RoutePoint.builder()
                        .dayNumber(1).dayLabel("Day 1 · 도착의 날")
                        .sequenceOrder(3).label("광화문 광장")
                        .lat(37.5720).lng(126.9768).build();
                RoutePoint seoul_day2_p1 = RoutePoint.builder()
                        .dayNumber(2).dayLabel("Day 2 · 힙한 서울")
                        .sequenceOrder(1).label("성수동 카페거리")
                        .lat(37.5443).lng(127.0557).build();
                RoutePoint seoul_day2_p2 = RoutePoint.builder()
                        .dayNumber(2).dayLabel("Day 2 · 힙한 서울")
                        .sequenceOrder(2).label("DDP (동대문디자인플라자)")
                        .lat(37.5670).lng(127.0093).build();
                RoutePoint seoul_day3_p1 = RoutePoint.builder()
                        .dayNumber(3).dayLabel("Day 3 · 마지막 날")
                        .sequenceOrder(1).label("한강 반포공원")
                        .lat(37.5122).lng(126.9970).build();
                RoutePoint seoul_day3_p2 = RoutePoint.builder()
                        .dayNumber(3).dayLabel("Day 3 · 마지막 날")
                        .sequenceOrder(2).label("인천국제공항")
                        .lat(37.4602).lng(126.4407).build();

                Itinerary seoulTrip = Itinerary.builder()
                        .title("Seoul Weekend Trip (나의 주말 서울 나들이)")
                        .author("Jihyun (지현)")
                        .description("N서울타워 → 경복궁 → 성수동 → DDP → 한강 → 귀가")
                        .location("서울 중구")
                        .dates("8/17 - 8/19")
                        .bgImageUrl("https://images.unsplash.com/photo-1535191036316-2eb944f24d85?auto=format&fit=crop&q=80&w=600")
                        .routePoints(new java.util.ArrayList<>(List.of(
                                seoul_day1_p1, seoul_day1_p2, seoul_day1_p3,
                                seoul_day2_p1, seoul_day2_p2,
                                seoul_day3_p1, seoul_day3_p2
                        )))
                        .build();
                seoulTrip.getRoutePoints().forEach(rp -> rp.setItinerary(seoulTrip));
                itineraryRepo.save(seoulTrip);

                // 제주도 2박 3일 먹방 여행
                RoutePoint jeju_day1_p1 = RoutePoint.builder()
                        .dayNumber(1).dayLabel("Day 1 · 제주 도착")
                        .sequenceOrder(1).label("제주국제공항")
                        .lat(33.5113).lng(126.4930).build();
                RoutePoint jeju_day1_p2 = RoutePoint.builder()
                        .dayNumber(1).dayLabel("Day 1 · 제주 도착")
                        .sequenceOrder(2).label("고기국수 골목 (제주시)")
                        .lat(33.4996).lng(126.5312).build();
                RoutePoint jeju_day2_p1 = RoutePoint.builder()
                        .dayNumber(2).dayLabel("Day 2 · 남쪽 탐방")
                        .sequenceOrder(1).label("흑돼지 골목 (서귀포)")
                        .lat(33.2497).lng(126.5618).build();
                RoutePoint jeju_day2_p2 = RoutePoint.builder()
                        .dayNumber(2).dayLabel("Day 2 · 남쪽 탐방")
                        .sequenceOrder(2).label("성산일출봉")
                        .lat(33.4582).lng(126.9425).build();
                RoutePoint jeju_day3_p1 = RoutePoint.builder()
                        .dayNumber(3).dayLabel("Day 3 · 귀가")
                        .sequenceOrder(1).label("귤밭 카페 (한림)")
                        .lat(33.4113).lng(126.2661).build();
                RoutePoint jeju_day3_p2 = RoutePoint.builder()
                        .dayNumber(3).dayLabel("Day 3 · 귀가")
                        .sequenceOrder(2).label("제주국제공항")
                        .lat(33.5113).lng(126.4930).build();

                Itinerary jejuTrip = Itinerary.builder()
                        .title("제주도 2박 3일 먹방 코스 🍊")
                        .author("Alex")
                        .description("고기국수 → 흑돼지 → 성산일출봉 → 귤밭 카페")
                        .location("제주 제주시")
                        .dates("9/1 - 9/3")
                        .bgImageUrl("https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&q=80&w=600")
                        .routePoints(new java.util.ArrayList<>(List.of(
                                jeju_day1_p1, jeju_day1_p2,
                                jeju_day2_p1, jeju_day2_p2,
                                jeju_day3_p1, jeju_day3_p2
                        )))
                        .build();
                jejuTrip.getRoutePoints().forEach(rp -> rp.setItinerary(jejuTrip));
                itineraryRepo.save(jejuTrip);

                // User Mission 데이터 추가
                UserMission mission = UserMission.builder()
                        .user(jihyun)
                        .itinerary(seoulTrip)
                        .status(MissionStatus.ACTIVE)
                        .startedAt(LocalDateTime.now().minusDays(1))
                        .stampImageUrl("https://cdn-icons-png.flaticon.com/512/3715/3715013.png")
                        .build();

                UserMissionStep s1 = UserMissionStep.builder().userMission(mission).routePoint(seoul_day1_p1).isCompleted(true).photoUrl("").memo("타워 구경 완료!").build();
                UserMissionStep s2 = UserMissionStep.builder().userMission(mission).routePoint(seoul_day1_p2).isCompleted(true).photoUrl("").memo("한복 입고 사진 찍음").build();
                UserMissionStep s3 = UserMissionStep.builder().userMission(mission).routePoint(seoul_day1_p3).isCompleted(false).photoUrl("").memo("").build();

                mission.getSteps().addAll(List.of(s1, s2, s3));
                missionRepo.save(mission);
            }
        };
    }
}
