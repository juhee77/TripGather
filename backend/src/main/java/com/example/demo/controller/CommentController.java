package com.example.demo.controller;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Gathering;
import com.example.demo.dto.CommentResponse;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.GatheringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.domain.MemberStatus;
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
    private final GatheringMemberRepository gatheringMemberRepository;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long gatheringId, Principal principal) {
        validateMembership(gatheringId, principal);
        return ResponseEntity.ok(commentRepository.findAllByGatheringIdOrderByCreatedAtAsc(gatheringId).stream()
                .map(CommentResponse::from)
                .toList());
    }

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long gatheringId, @RequestBody Comment comment, Principal principal) {
        validateMembership(gatheringId, principal);
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        
        comment.setGathering(gathering);
        comment.setAuthor(principal.getName()); // Use authenticated email/name
        
        return ResponseEntity.ok(CommentResponse.from(commentRepository.save(comment)));
    }

    private void validateMembership(Long gatheringId, Principal principal) {
        if (principal == null) throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
                
        boolean isHost = gathering.getHost().getEmail().equals(principal.getName());
        boolean isApproved = gatheringMemberRepository.existsByGatheringIdAndUserEmailAndStatus(gatheringId, principal.getName(), MemberStatus.APPROVED);
        
        if (!isHost && !isApproved) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "승인된 크루원만 접근 가능합니다.");
        }
    }
}
