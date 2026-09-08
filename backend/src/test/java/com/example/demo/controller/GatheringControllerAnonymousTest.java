package com.example.demo.controller;

import com.example.demo.domain.Gathering;
import com.example.demo.repository.StampRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.usecase.GatheringMemberUseCase;
import com.example.demo.usecase.GatheringUseCase;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/gatherings/** 는 SecurityConfig 상 permitAll 이라 비로그인 사용자도 도달한다.
 * 다만 @WebMvcTest 는 애플리케이션의 SecurityConfig 를 로드하지 않고 Spring Boot 기본 보안
 * (전 요청 인증 필요)을 적용하므로 익명 요청이 302 로 막힌다. 따라서 보안 필터가 없는
 * standaloneSetup 으로 컨트롤러 자체의 principal == null 처리만 분리해 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class GatheringControllerAnonymousTest {

    private MockMvc mockMvc;

    @Mock
    private GatheringUseCase gatheringService;
    @Mock
    private GatheringMemberUseCase gatheringMemberService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StampRepository stampRepository;

    @InjectMocks
    private GatheringController gatheringController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gatheringController).build();
    }

    @Test
    @DisplayName("비로그인 사용자의 전체 모임 조회는 찜/체크인 조회 없이 반환")
    void getAllGatherings_Anonymous_SkipsPersonalizedLookups() throws Exception {
        // given
        given(gatheringService.getAllGatherings(null)).willReturn(List.of(
                Gathering.builder().id(1L).title("공개 모임").build()));

        // when & then
        mockMvc.perform(get("/api/gatherings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("공개 모임"));

        verify(gatheringService, never()).isLikedByUser(anyLong(), any());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("비로그인 사용자의 인기 모임 조회는 찜/체크인 조회 없이 반환")
    void getPopularGatherings_Anonymous_SkipsPersonalizedLookups() throws Exception {
        // given
        given(gatheringService.getPopularGatherings()).willReturn(List.of(
                Gathering.builder().id(1L).title("인기 모임").build()));

        // when & then
        mockMvc.perform(get("/api/gatherings/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("인기 모임"));

        verify(gatheringService, never()).isLikedByUser(anyLong(), any());
    }

    @Test
    @DisplayName("비로그인 사용자의 찜 여부 조회는 조회 없이 false 반환")
    void isLikedByUser_Anonymous_ReturnsFalse() throws Exception {
        // when & then
        mockMvc.perform(get("/api/gatherings/1/is-liked"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(gatheringService, never()).isLikedByUser(anyLong(), any());
    }

    @Test
    @DisplayName("비로그인 사용자의 참여 권한 조회는 조회 없이 false 반환")
    void isAuthorizedMember_Anonymous_ReturnsFalse() throws Exception {
        // when & then
        mockMvc.perform(get("/api/gatherings/1/is-authorized"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(gatheringMemberService, never()).isAuthorizedMember(anyLong(), any());
    }

    @Test
    @DisplayName("비로그인 사용자의 찜 목록 조회는 401 반환")
    void getUserLikedGatherings_Anonymous_Returns401() throws Exception {
        // when & then
        mockMvc.perform(get("/api/gatherings/me/liked"))
                .andExpect(status().isUnauthorized());

        verify(gatheringService, never()).getUserLikedGatherings(any());
    }
}
