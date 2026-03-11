package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("내 정보(프로필) 조회 API 테스트")
    void getMyProfileTest() throws Exception {
        // 단순 임시 인증 스펙: 하드코딩된 디폴트 정보나 Session 기반 유저 객체 리턴 검증
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jihyun (지현)")) // 호환성 유지를 위해 디폴트 Jihyun 기대
                .andExpect(jsonPath("$.bio").value("여행을 좋아하는 사람"))
                .andExpect(jsonPath("$.points").exists());
    }
}
