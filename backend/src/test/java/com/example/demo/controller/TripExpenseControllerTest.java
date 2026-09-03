package com.example.demo.controller;

import com.example.demo.dto.TripExpenseRequest;
import com.example.demo.dto.TripExpenseResponse;
import com.example.demo.dto.TripSettlementResponse;
import com.example.demo.service.TripExpenseService;
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

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TripExpenseControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TripExpenseService tripExpenseService;

    @InjectMocks
    private TripExpenseController tripExpenseController;

    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tripExpenseController).build();
        principal = mock(Principal.class);
        lenient().when(principal.getName()).thenReturn("user@example.com");
    }

    @Test
    @DisplayName("지출 등록 API 성공")
    void addExpense_Success() throws Exception {
        // given
        given(tripExpenseService.addExpense(eq("user@example.com"), any(TripExpenseRequest.class)))
                .willReturn(TripExpenseResponse.builder().id(1L).title("숙소비")
                        .amount(new BigDecimal("120000")).build());

        // when & then
        mockMvc.perform(post("/api/trips/expenses")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TripExpenseRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("숙소비"));
    }

    @Test
    @DisplayName("여행별 지출 목록 조회 API 성공")
    void getExpensesByTrip_Success() throws Exception {
        // given
        given(tripExpenseService.getExpensesByTrip(1L)).willReturn(List.of(
                TripExpenseResponse.builder().id(1L).title("숙소비").build()));

        // when & then
        mockMvc.perform(get("/api/trips/1/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("숙소비"));
    }

    @Test
    @DisplayName("지출 삭제 API는 204 반환")
    void deleteExpense_Returns204() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/trips/expenses/1").principal(principal))
                .andExpect(status().isNoContent());
        verify(tripExpenseService).deleteExpense(1L, "user@example.com");
    }

    @Test
    @DisplayName("정산 계산 API는 memberCount 미지정 시 기본값 1로 위임")
    void calculateSettlement_DefaultMemberCount() throws Exception {
        // given
        given(tripExpenseService.calculateSettlement(1L, 1)).willReturn(
                TripSettlementResponse.builder().tripId(1L).memberCount(1)
                        .totalAmount(new BigDecimal("120000")).build());

        // when & then
        mockMvc.perform(get("/api/trips/1/settlement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(1));
    }

    @Test
    @DisplayName("정산 계산 API는 memberCount 지정 시 해당 인원으로 위임")
    void calculateSettlement_WithMemberCount() throws Exception {
        // given
        given(tripExpenseService.calculateSettlement(1L, 4)).willReturn(
                TripSettlementResponse.builder().tripId(1L).memberCount(4)
                        .perPersonAmount(new BigDecimal("30000")).build());

        // when & then
        mockMvc.perform(get("/api/trips/1/settlement").param("memberCount", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(4));
    }
}
