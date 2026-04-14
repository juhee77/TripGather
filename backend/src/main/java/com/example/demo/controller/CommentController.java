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
    private final GatheringRepository gatheringRepository;
    private final com.example.demo.repository.GatheringMemberRepository gatheringMemberRepository;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(commentRepository.findAllByGatheringIdOrderByCreatedAtAsc(gatheringId).stream()
                .map(CommentResponse::from)
                .toList());
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long gatheringId, @RequestBody Comment comment, Principal principal) {
        if (principal == null) throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        
        // Check privacy: if not public, only members can comment
        if (!gathering.isCommentPublic()) {
            boolean isMember = gatheringMemberRepository.existsByGatheringIdAndUserEmailAndStatus(
                    gatheringId, principal.getName(), com.example.demo.domain.MemberStatus.APPROVED);
            if (!isMember) {
                throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "모임 멤버만 댓글을 작성할 수 있습니다.");
            }
        }
        
        comment.setGathering(gathering);
        comment.setAuthor(principal.getName());
        
        return ResponseEntity.ok(CommentResponse.from(commentRepository.save(comment)));
    }
}
