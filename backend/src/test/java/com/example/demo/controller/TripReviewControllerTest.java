package com.example.demo.controller;

import com.example.demo.dto.TripReviewResponse;
import com.example.demo.service.TripReviewService;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TripReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TripReviewService tripReviewService;

    @InjectMocks
    private TripReviewController tripReviewController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tripReviewController).build();
    }

    @Test
    @DisplayName("여행 후기 수정 성공 200 OK")
    void updateReview_Success() throws Exception {
        // given
        TripReviewResponse response = TripReviewResponse.builder()
                .id(10L)
                .content("Updated Review Content")
                .rating(5)
                .category("관광지")
                .build();

        given(tripReviewService.updateReview(10L, "Updated Review Content", 5, "관광지", null))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/trips/1/reviews/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": \"Updated Review Content\", \"rating\": 5, \"category\": \"관광지\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.content").value("Updated Review Content"));
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("여행 후기 작성 API 성공")
    void createReview_Success() throws Exception {
        // given
        given(tripReviewService.createReview(1L, "좋은 여행이었습니다.", 4, "숙소", "img1.png"))
                .willReturn(TripReviewResponse.builder().id(1L).content("좋은 여행이었습니다.").rating(4).build());

        // when & then
        mockMvc.perform(post("/api/trips/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "content", "좋은 여행이었습니다.",
                                "rating", 4,
                                "category", "숙소",
                                "imageUrls", "img1.png"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    @DisplayName("여행 후기 작성 시 평점과 카테고리 미지정이면 기본값(5점, 관광지) 적용")
    void createReview_DefaultsApplied() throws Exception {
        // given
        given(tripReviewService.createReview(1L, "좋은 여행이었습니다.", 5, "관광지", null))
                .willReturn(TripReviewResponse.builder().id(1L).rating(5).category("관광지").build());

        // when & then
        mockMvc.perform(post("/api/trips/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "좋은 여행이었습니다."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("관광지"));
        verify(tripReviewService).createReview(1L, "좋은 여행이었습니다.", 5, "관광지", null);
    }

    @Test
    @DisplayName("여행 후기 목록 조회 API 성공")
    void getReviews_Success() throws Exception {
        // given
        given(tripReviewService.getReviews(1L, "숙소")).willReturn(List.of(
                TripReviewResponse.builder().id(1L).content("깨끗한 숙소").category("숙소").build()));

        // when & then
        mockMvc.perform(get("/api/trips/1/reviews").param("category", "숙소"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("숙소"));
    }

    @Test
    @DisplayName("여행 후기 삭제 API는 204 반환")
    void deleteReview_Returns204() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/trips/1/reviews/10"))
                .andExpect(status().isNoContent());
        verify(tripReviewService).deleteReview(10L);
    }

    @Test
    @DisplayName("여행 후기 요약 통계 조회 API 성공")
    void getReviewSummary_Success() throws Exception {
        // given
        given(tripReviewService.getReviewSummary(1L)).willReturn(
                com.example.demo.dto.TripReviewSummaryResponse.builder()
                        .tripId(1L).totalReviews(2).averageRating(4.5).build());

        // when & then
        mockMvc.perform(get("/api/trips/1/reviews/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }
}
