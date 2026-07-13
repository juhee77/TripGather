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


    @GetMapping
    public ResponseEntity<List<GatheringResponse>> getAllGatherings(@RequestParam(required = false) String location, java.security.Principal principal) {
        return ResponseEntity.ok(gatheringService.getAllGatherings(location).stream()
                .map(g -> {
                    boolean isLiked = principal != null && gatheringService.isLikedByUser(g.getId(), principal.getName());
                    boolean hasCheckedIn = checkUserHasCheckedIn(g.getId(), principal);
                    return GatheringResponse.from(g, isLiked, hasCheckedIn);
                })
                .collect(Collectors.toList()));
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
            java.security.Principal principal) {
        return ResponseEntity.ok(gatheringService.searchGatherings(query, category, location, availableOnly).stream()
                .map(g -> {
                    boolean isLiked = principal != null && gatheringService.isLikedByUser(g.getId(), principal.getName());
                    boolean hasCheckedIn = checkUserHasCheckedIn(g.getId(), principal);
                    return GatheringResponse.from(g, isLiked, hasCheckedIn);
                })
                .collect(Collectors.toList()));
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
        return ResponseEntity.ok(gatheringService.getHostedGatherings().stream()
                .map(g -> {
                    boolean isLiked = principal != null && gatheringService.isLikedByUser(g.getId(), principal.getName());
                    boolean hasCheckedIn = checkUserHasCheckedIn(g.getId(), principal);
                    return GatheringResponse.from(g, isLiked, hasCheckedIn);
                })
                .collect(Collectors.toList()));
    }

    @GetMapping({"/my/joined", "/my/participating"})
    public ResponseEntity<List<GatheringResponse>> getMyJoinedGatherings(java.security.Principal principal) {
        return ResponseEntity.ok(gatheringMemberService.getJoinedGatherings().stream()
                .map(g -> {
                    boolean isLiked = principal != null && gatheringService.isLikedByUser(g.getId(), principal.getName());
                    boolean hasCheckedIn = checkUserHasCheckedIn(g.getId(), principal);
                    return GatheringResponse.from(g, isLiked, hasCheckedIn);
                })
                .collect(Collectors.toList()));
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
}
