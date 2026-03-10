package com.example.demo;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.Itinerary;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.ItineraryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(GatheringRepository gatheringRepo, ItineraryRepository itineraryRepo) {
        return args -> {
            if(gatheringRepo.count() == 0) {
                gatheringRepo.save(Gathering.builder()
                        .title("Weekend Trip to Busan! (부산 주말 여행! 🌊)")
                        .host("Jihyun (지현)")
                        .location("Busan, Korea")
                        .dates("Aug 17-18")
                        .currentJoining(4)
                        .maxJoining(6)
                        .bgImageUrl("https://images.unsplash.com/photo-1546872957-3f746681498b?auto=format&fit=crop&q=80&w=600")
                        .build());
                        
                gatheringRepo.save(Gathering.builder()
                        .title("오늘 저녁 한강 러닝 뛸 사람~ 🏃‍♂️")
                        .host("Alex")
                        .location("Banpo Hangang Park")
                        .dates("Today 20:00")
                        .currentJoining(2)
                        .maxJoining(4)
                        .bgImageUrl("")
                        .build());
            }

            if(itineraryRepo.count() == 0) {
                itineraryRepo.save(Itinerary.builder()
                        .title("Seoul Weekend Trip (나의 주말 서울 나들이)")
                        .author("Jihyun (지현)")
                        .description("N Seoul Tower -> DDP -> Hangang Park")
                        .build());
                itineraryRepo.save(Itinerary.builder()
                        .title("제주도 2박 3일 먹방 코스 🍊")
                        .author("Alex")
                        .description("고기국수 -> 흑돼지 -> 귤밭 카페")
                        .build());
            }
        };
    }
}
