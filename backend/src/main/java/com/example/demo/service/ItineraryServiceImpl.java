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
    private final com.example.demo.repository.GatheringRepository gatheringRepository;
    private final com.example.demo.security.SecurityService securityService;

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

    @Override
    @Transactional(readOnly = true)
    public Itinerary getByIdForViewer(Long id) {
        Itinerary itinerary = getById(id);

        // 공개 여정은 비로그인 사용자도 볼 수 있다. (라운지/여행 피드는 로그인 없이 탐색 가능)
        if (itinerary.isPublicStatus()) {
            return itinerary;
        }

        // 비공개 여정은 소유자, 또는 이 여정이 걸린 모임의 호스트/승인 크루만 볼 수 있다.
        if (securityService.isAnonymous()) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "비공개 여정입니다.");
        }
        String email = securityService.getCurrentUserEmail();
        boolean isOwner = email.equals(itinerary.getOwnerEmail()) || email.equals(itinerary.getAuthorEmail());
        if (isOwner || gatheringRepository.isVisibleThroughGathering(id, email)) {
            return itinerary;
        }
        throw new CustomException(ErrorCode.FORBIDDEN_ACTION, "비공개 여정입니다.");
    }

    @Transactional(readOnly = true)
    public Itinerary getById(Long id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ITINERARY_NOT_FOUND));
    }

    @Transactional
    public Itinerary createItinerary(Itinerary itinerary) {
        if (itinerary == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여정 정보가 올바르지 않습니다.");
        }
        if (itinerary.getTitle() != null && itinerary.getTitle().length() > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여정 제목은 100자 이하이어야 합니다.");
        }
        if (itinerary.getDescription() != null && itinerary.getDescription().length() > 1000) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여정 설명은 1000자 이하이어야 합니다.");
        }
        if (itinerary.getLocation() != null && itinerary.getLocation().length() > 200) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행 위치/장소는 200자 이하이어야 합니다.");
        }
        if (itinerary.getStartDate() != null && itinerary.getEndDate() != null) {
            if (itinerary.getEndDate().isBefore(itinerary.getStartDate())) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "종료일은 시작일보다 빠를 수 없습니다.");
            }
        }
        if (itinerary.getTitle() != null) profanityFilterService.validateText(itinerary.getTitle());
        if (itinerary.getDescription() != null) profanityFilterService.validateText(itinerary.getDescription());
        
        if (itinerary.getRoutePoints() != null) {
            itinerary.getRoutePoints().forEach(rp -> validateRoutePoint(rp, itinerary));
        }
        // 소유자는 인증 주체에서 채운다.
        // 클라이언트가 보낸 authorEmail 에만 의존하면 (1) 값을 빼먹었을 때 주인 없는 여정이 만들어져
        // 비공개 여정을 아무도 열람할 수 없게 되고, (2) 타인 이메일을 넣어 소유자를 위조할 수도 있다.
        if (!securityService.isAnonymous()) {
            String currentEmail = securityService.getCurrentUserEmail();
            itinerary.setOwnerEmail(currentEmail);
            if (itinerary.getAuthorEmail() == null) {
                itinerary.setAuthorEmail(currentEmail);
            }
        } else if (itinerary.getOwnerEmail() == null) {
            itinerary.setOwnerEmail(itinerary.getAuthorEmail());
        }
        return itineraryRepository.save(itinerary);
    }

    private void validateRoutePoint(com.example.demo.domain.RoutePoint rp, Itinerary itinerary) {
        if (rp.getLabel() == null || rp.getLabel().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "경로 포인트 장소명을 입력해주세요.");
        }
        if (rp.getLabel().length() > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "경로 포인트 장소명은 100자 이하이어야 합니다.");
        }
        if (rp.getDayNumber() <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "일차 번호는 1 이상이어야 합니다.");
        }
        if (rp.getSequenceOrder() < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "경로 순서는 0 이상이어야 합니다.");
        }
        if (rp.getMemo() != null) {
            if (rp.getMemo().length() > 500) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "경로 포인트 메모는 500자 이하이어야 합니다.");
            }
            profanityFilterService.validateText(rp.getMemo());
            rp.setMemo(rp.getMemo().trim());
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
                        .lat(originalPoint.getLat())
                        .lng(originalPoint.getLng())
                        .memo(originalPoint.getMemo())
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
        if (email == null || email.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "유저 이메일 정보가 올바르지 않습니다.");
        }
        Itinerary itinerary = getById(id);
        if (!itinerary.getOwnerEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION); // Use standard forbidden error
        }
        itinerary.setPublicStatus(isPublic);
        return itineraryRepository.save(itinerary);
    }

    @Transactional
    public Itinerary updateItinerary(Long id, Itinerary update) {
        if (id == null || update == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여정 ID 또는 수정 정보가 올바르지 않습니다.");
        }
        Itinerary itinerary = getById(id);
        
        if (update.getStartDate() != null && update.getEndDate() != null) {
            if (update.getEndDate().isBefore(update.getStartDate())) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "종료일은 시작일보다 빠를 수 없습니다.");
            }
        }
        
        // 여정 완수 감지 (이전에 stampImageUrl이 없었으나, 새로 들어온 경우)
        boolean completedNow = (itinerary.getStampImageUrl() == null && update.getStampImageUrl() != null);
        
        if (update.getTitle() != null) {
            if (update.getTitle().trim().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여정 제목을 입력해주세요.");
            }
            if (update.getTitle().length() > 100) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여정 제목은 100자 이하이어야 합니다.");
            }
            profanityFilterService.validateText(update.getTitle());
            itinerary.setTitle(update.getTitle().trim());
        }
        if (update.getDescription() != null) {
            if (update.getDescription().length() > 1000) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여정 설명은 1000자 이하이어야 합니다.");
            }
            profanityFilterService.validateText(update.getDescription());
            itinerary.setDescription(update.getDescription());
        }
        if (update.getLocation() != null && update.getLocation().length() > 200) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "여행 위치/장소는 200자 이하이어야 합니다.");
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
    public Itinerary mergeItinerary(Long sourceId, Long targetId, int targetDay, String requesterEmail) {
        if (targetDay <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "병합 대상 일차 번호는 1 이상이어야 합니다.");
        }
        if (sourceId == null || targetId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "병합할 여정 ID가 올바르지 않습니다.");
        }
        if (requesterEmail == null || requesterEmail.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "유저 이메일 정보가 올바르지 않습니다.");
        }
        if (sourceId.equals(targetId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "자기 자신 여정과는 병합할 수 없습니다.");
        }
        Itinerary source = getById(sourceId);
        Itinerary target = getById(targetId);

        // 병합은 대상 여정에 경로를 써 넣는 쓰기 작업이므로 소유자만 수행할 수 있다.
        if (!requesterEmail.equals(target.getOwnerEmail())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION);
        }

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
                    .lat(sourcePoint.getLat())
                    .lng(sourcePoint.getLng())
                    .memo(sourcePoint.getMemo())
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
