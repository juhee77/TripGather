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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@org.springframework.security.test.context.support.WithMockUser
class ItineraryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("일정 생성 시 RoutePoint 배열이 함께 저장되는지 검증한다.")
    void createItineraryWithRoutePointsTest() throws Exception {
        // given
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

        // when & then
        mockMvc.perform(post("/api/itineraries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Trip to Japan"))
                .andExpect(jsonPath("$.routePoints.length()").value(2))
                .andExpect(jsonPath("$.routePoints[0].label").value("Seoul Station"))
                .andExpect(jsonPath("$.routePoints[1].label").value("Incheon Airport"));
    }

    @Test
    @DisplayName("일정 상세 조회, 수정, 삭제의 전체 CRUD 흐름을 검증한다.")
    void itineraryFullCrudFlowTest() throws Exception {
        // given: 일정 하나 생성
        Itinerary initialData = Itinerary.builder()
                .title("Initial Trip")
                .author("Tester")
                .description("Initial Description")
                .build();

        String createResponse = mockMvc.perform(post("/api/itineraries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialData)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createResponse).get("id").asLong();

        // when & then: 단일 조회 (GET /{id})
        mockMvc.perform(get("/api/itineraries/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Initial Trip"));

        // when: 수정 (PATCH /{id})
        Itinerary updateData = Itinerary.builder()
                .title("Updated Trip")
                .description("Updated Description")
                .build();

        // then: 수정 결과 확인
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/itineraries/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Trip"))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        // when: 삭제 (DELETE /{id})
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/itineraries/" + id))
                .andExpect(status().isNoContent());

        // then: 삭제 후 조회 시 서버 에러 (IllegalArgumentException) 반환 -> 400 Bad Request
        mockMvc.perform(get("/api/itineraries/" + id))
                .andExpect(status().isBadRequest());
    }
}
