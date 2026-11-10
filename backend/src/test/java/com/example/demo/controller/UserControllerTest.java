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
import java.util.Map;

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
    @DisplayName("단건 프로필 조회는 이메일과 권한을 내보내지 않는다")
    void getUser_DoesNotExposeEmailOrRole() throws Exception {
        // 이 엔드포인트는 호스트·크루 표시를 위해 비회원에게도 열려 있다.
        // 예전에는 전체 UserResponse 를 그대로 돌려줘 이메일과 role, 보유 포인트까지 나갔다.
        User other = User.builder()
                .id(7L).name("다른 사람").email("other@ex.com")
                .role("ROLE_ADMIN").points(5000).bio("안녕하세요")
                .build();
        given(userService.getById(7L)).willReturn(other);

        mockMvc.perform(get("/api/users/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("다른 사람"))
                .andExpect(jsonPath("$.bio").value("안녕하세요"))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist())
                .andExpect(jsonPath("$.points").doesNotExist());
    }

    @Test
    @WithMockUser
    @DisplayName("프로필 업데이트 성공")
    void updateProfile_Success() throws Exception {
        // given
        User updateData = User.builder().name("New Name").build();
        User updatedUser = User.builder().id(1L).name("New Name").email("test@example.com").build();
        given(userService.getCurrentUser()).willReturn(User.builder().id(1L).email("test@example.com").build());
        given(userService.updateProfile(anyLong(), any(User.class))).willReturn(updatedUser);

        // when & then
        mockMvc.perform(patch("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    @WithMockUser
    @DisplayName("타인 프로필 수정 시도 시 권한 예외 발생")
    void updateProfile_NotOwner_ReturnsForbidden() throws Exception {
        // given
        User updateData = User.builder().name("Hacked").build();
        given(userService.getCurrentUser()).willReturn(User.builder().id(2L).email("other@example.com").build());

        // when & then
        mockMvc.perform(patch("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("회원 탈퇴 요청 성공")
    void withdrawMyProfile_Success() throws Exception {
        // given
        User user = User.builder().id(1L).email("test@example.com").build();
        given(userService.getCurrentUser()).willReturn(user);

        // when & then
        mockMvc.perform(delete("/api/users/me")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("인증 정보가 없어 현재 유저를 특정할 수 없으면 프로필 조회는 401 반환")
    void getMyProfile_NoCurrentUser_Returns401() throws Exception {
        // given
        given(userService.getCurrentUser()).willThrow(new IllegalStateException("인증 정보 없음"));

        // when & then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("유저 생성 API 성공")
    void createUser_Success() throws Exception {
        // given
        User created = User.builder().id(5L).email("new@example.com").name("신규유저").build();
        given(userService.createUser(any(User.class))).willReturn(created);

        // when & then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "new@example.com", "name", "신규유저"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    @WithMockUser
    @DisplayName("인증 정보가 없어 현재 유저를 특정할 수 없으면 회원 탈퇴는 401 반환")
    void withdrawMyProfile_NoCurrentUser_Returns401() throws Exception {
        // given
        given(userService.getCurrentUser()).willThrow(new IllegalStateException("인증 정보 없음"));

        // when & then
        mockMvc.perform(delete("/api/users/me").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
