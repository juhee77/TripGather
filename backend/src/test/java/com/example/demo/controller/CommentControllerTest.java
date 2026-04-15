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
    private GatheringRepository gatheringRepository;
    @Mock
    private GatheringMemberRepository gatheringMemberRepository;

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
        Comment comment = Comment.builder().id(1L).content("Hello").author("user").build();
        given(commentRepository.findAllByGatheringIdOrderByCreatedAtAsc(1L)).willReturn(List.of(comment));

        // when & then
        mockMvc.perform(get("/api/gatherings/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello"));
    }

    @Test
    @DisplayName("공개 모임 댓글 작성 성공")
    void addComment_PublicGathering_Success() throws Exception {
        // given
        given(gatheringRepository.findById(1L)).willReturn(Optional.of(publicGathering));
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
        given(gatheringRepository.findById(2L)).willReturn(Optional.of(privateGathering));
        given(gatheringMemberRepository.existsByGatheringIdAndUserEmailAndStatus(anyLong(), anyString(), any(MemberStatus.class)))
                .willReturn(true);
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
        given(gatheringRepository.findById(2L)).willReturn(Optional.of(privateGathering));
        given(gatheringMemberRepository.existsByGatheringIdAndUserEmailAndStatus(anyLong(), anyString(), any(MemberStatus.class)))
                .willReturn(false);

        // when & then
        mockMvc.perform(post("/api/gatherings/2/comments")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\": \"Stranger Comment\"}"))
                .andExpect(status().isForbidden());
    }
}
