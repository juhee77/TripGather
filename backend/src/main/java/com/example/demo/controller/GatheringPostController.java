package com.example.demo.controller;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringPost;
import com.example.demo.domain.User;
import com.example.demo.repository.GatheringPostRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.UserRepository;
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

    @GetMapping("/{gatheringId}/posts")
    public ResponseEntity<List<PostResponse>> getPosts(@PathVariable Long gatheringId) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new RuntimeException("Gathering not found"));
        
        List<PostResponse> responses = postRepository.findByGatheringOrderByCreatedAtDesc(gathering)
                .stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{gatheringId}/posts")
    public ResponseEntity<PostResponse> createPost(
            @PathVariable Long gatheringId,
            @RequestBody PostRequest request,
            Principal principal) {
            
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new RuntimeException("Gathering not found"));

        GatheringPost post = GatheringPost.builder()
                .author(user)
                .gathering(gathering)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
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
