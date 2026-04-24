package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.User;
import com.example.demo.repository.GatheringLikeRepository;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.usecase.GatheringUseCase;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.domain.GatheringStatus;
import com.example.demo.domain.GatheringLike;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatheringServiceImpl implements GatheringUseCase {
    
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringLikeRepository gatheringLikeRepository;

    @Transactional(readOnly = true)
    public List<Gathering> getAllGatherings(String location) {
        String filterLocation = (location != null && !location.trim().isEmpty() && !location.equals("전체")) ? location.trim() : null;
        return gatheringRepository.searchGatherings(null, null, filterLocation, null);
    }

    @Transactional(readOnly = true)
    public List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly) {
        String filterLocation = (location != null && !location.trim().isEmpty() && !location.equals("전체")) ? location.trim() : null;
        return gatheringRepository.searchGatherings(query, category, filterLocation, availableOnly);
    }

    @Transactional
    public Gathering createGathering(Gathering gathering) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User host = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "Host user not found"));
        
        gathering.setHost(host);
        // Save the gathering first to get the ID
        Gathering savedGathering = gatheringRepository.save(gathering);
        
        // Add host as an APPROVED member
        GatheringMember hostMember = GatheringMember.builder()
                .gathering(savedGathering)
                .user(host)
                .status(MemberStatus.APPROVED)
                .build();
        
        savedGathering.getMembers().add(hostMember);
        gatheringMemberRepository.save(hostMember);
        
        // Host is the first member
        savedGathering.setCurrentJoining(1); 
        
        return gatheringRepository.save(savedGathering);
    }

    @Transactional
    public Gathering joinGathering(Long id) {
        Gathering gathering = gatheringRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND, "Invalid gathering ID"));
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (gathering.getHost().equals(user)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "호스트는 이미 멤버입니다.");
        }

        boolean alreadyApplied = gathering.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!alreadyApplied) {
            if (gathering.getMembers().size() < gathering.getMaxJoining()) {
                GatheringMember member = GatheringMember.builder()
                        .gathering(gathering)
                        .user(user)
                        .status(MemberStatus.PENDING)
                        .build();
                gathering.getMembers().add(member);
                gatheringMemberRepository.save(member);
            } else {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "모임 정원이 초과되었습니다.");
            }
        }
        
        return gatheringRepository.save(gathering);
    }

    @Transactional
    public void approveMember(Long gatheringId, Long userId) {
        validateHost(gatheringId);
        
        GatheringMember member = gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_REQUEST_NOT_FOUND));
                
        if (member.getGathering().getHost().getId().equals(userId)) {
            throw new CustomException(ErrorCode.SELF_ACTION_NOT_ALLOWED, "호스트 본인을 승인/거절할 수 없습니다.");
        }

        member.setStatus(MemberStatus.APPROVED);
        
        // Update currentJoining
        Gathering gathering = member.getGathering();
        int approvedCount = (int) gathering.getMembers().stream()
                .filter(m -> m.getStatus() == MemberStatus.APPROVED).count();
        gathering.setCurrentJoining(approvedCount);

        // Auto Close logic
        if (approvedCount >= gathering.getMaxJoining()) {
            gathering.setStatus(GatheringStatus.CLOSED);
        }
    }

    @Transactional
    public void rejectMember(Long gatheringId, Long userId) {
        validateHost(gatheringId);
        GatheringMember member = gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_REQUEST_NOT_FOUND));
                
        if (member.getGathering().getHost().getId().equals(userId)) {
            throw new CustomException(ErrorCode.SELF_ACTION_NOT_ALLOWED, "호스트 본인을 승인/거절할 수 없습니다.");
        }

        member.setStatus(MemberStatus.REJECTED);
    }

    @Transactional
    public Gathering updateGathering(Long id, Gathering updateData) {
        validateHost(id);
        Gathering gathering = gatheringRepository.findById(id).get();
        gathering.setTitle(updateData.getTitle());
        gathering.setLocation(updateData.getLocation());
        gathering.setStartDate(updateData.getStartDate());
        gathering.setEndDate(updateData.getEndDate());
        gathering.setMaxJoining(updateData.getMaxJoining());
        gathering.setBgImageUrl(updateData.getBgImageUrl());
        gathering.setCategory(updateData.getCategory());
        
        // Update privacy settings
        gathering.setGalleryPublic(updateData.isGalleryPublic());
        gathering.setChatPublic(updateData.isChatPublic());
        gathering.setCommentPublic(updateData.isCommentPublic());
        
        return gathering;
    }

    @Transactional
    public void deleteGathering(Long id) {
        validateHost(id);
        gatheringRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Gathering> getJoinedGatherings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return gatheringRepository.findJoinedGatherings(email);
    }

    @Transactional(readOnly = true)
    public List<Gathering> getHostedGatherings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return gatheringRepository.findByHostEmailOrderByCreatedAtDesc(email);
    }

    @Transactional
    public void leaveGathering(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        GatheringMember member = gatheringMemberRepository.findByGatheringIdAndUserId(id, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN_ACTION, "해당 모임의 멤버가 아닙니다."));
        
        Gathering gathering = member.getGathering();
        if (gathering.getHost().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "호스트는 모임을 나갈 수 없습니다. 모임을 삭제해주십시오.");
        }
        
        boolean wasApproved = member.getStatus() == MemberStatus.APPROVED;
        gatheringMemberRepository.delete(member);
        
        if (wasApproved) {
            // Flush and update count
            gatheringMemberRepository.flush();
            long count = gatheringMemberRepository.countByGatheringIdAndStatus(id, MemberStatus.APPROVED);
            gathering.setCurrentJoining((int) count);
            
            // Re-open if below max
            if (count < gathering.getMaxJoining()) {
                gathering.setStatus(GatheringStatus.OPEN);
            }
            
            gatheringRepository.save(gathering);
        }
    }

    @Transactional(readOnly = true)
    public Gathering getGathering(Long id) {
        return gatheringRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND, "Invalid gathering ID"));
    }

    @Transactional
    public void likeGathering(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        Gathering gathering = gatheringRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND));
        
        java.util.Optional<GatheringLike> existingLike = gatheringLikeRepository.findByUserAndGathering(user, gathering);
        
        if (existingLike.isPresent()) {
            gatheringLikeRepository.delete(existingLike.get());
            gathering.setLikeCount(Math.max(0, gathering.getLikeCount() - 1));
        } else {
            GatheringLike newLike = GatheringLike.builder()
                    .user(user)
                    .gathering(gathering)
                    .build();
            gatheringLikeRepository.save(newLike);
            gathering.setLikeCount(gathering.getLikeCount() + 1);
        }
        gatheringRepository.save(gathering);
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long gatheringId, String email) {
        if (email == null || email.isEmpty() || email.equals("anonymousUser")) return false;
        
        return userRepository.findByEmail(email)
                .map(user -> gatheringRepository.findById(gatheringId)
                        .map(gathering -> gatheringLikeRepository.existsByUserAndGathering(user, gathering))
                        .orElse(false))
                .orElse(false);
    }

    @Override
    @Transactional
    public void inviteMember(Long gatheringId, Long userId) {
        validateHost(gatheringId);
        
        Gathering gathering = gatheringRepository.findById(gatheringId).get();
        User guest = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "초대할 사용자를 찾을 수 없습니다."));
        
        boolean alreadyMember = gathering.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId));
        
        if (alreadyMember) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 멤버이거나 신청 중인 사용자입니다.");
        }
        
        if (gathering.getCurrentJoining() >= gathering.getMaxJoining()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "모임 정원이 가득 차서 초대할 수 없습니다.");
        }
        
        GatheringMember newMember = GatheringMember.builder()
                .gathering(gathering)
                .user(guest)
                .status(MemberStatus.APPROVED)
                .build();
        
        gathering.getMembers().add(newMember);
        gatheringMemberRepository.save(newMember);
        
        int approvedCount = (int) gathering.getMembers().stream()
                .filter(m -> m.getStatus() == MemberStatus.APPROVED).count();
        gathering.setCurrentJoining(approvedCount);
        
        if (approvedCount >= gathering.getMaxJoining()) {
            gathering.setStatus(GatheringStatus.CLOSED);
        }
        
        gatheringRepository.save(gathering);
    }

    private void validateHost(Long gatheringId) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND, "Invalid gathering ID"));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!gathering.getHost().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "호스트만 접근 가능합니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAuthorizedMember(Long gatheringId, String email) {
        if (email == null || email.isEmpty() || email.equals("anonymousUser")) return false;
        
        Gathering gathering = gatheringRepository.findById(gatheringId).orElse(null);
        if (gathering == null) return false;
        
        if (gathering.getHost().getEmail().equals(email)) return true;
        
        return gatheringMemberRepository.existsByGatheringIdAndUserEmailAndStatus(
                gatheringId, email, com.example.demo.domain.MemberStatus.APPROVED);
    }
}
