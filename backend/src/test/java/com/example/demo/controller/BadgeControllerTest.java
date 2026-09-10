package com.example.demo.controller;

import com.example.demo.dto.BadgeDto;
import com.example.demo.service.BadgeService;
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
class BadgeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BadgeService badgeService;

    @InjectMocks
    private BadgeController badgeController;

    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(badgeController).build();
        principal = mock(Principal.class);
        lenient().when(principal.getName()).thenReturn("user@example.com");
    }

    @Test
    @DisplayName("내 배지 목록 조회 API 성공")
    void getMyBadges_Success() throws Exception {
        // given
        given(badgeService.getUserBadges("user@example.com")).willReturn(List.of(
                BadgeDto.builder().code("FIRST_TRIP").name("첫 여행").unlocked(true).build()));

        // when & then
        mockMvc.perform(get("/api/users/me/badges").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("FIRST_TRIP"))
                .andExpect(jsonPath("$[0].unlocked").value(true));
    }

    @Test
    @DisplayName("인증 정보 없이 배지 목록 조회 시 401 반환")
    void getMyBadges_NoPrincipal_Returns401() throws Exception {
        // when & then
        mockMvc.perform(get("/api/users/me/badges"))
                .andExpect(status().isUnauthorized());
    }
}
