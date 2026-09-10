package com.example.demo.controller;

import com.example.demo.dto.ItineraryResponse;
import com.example.demo.dto.TripRequest;
import com.example.demo.dto.TripResponse;
import com.example.demo.service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tripController).build();
    }

    @Test
    @DisplayName("여행 생성 API 성공")
    void createTrip_Success() throws Exception {
        // given
        TripRequest request = TripRequest.builder().title("제주 여행").destination("Jeju").build();
        given(tripService.createTrip(any(TripRequest.class)))
                .willReturn(TripResponse.builder().id(1L).title("제주 여행").build());

        // when & then
        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("제주 여행"));
    }

    @Test
    @DisplayName("내 여행 목록 조회 API 성공")
    void getMyTrips_Success() throws Exception {
        // given
        given(tripService.getMyTrips()).willReturn(List.of(
                TripResponse.builder().id(1L).title("제주 여행").build()));

        // when & then
        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("여행 단건 조회 API 성공")
    void getTrip_Success() throws Exception {
        // given
        given(tripService.getTrip(1L)).willReturn(TripResponse.builder().id(1L).title("제주 여행").build());

        // when & then
        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("제주 여행"));
    }

    @Test
    @DisplayName("여행 수정 API 성공")
    void updateTrip_Success() throws Exception {
        // given
        TripRequest request = TripRequest.builder().title("부산 여행").build();
        given(tripService.updateTrip(eq(1L), any(TripRequest.class)))
                .willReturn(TripResponse.builder().id(1L).title("부산 여행").build());

        // when & then
        mockMvc.perform(put("/api/trips/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("부산 여행"));
    }

    @Test
    @DisplayName("여행 삭제 API는 204 반환")
    void deleteTrip_Returns204() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/trips/1"))
                .andExpect(status().isNoContent());
        verify(tripService).deleteTrip(1L);
    }

    @Test
    @DisplayName("추천 여정 조회 API 성공")
    void getRecommendations_Success() throws Exception {
        // given
        given(tripService.getRecommendedItineraries(1L)).willReturn(List.of(
                ItineraryResponse.builder().id(5L).title("제주 3박4일").build()));

        // when & then
        mockMvc.perform(get("/api/trips/1/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));
    }
}
