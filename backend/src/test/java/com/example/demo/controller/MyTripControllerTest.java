package com.example.demo.controller;

import com.example.demo.domain.Itinerary;
import com.example.demo.security.SecurityService;
import com.example.demo.usecase.ItineraryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MyTripControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItineraryUseCase itineraryService;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private MyTripController myTripController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(myTripController).build();
        lenient().when(securityService.getCurrentUserEmail()).thenReturn("user@example.com");
    }

    @Test
    @DisplayName("내 여정 목록 조회 API는 인증 주체의 이메일로 조회")
    void getMyJourneys_UsesCurrentUserEmail() throws Exception {
        // given
        given(itineraryService.getUserJourneys("user@example.com")).willReturn(List.of(
                Itinerary.builder().id(1L).title("제주 3박4일").build()));

        // when & then
        mockMvc.perform(get("/api/my-trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("제주 3박4일"));
    }

    @Test
    @DisplayName("여정 복제 API 성공")
    void cloneItinerary_Success() throws Exception {
        // given
        given(itineraryService.cloneItinerary(5L, "user@example.com")).willReturn(
                Itinerary.builder().id(9L).title("제주 3박4일").originalId(5L).build());

        // when & then
        mockMvc.perform(post("/api/my-trips/clone").param("originalId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.originalId").value(5));
    }

    @Test
    @DisplayName("여정 공개 여부 토글 API 성공")
    void togglePublic_Success() throws Exception {
        // given
        given(itineraryService.togglePublicStatus(9L, "user@example.com", true)).willReturn(
                Itinerary.builder().id(9L).title("제주 3박4일").publicStatus(true).build());

        // when & then
        mockMvc.perform(patch("/api/my-trips/9/share").param("isPublic", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isPublic").value(true));
    }

    @Test
    @DisplayName("여정 병합 API 성공")
    void mergeItinerary_Success() throws Exception {
        // given
        given(itineraryService.mergeItinerary(1L, 2L, 3, "user@example.com")).willReturn(
                Itinerary.builder().id(2L).title("통합 여정").build());

        // when & then
        mockMvc.perform(post("/api/my-trips/merge")
                        .param("sourceId", "1")
                        .param("targetId", "2")
                        .param("targetDay", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("통합 여정"));
    }
}
