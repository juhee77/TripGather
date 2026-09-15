package com.example.demo.controller;

import com.example.demo.dto.PackingItemResponse;
import com.example.demo.dto.PackingProgressResponse;
import com.example.demo.service.PackingService;
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
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PackingControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PackingService packingService;

    @InjectMocks
    private PackingController packingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(packingController).build();
    }

    @Test
    @DisplayName("기본 준비물 세트 초기화 API 성공")
    void initDefaults_Success() throws Exception {
        // given
        given(packingService.initDefaultItems(1L)).willReturn(List.of(
                PackingItemResponse.builder().id(1L).name("여권").category("필수").build()));

        // when & then
        mockMvc.perform(post("/api/trips/1/packing/init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("여권"));
    }

    @Test
    @DisplayName("준비물 목록 조회 API 성공")
    void getItems_Success() throws Exception {
        // given
        given(packingService.getItems(1L)).willReturn(List.of(
                PackingItemResponse.builder().id(1L).name("여권").build()));

        // when & then
        mockMvc.perform(get("/api/trips/1/packing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("준비물 추가 API 성공")
    void addItem_Success() throws Exception {
        // given
        given(packingService.addItem(1L, "선크림", "화장품")).willReturn(
                PackingItemResponse.builder().id(2L).name("선크림").category("화장품").build());

        // when & then
        mockMvc.perform(post("/api/trips/1/packing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "선크림", "category", "화장품"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("선크림"));
    }

    @Test
    @DisplayName("준비물 체크 토글 API 성공")
    void toggleCheck_Success() throws Exception {
        // given
        given(packingService.toggleCheck(7L)).willReturn(
                PackingItemResponse.builder().id(7L).name("여권").checked(true).build());

        // when & then
        mockMvc.perform(patch("/api/trips/1/packing/7/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checked").value(true));
    }

    @Test
    @DisplayName("준비물 삭제 API는 204 반환")
    void deleteItem_Returns204() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/trips/1/packing/7"))
                .andExpect(status().isNoContent());
        verify(packingService).deleteItem(7L);
    }

    @Test
    @DisplayName("준비물 준비 진행률 조회 API 성공")
    void getProgress_Success() throws Exception {
        // given
        given(packingService.getPackingProgress(1L)).willReturn(
                PackingProgressResponse.builder().tripId(1L).totalCount(4).checkedCount(2)
                        .progressPercentage(50.0).build());

        // when & then
        mockMvc.perform(get("/api/trips/1/packing/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPercentage").value(50.0));
    }
}
