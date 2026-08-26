package com.example.demo.controller;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Gathering;
import com.example.demo.domain.MemberStatus;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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
import static org.mockito.Mockito.lenient;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private com.example.demo.usecase.GatheringUseCase gatheringService;
    @Mock
    private com.example.demo.usecase.GatheringMemberUseCase gatheringMemberService;
    @Mock
    private com.example.demo.service.ProfanityFilterService profanityFilterService;

    @InjectMocks
    private CommentController commentController;

    private Gathering publicGathering;
    private Gathering privateGathering;
    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new com.example.demo.exception.GlobalExceptionHandler())
                .build();
        
        principal = mock(Principal.class);
        lenient().when(principal.getName()).thenReturn("user@example.com");

        publicGathering = Gathering.builder()
                .id(1L)
                .isCommentPublic(true)
                .build();

        privateGathering = Gathering.builder()
                .id(2L)
                .isCommentPublic(false)
                .build();
    }

    @Test
    @DisplayName("댓글 조회 성공")
    void getComments_Success() throws Exception {
        // given
        given(gatheringService.getGathering(1L)).willReturn(publicGathering);
        Comment comment = Comment.builder().id(1L).content("Hello").author("user").build();
        given(commentRepository.findAllByGatheringIdOrderByCreatedAtAsc(1L)).willReturn(List.of(comment));

        // when & then
        mockMvc.perform(get("/api/gatherings/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    @DisplayName("비공개 모임 비멤버 댓글 조회 시 빈 리스트 반환")
    void getComments_PrivateGathering_NonMember_EmptyList() throws Exception {
        // given
        given(gatheringService.getGathering(2L)).willReturn(privateGathering);
        given(gatheringMemberService.isAuthorizedMember(anyLong(), any())).willReturn(false);

        // when & then
        mockMvc.perform(get("/api/gatherings/2/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("공개 모임 댓글 작성 성공")
    void addComment_PublicGathering_Success() throws Exception {
        // given
        given(gatheringService.getGathering(1L)).willReturn(publicGathering);
        Comment savedComment = Comment.builder().id(1L).content("New Comment").author("user@example.com").build();
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when & then
        mockMvc.perform(post("/api/gatherings/1/comments")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": \"New Comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("New Comment"));
    }

    @Test
    @DisplayName("비공개 모임 멤버 댓글 작성 성공")
    void addComment_PrivateGathering_Member_Success() throws Exception {
        // given
        given(gatheringService.getGathering(2L)).willReturn(privateGathering);
        given(gatheringMemberService.isAuthorizedMember(2L, "user@example.com")).willReturn(true);
        Comment savedComment = Comment.builder().id(2L).content("Member Comment").author("user@example.com").build();
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when & then
        mockMvc.perform(post("/api/gatherings/2/comments")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": \"Member Comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Member Comment"));
    }

    @Test
    @DisplayName("비공개 모임 비멤버 댓글 작성 실패")
    void addComment_PrivateGathering_NonMember_Forbidden() throws Exception {
        // given
        given(gatheringService.getGathering(2L)).willReturn(privateGathering);
        given(gatheringMemberService.isAuthorizedMember(anyLong(), any())).willReturn(false);

        // when & then
        mockMvc.perform(post("/api/gatherings/2/comments")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": \"Stranger Comment\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("본인 댓글 삭제 성공")
    void deleteComment_Success() throws Exception {
        // given
        Comment comment = Comment.builder().id(10L).content("Hello").author("user@example.com").build();
        given(commentRepository.findById(10L)).willReturn(Optional.of(comment));

        // when & then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/gatherings/1/comments/10")
                .principal(principal))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("다른 사람의 댓글 삭제 시도 시 금지 403 반환")
    void deleteComment_NotAuthor_Forbidden() throws Exception {
        // given
        Comment comment = Comment.builder().id(10L).content("Hello").author("other@example.com").build();
        given(commentRepository.findById(10L)).willReturn(Optional.of(comment));

        // when & then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/gatherings/1/comments/10")
                .principal(principal))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("댓글 작성 시 공백 내용 제출 시 400 Bad Request 발생")
    void addComment_EmptyContent_BadRequest() throws Exception {
        mockMvc.perform(post("/api/gatherings/1/comments")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": \"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 모임에 댓글 작성 시도 시 404 Not Found 반환")
    void addComment_GatheringNotFound_ReturnsNotFound() throws Exception {
        // given
        given(gatheringService.getGathering(99L))
                .willThrow(new com.example.demo.exception.CustomException(com.example.demo.exception.ErrorCode.GATHERING_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/gatherings/99/comments")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": \"Hello Gathering\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시도 시 400 Bad Request 반환")
    void deleteComment_NotFound_ReturnsBadRequest() throws Exception {
        // given
        given(commentRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/gatherings/1/comments/99")
                .principal(principal))
                .andExpect(status().isBadRequest());
    }
}
