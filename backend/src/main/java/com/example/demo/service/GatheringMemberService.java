package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.GatheringStatus;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.security.SecurityService;
import com.example.demo.usecase.GatheringMemberUseCase;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatheringMemberService implements GatheringMemberUseCase {

    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final SecurityService securityService;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Gathering> getJoinedGatherings() {
        String email = securityService.getCurrentUserEmail();
        return gatheringRepository.findJoinedGatherings(email);
    }

    @Transactional
    public Gathering joinGathering(Long id) {
        Gathering gathering = getGatheringById(id);
        User user = securityService.getCurrentUser();

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
        
        Gathering gathering = member.getGathering();
        int approvedCount = (int) gathering.getMembers().stream()
                .filter(m -> m.getStatus() == MemberStatus.APPROVED).count();
        gathering.setCurrentJoining(approvedCount);

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
    public void leaveGathering(Long id) {
        User user = securityService.getCurrentUser();
        GatheringMember member = gatheringMemberRepository.findByGatheringIdAndUserId(id, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN_ACTION, "해당 모임의 멤버가 아닙니다."));
        
        Gathering gathering = member.getGathering();
        if (gathering.getHost().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "호스트는 모임을 나갈 수 없습니다. 모임을 삭제해주십시오.");
        }
        
        boolean wasApproved = member.getStatus() == MemberStatus.APPROVED;
        gatheringMemberRepository.delete(member);
        
        if (wasApproved) {
            gatheringMemberRepository.flush();
            long count = gatheringMemberRepository.countByGatheringIdAndStatus(id, MemberStatus.APPROVED);
            gathering.setCurrentJoining((int) count);
            
            if (count < gathering.getMaxJoining()) {
                gathering.setStatus(GatheringStatus.OPEN);
            }
            gatheringRepository.save(gathering);
        }
    }

    @Transactional
    public void inviteMember(Long gatheringId, Long userId) {
        validateHost(gatheringId);
        
        Gathering gathering = getGatheringById(gatheringId);
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

    @Override
    @Transactional(readOnly = true)
    public boolean isAuthorizedMember(Long gatheringId, String email) {
        if (email == null || email.isEmpty() || email.equals("anonymousUser")) {
            System.out.println("[Auth] Unauthorized: No email provided or anonymousUser");
            return false;
        }
        
        Gathering gathering = gatheringRepository.findById(gatheringId).orElse(null);
        if (gathering == null) {
            System.out.println("[Auth] Unauthorized: Gathering not found " + gatheringId);
            return false;
        }
        
        // Host check
        if (gathering.getHost() != null && gathering.getHost().getEmail().equalsIgnoreCase(email)) {
            return true;
        }
        
        // Fallback for broken data: check linked itinerary author
        if (gathering.getHost() == null && gathering.getLinkedItinerary() != null) {
            if (gathering.getLinkedItinerary().getAuthorEmail() != null && 
                gathering.getLinkedItinerary().getAuthorEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        
        // Member check
        boolean isApproved = gatheringMemberRepository.existsByGatheringIdAndUserEmailAndStatus(
                gatheringId, email, MemberStatus.APPROVED);
        
        if (!isApproved) {
            System.out.println("[Auth] Unauthorized: User " + email + " is not an approved member of gathering " + gatheringId);
        }
        
        return isApproved;
    }

    private void validateHost(Long gatheringId) {
        Gathering gathering = getGatheringById(gatheringId);
        String currentUserEmail = securityService.getCurrentUserEmail();
        if (!gathering.getHost().getEmail().equals(currentUserEmail)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "호스트만 접근 가능합니다.");
        }
    }

    private Gathering getGatheringById(Long id) {
        return gatheringRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND, "Invalid gathering ID"));
    }
}
