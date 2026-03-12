package com.example.demo;

import com.example.demo.domain.Itinerary;
import com.example.demo.domain.RoutePoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItineraryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("일정 생성 시 RoutePoint 배멸 함께 저장 검증")
    void createItineraryWithRoutePointsTest() throws Exception {
        RoutePoint rp1 = RoutePoint.builder()
                .sequenceOrder(1)
                .label("Seoul Station")
                .lat(37.5545)
                .lng(126.9708)
                .build();

        RoutePoint rp2 = RoutePoint.builder()
                .sequenceOrder(2)
                .label("Incheon Airport")
                .lat(37.4602)
                .lng(126.4407)
                .build();

        Itinerary requestData = Itinerary.builder()
                .title("Trip to Japan")
                .author("Jihyun (지현)")
                .description("A wonderful journey.")
                .routePoints(List.of(rp1, rp2))
                .build();

        mockMvc.perform(post("/api/itineraries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Trip to Japan"))
                .andExpect(jsonPath("$.routePoints.length()").value(2))
                .andExpect(jsonPath("$.routePoints[0].label").value("Seoul Station"))
                .andExpect(jsonPath("$.routePoints[1].label").value("Incheon Airport"));
    }
}
