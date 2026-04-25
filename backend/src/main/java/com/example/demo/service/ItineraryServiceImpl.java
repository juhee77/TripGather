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

    @Transactional(readOnly = true)
    public List<Itinerary> getAllItineraries() {
        return itineraryRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Itinerary> getPublicItineraries() {
        return itineraryRepository.findByPublicStatusTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Itinerary> getUserJourneys(String email) {
        return itineraryRepository.findByOwnerEmailOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public Itinerary getById(Long id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ITINERARY_NOT_FOUND));
    }

    @Transactional
    public Itinerary createItinerary(Itinerary itinerary) {
        if (itinerary.getRoutePoints() != null) {
            itinerary.getRoutePoints().forEach(rp -> rp.setItinerary(itinerary));
        }
        // Initially, the author is the owner
        if (itinerary.getOwnerEmail() == null) {
            itinerary.setOwnerEmail(itinerary.getAuthorEmail());
        }
        return itineraryRepository.save(itinerary);
    }

    @Override
    @Transactional
    public Itinerary cloneItinerary(Long originalId, String ownerEmail) {
        Itinerary original = getById(originalId);
        
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
                        .itinerary(clone)
                        .build();
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
        itinerary.setTitle(update.getTitle());
        itinerary.setDescription(update.getDescription());
        itinerary.setStampImageUrl(update.getStampImageUrl());
        itinerary.setStartDate(update.getStartDate());
        itinerary.setEndDate(update.getEndDate());
        itinerary.setPublicStatus(update.isPublicStatus());
        
        // RoutePoints 업데이트 로직
        if (update.getRoutePoints() != null) {
            itinerary.getRoutePoints().clear();
            update.getRoutePoints().forEach(rp -> {
                rp.setItinerary(itinerary);
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
        
        return itineraryRepository.save(itinerary);
    }

    @Transactional
    public void deleteItinerary(Long id) {
        itineraryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Itinerary mergeItinerary(Long sourceId, Long targetId, int targetDay) {
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
                    .itinerary(target)
                    .build();
            target.getRoutePoints().add(newPoint);
            count++;
        }

        return itineraryRepository.save(target);
    }
}
