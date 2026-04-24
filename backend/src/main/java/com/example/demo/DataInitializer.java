package com.example.demo;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

            User jihyun = userRepo.findByEmail("jihyun@test.com").orElse(null);
            User alex = userRepo.findByEmail("alex@test.com").orElse(null);

            if (gatheringRepo.count() == 0 && jihyun != null && alex != null) {
                Gathering g1 = gatheringRepo.save(Gathering.builder()
                        .title("Weekend Trip to Busan! (부산 주말 여행! 🌊)")
                        .host(jihyun)
                        .location("Busan Station (부산역)")
                        .lat(35.1154)
                        .lng(129.0422)
                        .startDate(java.time.LocalDate.now().plusDays(10))
                        .endDate(java.time.LocalDate.now().plusDays(11))
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
                        .startDate(java.time.LocalDate.now())
                        .endDate(java.time.LocalDate.now())
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
                        .startDate(java.time.LocalDate.now().plusDays(2))
                        .endDate(java.time.LocalDate.now().plusDays(2))
                        .currentJoining(2)
                        .maxJoining(5)
                        .category("밥/카페")
                        .bgImageUrl("https://images.unsplash.com/photo-1517457373958-b7bdd4587205?auto=format&fit=crop&q=80&w=600")
                        .build());
                
                memberRepo.save(GatheringMember.builder().gathering(g1).user(jihyun).status(MemberStatus.APPROVED).requestedAt(LocalDateTime.now()).build());
                memberRepo.save(GatheringMember.builder().gathering(g1).user(alex).status(MemberStatus.PENDING).requestedAt(LocalDateTime.now()).build());
                memberRepo.save(GatheringMember.builder().gathering(g2).user(alex).status(MemberStatus.APPROVED).requestedAt(LocalDateTime.now()).build());
                memberRepo.save(GatheringMember.builder().gathering(g3).user(alex).status(MemberStatus.APPROVED).requestedAt(LocalDateTime.now()).build());
                memberRepo.save(GatheringMember.builder().gathering(g3).user(jihyun).status(MemberStatus.APPROVED).requestedAt(LocalDateTime.now()).build());

                chatRepo.save(ChatMessage.builder().gathering(g3).sender(alex).content("안녕하세요! 강남역 어디서 뵐까요?").sentAt(LocalDateTime.now().minusMinutes(30)).build());
            }

            if (itineraryRepo.count() == 0) {
                // Seoul Trip
                RoutePoint p1 = RoutePoint.builder().dayNumber(1).dayLabel("Day 1").sequenceOrder(1).label("N서울타워").lat(37.5512).lng(126.9882).build();
                RoutePoint p2 = RoutePoint.builder().dayNumber(1).dayLabel("Day 1").sequenceOrder(2).label("경복궁").lat(37.5796).lng(126.9770).build();
                
                Itinerary seoulTrip = Itinerary.builder()
                        .title("Seoul Weekend Trip ✈️")
                        .author("Jihyun (지현)")
                        .authorEmail("jihyun@test.com")
                        .ownerEmail("jihyun@test.com")
                        .publicStatus(true)
                        .description("Exciting weekend in Seoul!")
                        .location("Seoul, Korea")
                        .startDate(java.time.LocalDate.now().plusDays(5))
                        .endDate(java.time.LocalDate.now().plusDays(7))
                        .bgImageUrl("https://images.unsplash.com/photo-1535191036316-2eb944f24d85?auto=format&fit=crop&q=80&w=600")
                        .routePoints(new ArrayList<>(List.of(p1, p2)))
                        .build();
                seoulTrip.getRoutePoints().forEach(rp -> rp.setItinerary(seoulTrip));
                itineraryRepo.save(seoulTrip);

                // Jeju Trip
                RoutePoint jp1 = RoutePoint.builder().dayNumber(1).dayLabel("Day 1").sequenceOrder(1).label("제주공항").lat(33.5113).lng(126.4930).build();
                Itinerary jejuTrip = Itinerary.builder()
                        .title("Jeju Island Adventure 🍊")
                        .author("Alex")
                        .authorEmail("alex@test.com")
                        .ownerEmail("alex@test.com")
                        .publicStatus(true)
                        .description("Food tour in Jeju!")
                        .location("Jeju, Korea")
                        .startDate(java.time.LocalDate.now().plusDays(12))
                        .endDate(java.time.LocalDate.now().plusDays(15))
                        .bgImageUrl("https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&q=80&w=600")
                        .routePoints(new ArrayList<>(List.of(jp1)))
                        .build();
                jejuTrip.getRoutePoints().forEach(rp -> rp.setItinerary(jejuTrip));
                itineraryRepo.save(jejuTrip);
            }
        };
    }
}
