package com.tripgather.controller;

import jakarta.validation.Valid;

import com.tripgather.domain.Comment;
import com.tripgather.domain.Gathering;
import com.tripgather.dto.CommentResponse;
import com.tripgather.repository.CommentRepository;
import com.tripgather.repository.GatheringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.tripgather.exception.CustomException;
import com.tripgather.exception.ErrorCode;
import java.security.Principal;

import java.util.List;

@RestController
@RequestMapping("/api/gatherings/{gatheringId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentRepository commentRepository;
    private final com.tripgather.usecase.GatheringUseCase gatheringService;
    private final com.tripgather.usecase.GatheringMemberUseCase gatheringMemberService;
    private final com.tripgather.service.ProfanityFilterService profanityFilterService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long gatheringId, Principal principal) {
        com.tripgather.domain.Gathering gathering = gatheringService.getGathering(gatheringId);
        
        // Check privacy: if not public, only members or host can view
        if (!gathering.isCommentPublic()) {
            String email = (principal != null) ? principal.getName() : null;
            if (!gatheringMemberService.isAuthorizedMember(gatheringId, email)) {
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
        }

        return ResponseEntity.ok(commentRepository.findAllByGatheringIdOrderByCreatedAtAsc(gatheringId).stream()
                .map(CommentResponse::from)
                .toList());
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long gatheringId, @Valid @RequestBody com.tripgather.dto.CommentRequest request, Principal principal) {
        if (principal == null) throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "댓글 내용을 입력해주세요.");
        }

        profanityFilterService.validateText(request.getContent());

        com.tripgather.domain.Gathering gathering = gatheringService.getGathering(gatheringId);
        
        // Check privacy: if not public, only members can comment
        if (!gathering.isCommentPublic()) {
            if (!gatheringMemberService.isAuthorizedMember(gatheringId, principal.getName())) {
                throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "모임 멤버만 댓글을 작성할 수 있습니다.");
            }
        }
        
        com.tripgather.domain.Comment comment = request.toEntity();
        comment.setGathering(gathering);
        comment.setAuthor(principal.getName());
        
        return ResponseEntity.ok(CommentResponse.from(commentRepository.save(comment)));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long gatheringId, @PathVariable Long commentId, Principal principal) {
        if (principal == null) throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "댓글을 찾을 수 없습니다."));

        if (!comment.getAuthor().equals(principal.getName())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "본인의 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
        return ResponseEntity.noContent().build();
    }
}
