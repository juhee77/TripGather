package com.example.demo.controller;

import com.example.demo.dto.GatheringMissionRequest;
import com.example.demo.dto.GatheringMissionResponse;
import com.example.demo.dto.MissionCompletionResponse;
import com.example.demo.dto.MissionProgressResponse;
import com.example.demo.dto.MissionSubmitRequest;
import com.example.demo.usecase.GatheringMissionUseCase;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GatheringMissionControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GatheringMissionUseCase missionService;

    @InjectMocks
    private GatheringMissionController gatheringMissionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gatheringMissionController).build();
    }

    @Test
    @DisplayName("모임 미션 목록 조회 API 성공")
    void getMissions_Success() throws Exception {
        // given
        given(missionService.getMissions(1L)).willReturn(List.of(
                GatheringMissionResponse.builder().id(1L).gatheringId(1L)
                        .title("인증샷 남기기").rewardPoints(50).build()));

        // when & then
        mockMvc.perform(get("/api/gatherings/1/missions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("인증샷 남기기"))
                .andExpect(jsonPath("$[0].rewardPoints").value(50));
    }

    @Test
    @DisplayName("내 미션 진행률 조회 API 성공")
    void getMyProgress_Success() throws Exception {
        // given
        given(missionService.getMyProgress(1L)).willReturn(
                MissionProgressResponse.builder().gatheringId(1L).totalCount(4)
                        .clearedCount(2).progressPercentage(50.0).earnedPoints(100).build());

        // when & then
        mockMvc.perform(get("/api/gatherings/1/missions/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clearedCount").value(2))
                .andExpect(jsonPath("$.progressPercentage").value(50.0));
    }

    @Test
    @DisplayName("미션 인증 제출 API 성공")
    void submit_Success() throws Exception {
        // given
        given(missionService.submitCompletion(eq(1L), eq(7L), any(MissionSubmitRequest.class)))
                .willReturn(MissionCompletionResponse.builder().id(1L).missionId(7L)
                        .missionTitle("인증샷 남기기").photoUrl("photo.png").build());

        // when & then
        mockMvc.perform(post("/api/gatherings/1/missions/7/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                MissionSubmitRequest.builder().photoUrl("photo.png").memo("완료").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missionId").value(7));
    }

    @Test
    @DisplayName("사진 없이 미션 인증 제출 시에도 본문 없이 위임")
    void submit_WithoutBody_Success() throws Exception {
        // given
        given(missionService.submitCompletion(1L, 7L, null))
                .willReturn(MissionCompletionResponse.builder().id(1L).missionId(7L).build());

        // when & then
        mockMvc.perform(post("/api/gatherings/1/missions/7/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missionId").value(7));
    }

    @Test
    @DisplayName("호스트의 미션 생성 API 성공")
    void create_Success() throws Exception {
        // given
        given(missionService.createMission(eq(1L), any(GatheringMissionRequest.class)))
                .willReturn(GatheringMissionResponse.builder().id(1L).title("인증샷 남기기").build());

        // when & then
        mockMvc.perform(post("/api/gatherings/1/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                GatheringMissionRequest.builder().title("인증샷 남기기")
                                        .rewardPoints(50).requiresPhoto(true).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("인증샷 남기기"));
    }

    @Test
    @DisplayName("호스트의 미션 수정 API 성공")
    void update_Success() throws Exception {
        // given
        given(missionService.updateMission(eq(1L), eq(7L), any(GatheringMissionRequest.class)))
                .willReturn(GatheringMissionResponse.builder().id(7L).title("수정된 미션").build());

        // when & then
        mockMvc.perform(put("/api/gatherings/1/missions/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                GatheringMissionRequest.builder().title("수정된 미션").build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 미션"));
    }

    @Test
    @DisplayName("호스트의 미션 삭제 API는 204 반환")
    void delete_Returns204() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/gatherings/1/missions/7"))
                .andExpect(status().isNoContent());
        verify(missionService).deleteMission(1L, 7L);
    }

    @Test
    @DisplayName("호스트의 미승인 인증 목록 조회 API 성공")
    void getPending_Success() throws Exception {
        // given
        given(missionService.getPendingCompletions(1L)).willReturn(List.of(
                MissionCompletionResponse.builder().id(1L).missionId(7L)
                        .userName("홍길동").build()));

        // when & then
        mockMvc.perform(get("/api/gatherings/1/missions/completions/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("홍길동"));
    }

    @Test
    @DisplayName("호스트의 미션 인증 승인 API 성공")
    void approve_Success() throws Exception {
        // given
        given(missionService.approveCompletion(1L, 3L)).willReturn(
                MissionCompletionResponse.builder().id(3L).missionId(7L).build());

        // when & then
        mockMvc.perform(patch("/api/gatherings/1/missions/completions/3/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @DisplayName("호스트의 미션 인증 거절 API 성공")
    void reject_Success() throws Exception {
        // given
        given(missionService.rejectCompletion(1L, 3L)).willReturn(
                MissionCompletionResponse.builder().id(3L).missionId(7L).build());

        // when & then
        mockMvc.perform(patch("/api/gatherings/1/missions/completions/3/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }
}
