package com.example.demo.controller;

import com.example.demo.domain.Gathering;
import com.example.demo.dto.GatheringResponse;
import com.example.demo.usecase.GatheringUseCase;
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

@WebMvcTest(GatheringController.class)
class GatheringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatheringUseCase gatheringService;

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
    @DisplayName("전체 모임 조회 성공")
    void getAllGatherings_Success() throws Exception {
        // given
        Gathering g1 = Gathering.builder().id(1L).title("Gathering 1").build();
        given(gatheringService.getAllGatherings(any())).willReturn(List.of(g1));

        // when & then
        mockMvc.perform(get("/api/gatherings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Gathering 1"));
    }

    @Test
    @WithMockUser
    @DisplayName("모임 생성 성공")
    void createGathering_Success() throws Exception {
        // given
        Gathering gathering = Gathering.builder().title("New Gathering").maxJoining(5).build();
        given(gatheringService.createGathering(any(Gathering.class))).willReturn(gathering);

        // when & then
        mockMvc.perform(post("/api/gatherings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gathering)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Gathering"));
    }

    @Test
    @WithMockUser
    @DisplayName("모임 참가 신청 성공")
    void joinGathering_Success() throws Exception {
        // given
        Gathering gathering = Gathering.builder().id(1L).title("Test").build();
        given(gatheringService.joinGathering(anyLong())).willReturn(gathering);

        // when & then
        mockMvc.perform(post("/api/gatherings/1/join")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("멤버 승인 성공")
    void approveMember_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/gatherings/1/members/2/approve")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
