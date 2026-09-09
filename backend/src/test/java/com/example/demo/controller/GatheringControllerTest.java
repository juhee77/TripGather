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
    private com.example.demo.usecase.GatheringMemberUseCase gatheringMemberService;

    @MockBean
    private com.example.demo.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.example.demo.repository.UserRepository userRepository;

    @MockBean
    private com.example.demo.security.oauth.CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private com.example.demo.security.oauth.OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockBean
    private com.example.demo.repository.StampRepository stampRepository;

    @MockBean
    private com.example.demo.repository.GatheringLikeRepository gatheringLikeRepository;

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
                .andExpect(jsonPath("$[0].title").value("Gathering 1"))
                .andExpect(jsonPath("$[0].isCommentPublic").value(true));
    }

    @Test
    @WithMockUser(username = "viewer@test.com")
    @DisplayName("목록 조회 시 좋아요/체크인 상태를 행마다 조회하지 않는다 (N+1 방지)")
    void getAllGatherings_DoesNotQueryPerRow() throws Exception {
        // given: 모임 3건
        List<Gathering> gatherings = List.of(
                Gathering.builder().id(1L).title("G1").build(),
                Gathering.builder().id(2L).title("G2").build(),
                Gathering.builder().id(3L).title("G3").build());
        given(gatheringService.getAllGatherings(any())).willReturn(gatherings);

        com.example.demo.domain.User viewer =
                com.example.demo.domain.User.builder().id(9L).email("viewer@test.com").build();
        given(userRepository.findByEmail("viewer@test.com")).willReturn(java.util.Optional.of(viewer));
        given(gatheringLikeRepository.findLikedGatheringIdsByUserId(9L)).willReturn(List.of(2L));
        given(stampRepository.findStampedGatheringIdsByUserId(9L)).willReturn(List.of(3L));

        // when & then
        mockMvc.perform(get("/api/gatherings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].likedByCurrentUser").value(false))
                .andExpect(jsonPath("$[1].likedByCurrentUser").value(true))
                .andExpect(jsonPath("$[2].hasCheckedIn").value(true));

        // 뷰어 상태는 요청당 한 번씩만 조회되어야 한다.
        org.mockito.Mockito.verify(gatheringLikeRepository, org.mockito.Mockito.times(1))
                .findLikedGatheringIdsByUserId(9L);
        org.mockito.Mockito.verify(stampRepository, org.mockito.Mockito.times(1))
                .findStampedGatheringIdsByUserId(9L);

        // 행 단위 조회 경로는 더 이상 사용되지 않는다.
        org.mockito.Mockito.verify(gatheringService, org.mockito.Mockito.never())
                .isLikedByUser(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
        org.mockito.Mockito.verify(stampRepository, org.mockito.Mockito.never())
                .existsByUserIdAndGatheringId(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    @WithMockUser
    @DisplayName("모임 검색 성공")
    void searchGatherings_Success() throws Exception {
        // given
        Gathering g1 = Gathering.builder().id(1L).title("Search Result").build();
        given(gatheringService.searchGatherings(any(), any(), any(), any(), any())).willReturn(List.of(g1));

        // when & then
        mockMvc.perform(get("/api/gatherings/search")
                        .param("query", "Search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Search Result"));
    }

    @Test
    @WithMockUser
    @DisplayName("내가 호스팅한 모임 조회 성공")
    void getMyHostedGatherings_Success() throws Exception {
        // given
        Gathering g1 = Gathering.builder().id(1L).title("My Gathering").build();
        given(gatheringService.getHostedGatherings()).willReturn(List.of(g1));

        // when & then
        mockMvc.perform(get("/api/gatherings/my/hosted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("My Gathering"));
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
        given(gatheringMemberService.joinGathering(anyLong())).willReturn(gathering);

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

    @Test
    @WithMockUser
    @DisplayName("모임 좋아요 성공")
    void likeGathering_Success() throws Exception {
        // when & then
        mockMvc.perform(post("/api/gatherings/1/like")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("모임 업데이트 성공")
    void updateGathering_Success() throws Exception {
        // given
        Gathering updated = Gathering.builder().id(1L).title("Updated").build();
        given(gatheringService.updateGathering(anyLong(), any(Gathering.class))).willReturn(updated);

        // when & then
        mockMvc.perform(put("/api/gatherings/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    @WithMockUser
    @DisplayName("모임 상세 조회 성공")
    void getGathering_Success() throws Exception {
        Gathering g = Gathering.builder().id(1L).title("Detail").build();
        given(gatheringService.getGathering(anyLong())).willReturn(g);

        mockMvc.perform(get("/api/gatherings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Detail"));
    }

    @Test
    @WithMockUser
    @DisplayName("모임 삭제 성공")
    void deleteGathering_Success() throws Exception {
        mockMvc.perform(delete("/api/gatherings/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }


    @Test
    @WithMockUser
    @DisplayName("모임 탈퇴 성공")
    void leaveGathering_Success() throws Exception {
        mockMvc.perform(post("/api/gatherings/1/leave")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("멤버 거절 성공")
    void rejectMember_Success() throws Exception {
        mockMvc.perform(post("/api/gatherings/1/members/2/reject")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("내가 참가한 모임 조회 성공")
    void getJoinedGatherings_Success() throws Exception {
        Gathering g = Gathering.builder().id(1L).title("Joined").build();
        given(gatheringMemberService.getJoinedGatherings()).willReturn(List.of(g));

        mockMvc.perform(get("/api/gatherings/my/joined"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Joined"));
    }
}
