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

import static org.mockito.BDDMockito.given;
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
}
