package com.example.demo.controller;

import com.example.demo.dto.UserMissionResponse;
import com.example.demo.dto.UserMissionStepResponse;
import com.example.demo.usecase.UserMissionUseCase;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserMissionController.class)
class UserMissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserMissionUseCase missionService;

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
    @DisplayName("미션 시작 성공")
    void startMission_Success() throws Exception {
        // given
        UserMissionResponse res = new UserMissionResponse();
        res.setId(1L);
        res.setStatus("ACTIVE");
        given(missionService.startMission(anyLong(), anyString())).willReturn(res);

        // when & then
        mockMvc.perform(post("/api/missions/start/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser
    @DisplayName("내 미션 목록 조회 성공")
    void getMyMissions_Success() throws Exception {
        // given
        UserMissionResponse res = new UserMissionResponse();
        res.setId(1L);
        given(missionService.getMyMissions(anyString())).willReturn(List.of(res));

        // when & then
        mockMvc.perform(get("/api/missions/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("미션 단계 완료 성공")
    void completeStep_Success() throws Exception {
        // given
        UserMissionStepResponse stepRes = new UserMissionStepResponse();
        stepRes.setId(1L);
        stepRes.setCompleted(true);
        given(missionService.completeStep(anyLong(), anyLong(), anyString(), anyString(), anyString())).willReturn(stepRes);

        UserMissionController.StepCompleteRequest request = new UserMissionController.StepCompleteRequest();
        request.setMemo("Great place!");
        request.setPhotoUrl("http://image.com");

        // when & then
        mockMvc.perform(post("/api/missions/1/steps/1/complete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }
}
