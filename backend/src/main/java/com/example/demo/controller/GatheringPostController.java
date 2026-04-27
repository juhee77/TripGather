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
    private final com.example.demo.usecase.GatheringUseCase gatheringService;
    private final com.example.demo.usecase.GatheringMemberUseCase gatheringMemberService;
    private final UserRepository userRepository;

    @GetMapping("/{gatheringId}/posts")
    public ResponseEntity<List<PostResponse>> getPosts(@PathVariable Long gatheringId, Principal principal) {
        Gathering gathering = gatheringService.getGathering(gatheringId);
        
        String email = (principal != null) ? principal.getName() : null;
        boolean isMember = gatheringMemberService.isAuthorizedMember(gatheringId, email);

        // If gallery is private and user is not a member, hide everything
        if (!gathering.isGalleryPublic() && !isMember) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }

        List<GatheringPost> posts = postRepository.findByGatheringOrderByCreatedAtDesc(gathering);
        
        List<PostResponse> responses = posts.stream()
                .filter(post -> isMember || post.isPublic())
                .map(PostResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{gatheringId}/posts")
    public ResponseEntity<PostResponse> createPost(
            @PathVariable Long gatheringId,
            @RequestBody PostRequest request,
            Principal principal) {
        
        if (principal == null) throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        if (!gatheringMemberService.isAuthorizedMember(gatheringId, principal.getName())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "승인된 크루원만 접근 가능합니다.");
        }
            
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Gathering gathering = gatheringService.getGathering(gatheringId);

        GatheringPost post = GatheringPost.builder()
                .author(user)
                .gathering(gathering)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .isPublic(request.isPublic())
                .build();

        GatheringPost saved = postRepository.save(post);
        return ResponseEntity.ok(PostResponse.from(saved));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostRequest {
        private String content;
        private String imageUrl;
        private boolean isPublic;
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
        private boolean isPublic;
        private String createdAt;

        public static PostResponse from(GatheringPost post) {
            return PostResponse.builder()
                    .id(post.getId())
                    .authorName(post.getAuthor().getName())
                    .authorImageUrl(post.getAuthor().getProfileImageUrl())
                    .content(post.getContent())
                    .imageUrl(post.getImageUrl())
                    .isPublic(post.isPublic())
                    .createdAt(post.getCreatedAt().toString())
                    .build();
        }
    }
}
