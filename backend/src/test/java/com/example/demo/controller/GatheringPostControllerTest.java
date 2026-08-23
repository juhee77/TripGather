package com.example.demo.controller;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringPost;
import com.example.demo.domain.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.GatheringPostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProfanityFilterService;
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

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GatheringPostControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GatheringPostRepository postRepository;
    @Mock
    private com.example.demo.usecase.GatheringUseCase gatheringService;
    @Mock
    private com.example.demo.usecase.GatheringMemberUseCase gatheringMemberService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfanityFilterService profanityFilterService;

    @InjectMocks
    private GatheringPostController gatheringPostController;

    private Principal principal;
    private User user;
    private Gathering gathering;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gatheringPostController)
                .setControllerAdvice(new com.example.demo.exception.GlobalExceptionHandler())
                .build();

        principal = mock(Principal.class);
        lenient().when(principal.getName()).thenReturn("user@example.com");

        user = User.builder().id(1L).email("user@example.com").name("Tester").profileImageUrl("/profile.png").build();
        gathering = Gathering.builder().id(10L).title("Test Gathering").build();
    }

    @Test
    @DisplayName("정상적인 모임 게시글 작성 성공")
    void createPost_Success() throws Exception {
        // given
        given(gatheringMemberService.isAuthorizedMember(10L, "user@example.com")).willReturn(true);
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(gatheringService.getGathering(10L)).willReturn(gathering);

        GatheringPost savedPost = GatheringPost.builder()
                .id(1L)
                .author(user)
                .gathering(gathering)
                .content("즐거운 여행 이야기")
                .imageUrl(null)
                .isPublic(true)
                .build();

        given(postRepository.save(any())).willReturn(savedPost);

        // when & then
        mockMvc.perform(post("/api/gatherings/10/posts")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": \"즐거운 여행 이야기\", \"isPublic\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("즐거운 여행 이야기"));
    }

    @Test
    @DisplayName("본인 모임 게시글 삭제 성공")
    void deletePost_Success() throws Exception {
        // given
        GatheringPost post = GatheringPost.builder()
                .id(100L)
                .author(user)
                .gathering(gathering)
                .content("삭제할 이야기")
                .build();

        given(postRepository.findById(100L)).willReturn(Optional.of(post));

        // when & then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/gatherings/10/posts/100")
                .principal(principal))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("다른 사람이 작성한 모임 게시글 삭제 시 403 Forbidden 거부")
    void deletePost_NotAuthor_Forbidden() throws Exception {
        // given
        User other = User.builder().id(2L).email("other@example.com").name("Other").build();
        GatheringPost post = GatheringPost.builder()
                .id(100L)
                .author(other)
                .gathering(gathering)
                .content("타인의 이야기")
                .build();

        given(postRepository.findById(100L)).willReturn(Optional.of(post));

        // when & then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/gatherings/10/posts/100")
                .principal(principal))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("모임 게시글 작성 시 공백 내용 제출 시 400 Bad Request 발생")
    void createPost_EmptyContent_BadRequest() throws Exception {
        // given
        GatheringPostController.PostRequest request = new GatheringPostController.PostRequest();
        request.setContent("   ");

        // when & then
        mockMvc.perform(post("/api/gatherings/10/posts")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
