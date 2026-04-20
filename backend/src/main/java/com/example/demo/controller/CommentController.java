package com.example.demo.controller;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Gathering;
import com.example.demo.dto.CommentResponse;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.GatheringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import java.security.Principal;

import java.util.List;

@RestController
@RequestMapping("/api/gatherings/{gatheringId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentRepository commentRepository;
    private final com.example.demo.usecase.GatheringUseCase gatheringService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long gatheringId, Principal principal) {
        com.example.demo.domain.Gathering gathering = gatheringService.getGathering(gatheringId);
        
        // Check privacy: if not public, only members or host can view
        if (!gathering.isCommentPublic()) {
            String email = (principal != null) ? principal.getName() : null;
            if (!gatheringService.isAuthorizedMember(gatheringId, email)) {
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }
        }

        return ResponseEntity.ok(commentRepository.findAllByGatheringIdOrderByCreatedAtAsc(gatheringId).stream()
                .map(CommentResponse::from)
                .toList());
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long gatheringId, @RequestBody com.example.demo.domain.Comment comment, Principal principal) {
        if (principal == null) throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        
        com.example.demo.domain.Gathering gathering = gatheringService.getGathering(gatheringId);
        
        // Check privacy: if not public, only members can comment
        if (!gathering.isCommentPublic()) {
            if (!gatheringService.isAuthorizedMember(gatheringId, principal.getName())) {
                throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "모임 멤버만 댓글을 작성할 수 있습니다.");
            }
        }
        
        comment.setGathering(gathering);
        comment.setAuthor(principal.getName());
        
        return ResponseEntity.ok(CommentResponse.from(commentRepository.save(comment)));
    }
}
