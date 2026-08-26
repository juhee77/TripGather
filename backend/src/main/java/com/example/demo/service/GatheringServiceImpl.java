package com.example.demo.service;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.GatheringMember;
import com.example.demo.domain.MemberStatus;
import com.example.demo.domain.User;
import com.example.demo.repository.GatheringLikeRepository;
import com.example.demo.repository.GatheringMemberRepository;
import com.example.demo.repository.GatheringRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ItineraryRepository;
import com.example.demo.security.SecurityService;
import com.example.demo.usecase.GatheringUseCase;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.domain.GatheringLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GatheringServiceImpl implements GatheringUseCase {
    
    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringLikeRepository gatheringLikeRepository;
    private final ItineraryRepository itineraryRepository;
    private final SecurityService securityService;
    private final ProfanityFilterService profanityFilterService;

    @Override
    @Transactional(readOnly = true)
    public List<Gathering> getAllGatherings(String location) {
        String filterLocation = (location != null && !location.trim().isEmpty() && !location.equals("전체")) ? location.trim() : null;
        return gatheringRepository.searchGatherings(null, null, filterLocation, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gathering> getPopularGatherings() {
        return gatheringRepository.findTop5ByDeletedFalseOrderByLikeCountDescCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gathering> getUserLikedGatherings(String email) {
        return gatheringRepository.findLikedGatheringsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly, String sortBy) {
        String filterLocation = (location != null && !location.trim().isEmpty() && !location.equals("전체")) ? location.trim() : null;
        return gatheringRepository.searchGatherings(query, category, filterLocation, availableOnly, sortBy);
    }

    @Override
    @Transactional
    public Gathering createGathering(Gathering gathering) {
        if (gathering.getMaxJoining() < 2) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "모임 인원은 최소 2명 이상이어야 합니다.");
        }
        if (gathering.getMaxJoining() > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "모임 인원은 최대 100명까지만 설정 가능합니다.");
        }

        if (gathering.getStartDate() != null && gathering.getEndDate() != null) {
            if (gathering.getEndDate().isBefore(gathering.getStartDate())) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "종료일은 시작일보다 빠를 수 없습니다.");
            }
        }

        if (gathering.getTitle() != null) profanityFilterService.validateText(gathering.getTitle());
        if (gathering.getLocation() != null) profanityFilterService.validateText(gathering.getLocation());

        User host = securityService.getCurrentUser();
        gathering.setHost(host);
        
        Gathering savedGathering = gatheringRepository.save(gathering);
        
        GatheringMember hostMember = GatheringMember.ofApprovedHost(savedGathering, host);
        
        savedGathering.getMembers().add(hostMember);
        gatheringMemberRepository.save(hostMember);
        savedGathering.setCurrentJoining(1); 
        
        if (gathering.getLinkedItinerary() != null && gathering.getLinkedItinerary().getId() != null) {
            itineraryRepository.findById(gathering.getLinkedItinerary().getId())
                .ifPresent(savedGathering::setLinkedItinerary);
        }
        
        return gatheringRepository.save(savedGathering);
    }

    @Override
    @Transactional
    public Gathering updateGathering(Long id, Gathering updateData) {
        validateHost(id);
        if (updateData.getStartDate() != null && updateData.getEndDate() != null) {
            if (updateData.getEndDate().isBefore(updateData.getStartDate())) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "종료일은 시작일보다 빠를 수 없습니다.");
            }
        }
        if (updateData.getTitle() != null) profanityFilterService.validateText(updateData.getTitle());
        if (updateData.getLocation() != null) profanityFilterService.validateText(updateData.getLocation());

        Gathering gathering = getGathering(id);
        gathering.setTitle(updateData.getTitle());
        gathering.setLocation(updateData.getLocation());
        gathering.setStartDate(updateData.getStartDate());
        gathering.setEndDate(updateData.getEndDate());
        gathering.setMaxJoining(updateData.getMaxJoining());
        gathering.setBgImageUrl(updateData.getBgImageUrl());
        gathering.setCategory(updateData.getCategory());
        
        gathering.setGalleryPublic(updateData.isGalleryPublic());
        gathering.setChatPublic(updateData.isChatPublic());
        gathering.setCommentPublic(updateData.isCommentPublic());
        
        if (updateData.getLinkedItinerary() != null && updateData.getLinkedItinerary().getId() != null) {
            itineraryRepository.findById(updateData.getLinkedItinerary().getId())
                .ifPresent(gathering::setLinkedItinerary);
        } else if (updateData.getLinkedItinerary() == null) {
            gathering.setLinkedItinerary(null);
        }
        
        return gathering;
    }

    @Override
    @Transactional
    public void deleteGathering(Long id) {
        validateHost(id);
        gatheringRepository.softDeleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Gathering> getHostedGatherings() {
        String email = securityService.getCurrentUserEmail();
        return gatheringRepository.findByHostEmailOrderByCreatedAtDesc(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Gathering getGathering(Long id) {
        return gatheringRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.GATHERING_NOT_FOUND, "Invalid gathering ID"));
    }

    @Override
    @Transactional
    public void likeGathering(Long id) {
        User user = securityService.getCurrentUser();
        Gathering gathering = getGathering(id);
        
        Optional<GatheringLike> existingLike = gatheringLikeRepository.findByUserAndGathering(user, gathering);
        
        if (existingLike.isPresent()) {
            gatheringLikeRepository.delete(existingLike.get());
            gathering.setLikeCount(Math.max(0, gathering.getLikeCount() - 1));
        } else {
            GatheringLike newLike = GatheringLike.of(user, gathering);
            gatheringLikeRepository.save(newLike);
            gathering.setLikeCount(gathering.getLikeCount() + 1);
        }
        gatheringRepository.save(gathering);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long gatheringId, String email) {
        if (securityService.isAnonymous()) return false;
        
        return userRepository.findByEmail(email)
                .map(user -> gatheringRepository.findById(gatheringId)
                        .map(gathering -> gatheringLikeRepository.existsByUserAndGathering(user, gathering))
                        .orElse(false))
                .orElse(false);
    }

    private void validateHost(Long gatheringId) {
        Gathering gathering = getGathering(gatheringId);
        String email = securityService.getCurrentUserEmail();
        if (!gathering.getHost().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "호스트만 접근 가능합니다.");
        }
    }
}
