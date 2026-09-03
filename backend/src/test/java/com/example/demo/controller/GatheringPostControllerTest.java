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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @DisplayName("모임 게시글 작성 시 비속어 포함 시 400 Bad Request 발생")
    void createPost_ProfanityContent_BadRequest() throws Exception {
        // given
        GatheringPostController.PostRequest request = new GatheringPostController.PostRequest();
        request.setContent("씨발 게시글");

        org.mockito.BDDMockito.willThrow(new CustomException(ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어가 포함되어 있습니다."))
                .given(profanityFilterService).validateText("씨발 게시글");

        // when & then
        mockMvc.perform(post("/api/gatherings/10/posts")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 모임 게시글 삭제 시도 시 400 Bad Request 반환")
    void deletePost_NotFound_ReturnsBadRequest() throws Exception {
        // given
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/gatherings/10/posts/999")
                .principal(principal))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비회원/미승인 사용자가 모임 게시글 작성 시도 시 403 Forbidden 반환")
    void createPost_NonMember_ReturnsForbidden() throws Exception {
        // given
        given(gatheringMemberService.isAuthorizedMember(10L, "user@example.com")).willReturn(false);

        GatheringPostController.PostRequest request = new GatheringPostController.PostRequest();
        request.setContent("비회원 작성 시도");

        // when & then
        mockMvc.perform(post("/api/gatherings/10/posts")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("공개 갤러리의 게시글 목록은 비회원도 공개 게시글만 조회 가능")
    void getPosts_PublicGallery_NonMember_SeesOnlyPublicPosts() throws Exception {
        // given
        Gathering publicGallery = Gathering.builder().id(10L).title("Test Gathering").isGalleryPublic(true).build();
        com.example.demo.domain.GatheringPost publicPost = com.example.demo.domain.GatheringPost.builder()
                .id(1L).author(user).gathering(publicGallery).content("공개 게시글").isPublic(true).build();
        com.example.demo.domain.GatheringPost privatePost = com.example.demo.domain.GatheringPost.builder()
                .id(2L).author(user).gathering(publicGallery).content("비공개 게시글").isPublic(false).build();

        given(gatheringService.getGathering(10L)).willReturn(publicGallery);
        given(gatheringMemberService.isAuthorizedMember(10L, null)).willReturn(false);
        given(postRepository.findByGatheringOrderByCreatedAtDesc(publicGallery))
                .willReturn(java.util.List.of(publicPost, privatePost));

        // when & then
        mockMvc.perform(get("/api/gatherings/10/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("공개 게시글"));
    }

    @Test
    @DisplayName("공개 갤러리의 게시글 목록은 승인된 멤버에게 비공개 게시글까지 노출")
    void getPosts_Member_SeesAllPosts() throws Exception {
        // given
        Gathering publicGallery = Gathering.builder().id(10L).title("Test Gathering").isGalleryPublic(true).build();
        com.example.demo.domain.GatheringPost publicPost = com.example.demo.domain.GatheringPost.builder()
                .id(1L).author(user).gathering(publicGallery).content("공개 게시글").isPublic(true).build();
        com.example.demo.domain.GatheringPost privatePost = com.example.demo.domain.GatheringPost.builder()
                .id(2L).author(user).gathering(publicGallery).content("비공개 게시글").isPublic(false).build();

        given(gatheringService.getGathering(10L)).willReturn(publicGallery);
        given(gatheringMemberService.isAuthorizedMember(10L, "user@example.com")).willReturn(true);
        given(postRepository.findByGatheringOrderByCreatedAtDesc(publicGallery))
                .willReturn(java.util.List.of(publicPost, privatePost));

        // when & then
        mockMvc.perform(get("/api/gatherings/10/posts").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("비공개 갤러리의 게시글 목록은 비회원에게 빈 목록 반환")
    void getPosts_PrivateGallery_NonMember_ReturnsEmpty() throws Exception {
        // given
        Gathering privateGallery = Gathering.builder().id(10L).title("Test Gathering").isGalleryPublic(false).build();
        given(gatheringService.getGathering(10L)).willReturn(privateGallery);
        given(gatheringMemberService.isAuthorizedMember(10L, null)).willReturn(false);

        // when & then
        mockMvc.perform(get("/api/gatherings/10/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        org.mockito.Mockito.verify(postRepository, org.mockito.Mockito.never())
                .findByGatheringOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.any());
    }
}
