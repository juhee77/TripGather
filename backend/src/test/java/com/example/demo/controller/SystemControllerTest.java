package com.example.demo.controller;

import com.example.demo.dto.SystemStatsResponse;
import com.example.demo.service.SystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SystemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SystemService systemService;

    @InjectMocks
    private SystemController systemController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(systemController).build();
    }

    @Test
    @DisplayName("헬스 체크 API는 서비스 의존 없이 UP 상태 반환")
    void healthCheck_ReturnsUp() throws Exception {
        // when & then
        mockMvc.perform(get("/api/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("TripGather Backend"));
    }

    @Test
    @DisplayName("시스템 통계 조회 API 성공")
    void getSystemStats_Success() throws Exception {
        // given
        given(systemService.getSystemStats()).willReturn(
                SystemStatsResponse.builder().totalUsers(10).totalGatherings(3).build());

        // when & then
        mockMvc.perform(get("/api/system/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(10))
                .andExpect(jsonPath("$.totalGatherings").value(3));
    }
}
