package com.example.demo.controller;

import com.example.demo.dto.PointTransactionResponse;
import com.example.demo.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PointControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PointService pointService;

    @InjectMocks
    private PointController pointController;

    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pointController).build();
        principal = mock(Principal.class);
        lenient().when(principal.getName()).thenReturn("user@example.com");
    }

    @Test
    @DisplayName("유저 포인트 거래 내역 조회 성공")
    void getUserPointTransactions_Success() throws Exception {
        // given
        PointTransactionResponse res = PointTransactionResponse.builder()
                .id(1L)
                .amount(50)
                .transactionType("EARN")
                .description("체크인 보상")
                .build();

        given(pointService.getUserPointTransactions("user@example.com", "EARN")).willReturn(List.of(res));

        // when & then
        mockMvc.perform(get("/api/points/transactions").param("type", "EARN").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(50))
                .andExpect(jsonPath("$[0].transactionType").value("EARN"));
    }
}
