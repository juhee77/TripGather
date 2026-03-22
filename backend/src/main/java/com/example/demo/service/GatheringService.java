package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.User;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatheringService {
    
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final com.example.demo.repository.GatheringMemberRepository gatheringMemberRepository;

    @Transactional(readOnly = true)
    public List<Gathering> getAllGatherings(String location) {
        if (location != null && !location.trim().isEmpty() && !location.equals("전체")) {
            return gatheringRepository.findAllByLocationContainingIgnoreCaseOrderByCreatedAtDesc(location.trim());
        }
        return gatheringRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Gathering createGathering(Gathering gathering) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User host = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Host user not found"));
        
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
                .orElseThrow(() -> new IllegalArgumentException("Invalid gathering ID"));
        
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (gathering.getHost().equals(user)) {
            throw new IllegalStateException("호스트는 이미 멤버입니다.");
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
                throw new IllegalStateException("모임 정원이 초과되었습니다.");
            }
        }
        
        return gatheringRepository.save(gathering);
    }

    @Transactional
    public void approveMember(Long gatheringId, Long userId) {
        validateHost(gatheringId);
        GatheringMember member = gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member request not found"));
        member.setStatus(MemberStatus.APPROVED);
        
        // Update currentJoining
        Gathering gathering = member.getGathering();
        gathering.setCurrentJoining((int) gathering.getMembers().stream()
                .filter(m -> m.getStatus() == MemberStatus.APPROVED).count());
    }

    @Transactional
    public void rejectMember(Long gatheringId, Long userId) {
        validateHost(gatheringId);
        GatheringMember member = gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member request not found"));
        member.setStatus(MemberStatus.REJECTED);
    }

    @Transactional
    public Gathering updateGathering(Long id, Gathering updateData) {
        validateHost(id);
        Gathering gathering = gatheringRepository.findById(id).get();
        gathering.setTitle(updateData.getTitle());
        gathering.setLocation(updateData.getLocation());
        gathering.setDates(updateData.getDates());
        gathering.setMaxJoining(updateData.getMaxJoining());
        gathering.setBgImageUrl(updateData.getBgImageUrl());
        gathering.setCategory(updateData.getCategory());
        return gathering;
    }

    @Transactional
    public void deleteGathering(Long id) {
        validateHost(id);
        gatheringRepository.deleteById(id);
    }

    private void validateHost(Long gatheringId) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid gathering ID"));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!gathering.getHost().getEmail().equals(email)) {
            throw new IllegalStateException("호스트만 접근 가능합니다.");
        }
    }
}
