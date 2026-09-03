package com.example.demo.controller;

import com.example.demo.dto.StampResponse;
import com.example.demo.usecase.StampUseCase;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StampControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StampUseCase stampService;

    @InjectMocks
    private StampController stampController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stampController).build();
    }

    @Test
    @DisplayName("내 스탬프 목록 조회 API 성공")
    void getMyStamps_Success() throws Exception {
        // given
        given(stampService.getMyStamps()).willReturn(List.of(
                StampResponse.builder().missionId(1L).missionTitle("제주 스탬프").build()));

        // when & then
        mockMvc.perform(get("/api/stamps/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].missionTitle").value("제주 스탬프"));
    }
}
