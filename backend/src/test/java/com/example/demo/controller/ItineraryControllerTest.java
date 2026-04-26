package com.example.demo.controller;

import com.example.demo.domain.Itinerary;
import com.example.demo.usecase.ItineraryUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItineraryController.class)
class ItineraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItineraryUseCase itineraryService;

    @MockBean
    private com.example.demo.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.example.demo.repository.UserRepository userRepository;

    @MockBean
    private com.example.demo.security.oauth.CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private com.example.demo.security.oauth.OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("전체 일정 조회 성공")
    void getAllItineraries_Success() throws Exception {
        // given
        Itinerary i1 = Itinerary.builder().id(1L).title("Trip 1").author("User 1").build();
        given(itineraryService.getPublicItineraries()).willReturn(List.of(i1));

        // when & then
        mockMvc.perform(get("/api/itineraries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Trip 1"));
    }

    @Test
    @WithMockUser
    @DisplayName("특정 일정 조회 성공")
    void getItinerary_Success() throws Exception {
        // given
        Itinerary i = Itinerary.builder().id(1L).title("Seoul Trip").author("User 1").build();
        given(itineraryService.getById(1L)).willReturn(i);

        // when & then
        mockMvc.perform(get("/api/itineraries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Seoul Trip"));
    }

    @Test
    @WithMockUser
    @DisplayName("일정 생성 성공")
    void createItinerary_Success() throws Exception {
        // given
        Itinerary input = Itinerary.builder().title("New Trip").build();
        Itinerary saved = Itinerary.builder().id(1L).title("New Trip").author("User 1").build();
        given(itineraryService.createItinerary(any(Itinerary.class))).willReturn(saved);

        // when & then
        mockMvc.perform(post("/api/itineraries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Trip"));
    }

    @Test
    @WithMockUser
    @DisplayName("일정 삭제 성공")
    void deleteItinerary_Success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/itineraries/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
