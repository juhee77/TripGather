package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.usecase.UserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserUseCase userService;

    @MockBean
    private com.example.demo.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.example.demo.repository.UserRepository userRepository;

    @MockBean
    private com.example.demo.security.oauth.CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private com.example.demo.security.oauth.OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("내 프로필 조회 성공")
    void getMyProfile_Success() throws Exception {
        // given
        User user = User.builder().id(1L).email("test@example.com").name("Test User").build();
        given(userService.getCurrentUser()).willReturn(user);

        // when & then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @WithMockUser
    @DisplayName("ID로 특정 유저 조회 성공")
    void getUser_Success() throws Exception {
        // given
        User user = User.builder().id(1L).email("user1@example.com").name("User 1").build();
        given(userService.getById(1L)).willReturn(user);

        // when & then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User 1"));
    }

    @Test
    @WithMockUser
    @DisplayName("전체 유저 목록 조회 성공")
    void getAllUsers_Success() throws Exception {
        // given
        User user1 = User.builder().id(1L).name("User 1").email("u1@ex.com").build();
        User user2 = User.builder().id(2L).name("User 2").email("u2@ex.com").build();
        given(userService.getAllUsers()).willReturn(List.of(user1, user2));

        // when & then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    @DisplayName("프로필 업데이트 성공")
    void updateProfile_Success() throws Exception {
        // given
        User updateData = User.builder().name("New Name").build();
        User updatedUser = User.builder().id(1L).name("New Name").email("test@example.com").build();
        given(userService.updateProfile(anyLong(), any(User.class))).willReturn(updatedUser);

        // when & then
        mockMvc.perform(patch("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }
}
