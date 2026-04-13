package com.example.demo.controller;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringPost;
import com.example.demo.domain.User;
import com.example.demo.repository.GatheringPostRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.domain.MemberStatus;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gatherings")
@RequiredArgsConstructor
public class GatheringPostController {

    private final GatheringPostRepository postRepository;
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final GatheringMemberRepository gatheringMemberRepository;

    @GetMapping("/{gatheringId}/posts")
    public ResponseEntity<List<PostResponse>> getPosts(@PathVariable Long gatheringId, Principal principal) {
        validateMembership(gatheringId, principal);
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        
        List<PostResponse> responses = postRepository.findByGatheringOrderByCreatedAtDesc(gathering)
                .stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    public ResponseEntity<PostResponse> createPost(
            @PathVariable Long gatheringId,
            @RequestBody PostRequest request,
            Principal principal) {
        
        validateMembership(gatheringId, principal);
            
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));

        GatheringPost post = GatheringPost.builder()
                .author(user)
                .gathering(gathering)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        GatheringPost saved = postRepository.save(post);
        return ResponseEntity.ok(PostResponse.from(saved));
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostRequest {
        private String content;
        private String imageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostResponse {
        private Long id;
        private String authorName;
        private String authorImageUrl;
        private String content;
        private String imageUrl;
        private String createdAt;

        public static PostResponse from(GatheringPost post) {
            return PostResponse.builder()
                    .id(post.getId())
                    .authorName(post.getAuthor().getName())
                    .authorImageUrl(post.getAuthor().getProfileImageUrl())
                    .content(post.getContent())
                    .imageUrl(post.getImageUrl())
                    .createdAt(post.getCreatedAt().toString())
                    .build();
        }
    }
}
