package com.example.demo.controller;

import com.example.demo.domain.Gathering;
import com.example.demo.dto.GatheringResponse;
import com.example.demo.usecase.GatheringUseCase;
import com.example.demo.usecase.GatheringMemberUseCase;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.StampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringUseCase gatheringService;
    private final GatheringMemberUseCase gatheringMemberService;
    private final UserRepository userRepository;
    private final StampRepository stampRepository;
    private final com.example.demo.repository.GatheringLikeRepository gatheringLikeRepository;


    @GetMapping
    public ResponseEntity<List<GatheringResponse>> getAllGatherings(@RequestParam(required = false) String location, java.security.Principal principal) {
        return ResponseEntity.ok(toResponses(gatheringService.getAllGatherings(location), principal));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<GatheringResponse>> getPopularGatherings(java.security.Principal principal) {
        return ResponseEntity.ok(toResponses(gatheringService.getPopularGatherings(), principal));
    }

    @GetMapping("/me/liked")
    public ResponseEntity<List<GatheringResponse>> getUserLikedGatherings(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        // 좋아요 목록이므로 isLiked 는 항상 true 다.
        return ResponseEntity.ok(
                toResponses(gatheringService.getUserLikedGatherings(principal.getName()), principal, true));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GatheringResponse> getGathering(@PathVariable Long id, java.security.Principal principal) {
        Gathering gathering = gatheringService.getGathering(id);
        boolean isLiked = principal != null && gatheringService.isLikedByUser(id, principal.getName());
        boolean hasCheckedIn = checkUserHasCheckedIn(id, principal);
        return ResponseEntity.ok(GatheringResponse.from(gathering, isLiked, hasCheckedIn));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GatheringResponse>> searchGatherings(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean availableOnly,
            @RequestParam(required = false, defaultValue = "LATEST") String sortBy,
            java.security.Principal principal) {
        return ResponseEntity.ok(toResponses(gatheringService.searchGatherings(query, category, location, availableOnly, sortBy), principal));
    }

    @PostMapping
    public ResponseEntity<GatheringResponse> createGathering(@RequestBody com.example.demo.dto.GatheringRequest request) {
        return ResponseEntity.ok(GatheringResponse.from(gatheringService.createGathering(request.toEntity())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GatheringResponse> updateGathering(@PathVariable Long id, @RequestBody com.example.demo.dto.GatheringRequest request) {
        return ResponseEntity.ok(GatheringResponse.from(gatheringService.updateGathering(id, request.toEntity())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGathering(@PathVariable Long id) {
        gatheringService.deleteGathering(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my/hosted")
    public ResponseEntity<List<GatheringResponse>> getMyHostedGatherings(java.security.Principal principal) {
        return ResponseEntity.ok(toResponses(gatheringService.getHostedGatherings(), principal));
    }

    @GetMapping({"/my/joined", "/my/participating"})
    public ResponseEntity<List<GatheringResponse>> getMyJoinedGatherings(java.security.Principal principal) {
        return ResponseEntity.ok(toResponses(gatheringMemberService.getJoinedGatherings(), principal));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<GatheringResponse> joinGathering(@PathVariable Long id) {
        return ResponseEntity.ok(GatheringResponse.from(gatheringMemberService.joinGathering(id)));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGathering(@PathVariable Long id) {
        gatheringMemberService.leaveGathering(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/members/{userId}/approve")
    public ResponseEntity<Void> approveMember(@PathVariable Long id, @PathVariable Long userId) {
        gatheringMemberService.approveMember(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/members/{userId}/reject")
    public ResponseEntity<Void> rejectMember(@PathVariable Long id, @PathVariable Long userId) {
        gatheringMemberService.rejectMember(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeGathering(@PathVariable Long id) {
        gatheringService.likeGathering(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/is-liked")
    public ResponseEntity<Boolean> isLikedByUser(@PathVariable Long id, java.security.Principal principal) {
        if (principal == null) return ResponseEntity.ok(false);
        return ResponseEntity.ok(gatheringService.isLikedByUser(id, principal.getName()));
    }

    @PostMapping("/{id}/invite/{userId}")
    public ResponseEntity<Void> inviteMember(@PathVariable Long id, @PathVariable Long userId) {
        gatheringMemberService.inviteMember(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/is-authorized")
    public ResponseEntity<Boolean> isAuthorizedMember(@PathVariable Long id, java.security.Principal principal) {
        if (principal == null) return ResponseEntity.ok(false);
        return ResponseEntity.ok(gatheringMemberService.isAuthorizedMember(id, principal.getName()));
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<Void> checkinStandbyGathering(
            @PathVariable Long id,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        gatheringMemberService.checkinStandbyGathering(id, lat, lng, force);
        return ResponseEntity.ok().build();
    }


    private boolean checkUserHasCheckedIn(Long gatheringId, java.security.Principal principal) {
        if (principal == null) return false;
        return userRepository.findByEmail(principal.getName())
                .map(u -> stampRepository.existsByUserIdAndGatheringId(u.getId(), gatheringId))
                .orElse(false);
    }

    /**
     * 목록 응답 매핑.
     *
     * 행마다 좋아요/체크인 여부를 개별 조회하면 모임 수에 비례해 쿼리가 늘어난다(N+1).
     * 뷰어의 좋아요 집합과 스탬프 집합을 요청당 한 번씩만 읽어 두고, 행에서는 집합 조회만 한다.
     */
    private List<GatheringResponse> toResponses(List<Gathering> gatherings, java.security.Principal principal) {
        return toResponses(gatherings, principal, false);
    }

    /**
     * @param forceLiked 좋아요 목록처럼 모든 행이 좋아요 상태임이 자명한 경우 true
     */
    private List<GatheringResponse> toResponses(List<Gathering> gatherings,
                                                java.security.Principal principal,
                                                boolean forceLiked) {
        java.util.Set<Long> likedIds = java.util.Collections.emptySet();
        java.util.Set<Long> checkedInIds = java.util.Collections.emptySet();

        if (principal != null) {
            java.util.Optional<com.example.demo.domain.User> viewer =
                    userRepository.findByEmail(principal.getName());
            if (viewer.isPresent()) {
                Long viewerId = viewer.get().getId();
                if (!forceLiked) {
                    likedIds = new java.util.HashSet<>(gatheringLikeRepository.findLikedGatheringIdsByUserId(viewerId));
                }
                checkedInIds = new java.util.HashSet<>(stampRepository.findStampedGatheringIdsByUserId(viewerId));
            }
        }

        final java.util.Set<Long> liked = likedIds;
        final java.util.Set<Long> checkedIn = checkedInIds;
        return gatherings.stream()
                .map(g -> GatheringResponse.from(g,
                        forceLiked || liked.contains(g.getId()),
                        checkedIn.contains(g.getId())))
                .collect(Collectors.toList());
    }
}
