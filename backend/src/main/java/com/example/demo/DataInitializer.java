package com.example.demo;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.Itinerary;
import com.example.demo.domain.RoutePoint;
import com.example.demo.domain.User;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.ItineraryRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepo, GatheringRepository gatheringRepo, ItineraryRepository itineraryRepo) {
        return args -> {
            if (userRepo.count() == 0) {
                userRepo.save(User.builder()
                        .name("Jihyun (지현)")
                        .bio("여행을 좋아하는 사람")
                        .profileImageUrl(null)
                        .points(1500)
                        .build());
                userRepo.save(User.builder()
                        .name("Alex")
                        .bio("러닝과 맛집 탐방을 좋아해요")
                        .profileImageUrl(null)
                        .points(800)
                        .build());
            }

            if (gatheringRepo.count() == 0) {
                gatheringRepo.save(Gathering.builder()
                        .title("Weekend Trip to Busan! (부산 주말 여행! 🌊)")
                        .host("Jihyun (지현)")
                        .location("Busan Station (부산역)")
                        .lat(35.1154)   // 부산역
                        .lng(129.0422)
                        .dates("Aug 17-18")
                        .currentJoining(4)
                        .maxJoining(6)
                        .bgImageUrl("https://images.unsplash.com/photo-1546872957-3f746681498b?auto=format&fit=crop&q=80&w=600")
                        .build());

                gatheringRepo.save(Gathering.builder()
                        .title("오늘 저녁 한강 러닝 뛸 사람~ 🏃‍♂️")
                        .host("Alex")
                        .location("Banpo Hangang Park (반포 한강공원)")
                        .lat(37.5122)   // 반포 한강공원
                        .lng(126.9970)
                        .dates("Today 20:00")
                        .currentJoining(2)
                        .maxJoining(4)
                        .bgImageUrl("")
                        .build());

                gatheringRepo.save(Gathering.builder()
                        .title("강남 맛집 탐방 🍜")
                        .host("Alex")
                        .location("Gangnam Station (강남역)")
                        .lat(37.4979)   // 강남역
                        .lng(127.0276)
                        .dates("Sat 18:00")
                        .currentJoining(1)
                        .maxJoining(5)
                        .category("밥/카페")
                        .bgImageUrl("https://images.unsplash.com/photo-1517457373958-b7bdd4587205?auto=format&fit=crop&q=80&w=600")
                        .build());
            }

            if (itineraryRepo.count() == 0) {
                // 서울 2박 3일 여행 — DAY별 여러 경유지
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
                        .routePoints(new java.util.ArrayList<>(List.of(
                                jeju_day1_p1, jeju_day1_p2,
                                jeju_day2_p1, jeju_day2_p2,
                                jeju_day3_p1, jeju_day3_p2
                        )))
                        .build();
                jejuTrip.getRoutePoints().forEach(rp -> rp.setItinerary(jejuTrip));
                itineraryRepo.save(jejuTrip);
            }
        };
    }
}

