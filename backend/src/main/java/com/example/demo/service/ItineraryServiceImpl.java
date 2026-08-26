package com.example.demo.service;

import com.example.demo.domain.Itinerary;
import com.example.demo.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.usecase.ItineraryUseCase;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryServiceImpl implements ItineraryUseCase {

    private final ItineraryRepository itineraryRepository;
    private final com.example.demo.repository.UserRepository userRepository;
    private final PointService pointService;
    private final ProfanityFilterService profanityFilterService;

    @Transactional(readOnly = true)
    public List<Itinerary> getAllItineraries() {
        return itineraryRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Itinerary> getPublicItineraries() {
        return itineraryRepository.findByPublicStatusTrueAndDeletedFalseOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Itinerary> getUserJourneys(String email) {
        return itineraryRepository.findByOwnerEmailAndDeletedFalseOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public Itinerary getById(Long id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ITINERARY_NOT_FOUND));
    }

    @Transactional
    public Itinerary createItinerary(Itinerary itinerary) {
        if (itinerary.getTitle() != null) profanityFilterService.validateText(itinerary.getTitle());
        if (itinerary.getDescription() != null) profanityFilterService.validateText(itinerary.getDescription());
        
        if (itinerary.getRoutePoints() != null) {
            itinerary.getRoutePoints().forEach(rp -> validateRoutePoint(rp, itinerary));
        }
        // Initially, the author is the owner
        if (itinerary.getOwnerEmail() == null) {
            itinerary.setOwnerEmail(itinerary.getAuthorEmail());
        }
        return itineraryRepository.save(itinerary);
    }

    private void validateRoutePoint(com.example.demo.domain.RoutePoint rp, Itinerary itinerary) {
        if (rp.getLabel() == null || rp.getLabel().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "경로 포인트 장소명을 입력해주세요.");
        }
        if (rp.getDayNumber() <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "일차 번호는 1 이상이어야 합니다.");
        }
        if (rp.getSequenceOrder() < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "경로 순서는 0 이상이어야 합니다.");
        }
        profanityFilterService.validateText(rp.getLabel());
        rp.setLabel(rp.getLabel().trim());
        rp.setItinerary(itinerary);
    }

    @Override
    @Transactional
    public Itinerary cloneItinerary(Long originalId, String ownerEmail) {
        Itinerary original = getById(originalId);
        if (original.isDeleted()) {
            throw new CustomException(ErrorCode.ITINERARY_NOT_FOUND, "삭제되었거나 존재하지 않는 여정입니다.");
        }
        
        if (original.getOwnerEmail() != null && original.getOwnerEmail().equals(ownerEmail)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "본인의 여정은 복제할 수 없습니다.");
        }

        // 여정 복제 저작 인센티브 지급 (+50 PTS)
        if (original.getAuthorEmail() != null && !original.getOwnerEmail().equals(ownerEmail)) {
            userRepository.findByEmail(original.getAuthorEmail()).ifPresent(authorUser -> {
                pointService.addPoints(authorUser.getId(), 50, 0, "'" + original.getTitle() + "' 여정 복제 저작 인센티브");
            });
        }
        
        Itinerary clone = Itinerary.builder()
                .title(original.getTitle() + " (Copy)")
                .description(original.getDescription())
                .author(original.getAuthor())
                .authorEmail(original.getAuthorEmail())
                .ownerEmail(ownerEmail)
                .originalId(originalId)
                .publicStatus(false) // Clones are private by default
                .location(original.getLocation())
                .startDate(original.getStartDate())
                .endDate(original.getEndDate())
                .bgImageUrl(original.getBgImageUrl())
                .stampImageUrl(original.getStampImageUrl())
                .build();

        if (original.getRoutePoints() != null) {
            original.getRoutePoints().forEach(originalPoint -> {
                com.example.demo.domain.RoutePoint clonedPoint = com.example.demo.domain.RoutePoint.builder()
                        .label(originalPoint.getLabel())
                        .dayNumber(originalPoint.getDayNumber())
                        .dayLabel(originalPoint.getDayLabel())
                        .sequenceOrder(originalPoint.getSequenceOrder())
                        .startTime(originalPoint.getStartTime())
                        .endTime(originalPoint.getEndTime())
                        .isCompleted(false)
                        .itinerary(clone)
                        .build();
                if (clone.getRoutePoints() == null) {
                    clone.setRoutePoints(new java.util.ArrayList<>());
                }
                clone.getRoutePoints().add(clonedPoint);
            });
        }

        return itineraryRepository.save(clone);
    }

    @Override
    @Transactional
    public Itinerary togglePublicStatus(Long id, String email, boolean isPublic) {
        Itinerary itinerary = getById(id);
        if (!itinerary.getOwnerEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION); // Use standard forbidden error
        }
        itinerary.setPublicStatus(isPublic);
        return itineraryRepository.save(itinerary);
    }

    @Transactional
    public Itinerary updateItinerary(Long id, Itinerary update) {
        Itinerary itinerary = getById(id);
        
        // 여정 완수 감지 (이전에 stampImageUrl이 없었으나, 새로 들어온 경우)
        boolean completedNow = (itinerary.getStampImageUrl() == null && update.getStampImageUrl() != null);
        
        if (update.getTitle() != null) {
            if (update.getTitle().trim().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여정 제목을 입력해주세요.");
            }
            profanityFilterService.validateText(update.getTitle());
            itinerary.setTitle(update.getTitle().trim());
        }
        if (update.getDescription() != null) {
            profanityFilterService.validateText(update.getDescription());
            itinerary.setDescription(update.getDescription());
        }
        itinerary.setStampImageUrl(update.getStampImageUrl());
        itinerary.setStartDate(update.getStartDate());
        itinerary.setEndDate(update.getEndDate());
        itinerary.setPublicStatus(update.isPublicStatus());
        
        // RoutePoints 업데이트 로직
        if (update.getRoutePoints() != null) {
            itinerary.getRoutePoints().clear();
            update.getRoutePoints().forEach(rp -> {
                validateRoutePoint(rp, itinerary);
                itinerary.getRoutePoints().add(rp);
            });
        }
        
        // 작성자 및 소유자 관리
        if (update.getAuthor() != null && itinerary.getAuthor() == null) {
            itinerary.setAuthor(update.getAuthor());
        }
        if (update.getAuthorEmail() != null && (itinerary.getAuthorEmail() == null || itinerary.getAuthorEmail().isEmpty())) {
            itinerary.setAuthorEmail(update.getAuthorEmail());
        }
        if (update.getOwnerEmail() != null) {
            itinerary.setOwnerEmail(update.getOwnerEmail());
        }
        
        Itinerary saved = itineraryRepository.save(itinerary);
        
        // 여정 완수 시 포인트 및 스탬프 적립 (+200 PTS & +1 STAMP)
        if (completedNow && itinerary.getOwnerEmail() != null) {
            com.example.demo.domain.User owner = userRepository.findByEmail(itinerary.getOwnerEmail())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "일정 소유자를 찾을 수 없습니다."));
            
            pointService.addPoints(
                    owner.getId(), 
                    200, 
                    1, 
                    itinerary.getTitle() != null ? itinerary.getTitle() : "여정 완수", 
                    itinerary.getId(), 
                    update.getStampImageUrl()
            );
        }
        
        return saved;
    }

    @Transactional
    public void deleteItinerary(Long id) {
        if (!itineraryRepository.existsById(id)) {
            throw new CustomException(ErrorCode.ITINERARY_NOT_FOUND, "여정을 찾을 수 없습니다.");
        }
        itineraryRepository.softDeleteById(id);
    }

    @Override
    @Transactional
    public Itinerary mergeItinerary(Long sourceId, Long targetId, int targetDay) {
        if (sourceId.equals(targetId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "자기 자신 여정과는 병합할 수 없습니다.");
        }
        Itinerary source = getById(sourceId);
        Itinerary target = getById(targetId);

        // Find existing day label for targetDay if exists
        String targetDayLabel = target.getRoutePoints().stream()
                .filter(p -> p.getDayNumber() == targetDay)
                .map(com.example.demo.domain.RoutePoint::getDayLabel)
                .findFirst()
                .orElse("Day " + targetDay);

        // Find max sequence in target day
        int maxSeq = target.getRoutePoints().stream()
                .filter(p -> p.getDayNumber() == targetDay)
                .mapToInt(com.example.demo.domain.RoutePoint::getSequenceOrder)
                .max().orElse(0);

        final int startSeq = maxSeq;
        int count = 1;

        for (com.example.demo.domain.RoutePoint sourcePoint : source.getRoutePoints()) {
            com.example.demo.domain.RoutePoint newPoint = com.example.demo.domain.RoutePoint.builder()
                    .label(sourcePoint.getLabel())
                    .dayNumber(targetDay)
                    .dayLabel(targetDayLabel)
                    .sequenceOrder(startSeq + count)
                    .startTime(sourcePoint.getStartTime())
                    .endTime(sourcePoint.getEndTime())
                    .isCompleted(false)
                    .itinerary(target)
                    .build();
            target.getRoutePoints().add(newPoint);
            count++;
        }

        return itineraryRepository.save(target);
    }

    @Override
    @Transactional
    public com.example.demo.domain.RoutePoint toggleRoutePointCompletion(Long itineraryId, Long pointId, String userEmail) {
        Itinerary itinerary = getById(itineraryId);
        com.example.demo.domain.RoutePoint point = itinerary.getRoutePoints().stream()
                .filter(p -> p.getId().equals(pointId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "경로 포인트를 찾을 수 없습니다."));

        boolean nowCompleted = !point.isCompleted();
        if (nowCompleted && point.getLabel() != null) {
            profanityFilterService.validateText(point.getLabel());
        }
        point.setCompleted(nowCompleted);

        if (nowCompleted && userEmail != null) {
            userRepository.findByEmail(userEmail).ifPresent(user -> {
                pointService.addPoints(user.getId(), 20, 0, "'" + (point.getLabel() != null ? point.getLabel() : "경로 지점") + "' 체크인 완료");
            });
        }

        itineraryRepository.save(itinerary);
        return point;
    }
}
