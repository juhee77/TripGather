package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("인증된 사용자가신의 프로필 정보를 정상적으로 조회할 수 있는지 검증한다.")
    @org.springframework.security.test.context.support.WithMockUser(username = "jihyun@test.com")
    void getMyProfileTest() throws Exception {
        // given: 인증된 사용자 세션이 WithMockUser에 의해 준비됨.
        // 현재 UserService/Controller 스펙상 /api/users/me는 jihyun@test.com 정보를 찾아 반환함.

        // when & then
        mockMvc.perform(get("/api/users/me"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jihyun@test.com"))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.bio").exists());
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 /api/users/me 접근 시 403 혹은 401 에러를 받는지 검증한다.")
    void getMyProfileUnauthenticatedTest() throws Exception {
        // given: 인증 정보 없음

        // when & then
        mockMvc.perform(get("/api/users/me"))
                .andDo(print())
                .andExpect(status().is4xxClientError()); // 401 or 403 or 302
    }
}
