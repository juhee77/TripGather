package com.example.demo;

import com.example.demo.domain.Gathering;
import com.example.demo.repository.GatheringRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback after tests
class GatheringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Test
    @DisplayName("모임 생성 API (정상 케이스) 테스트")
    void createGatheringTest() throws Exception {
        Gathering requestData = Gathering.builder()
                .title("테스트 모임입니다")
                .host("테스트 호스트")
                .location("테스트 장소")
                .dates("2026-03-10 18:00")
                .category("테스트 카테고리")
                .currentJoining(1)
                .maxJoining(5)
                .bgImageUrl("https://example.com/image.jpg")
                .build();

        mockMvc.perform(post("/api/gatherings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 모임입니다"))
                .andExpect(jsonPath("$.category").value("테스트 카테고리"));
    }

    @Test
    @DisplayName("이미지 텍스트 긴 경우 (Base64) 테스트")
    void createGatheringWithLongImageTest() throws Exception {
        // Base64 문자열이 255자를 넘을 때의 상황 모사
        String longBase64 = "data:image/jpeg;base64," + "A".repeat(1000);
        Gathering requestData = Gathering.builder()
                .title("긴 이미지 테스트")
                .host("테스트 호스트")
                .location("테스트 장소")
                .dates("2026-03-10 18:00")
                .category("테스트 카테고리")
                .currentJoining(1)
                .maxJoining(5)
                .bgImageUrl(longBase64)
                .build();

        mockMvc.perform(post("/api/gatherings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("긴 이미지 테스트"));
    }
}
