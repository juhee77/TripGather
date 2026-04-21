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
        return itineraryRepository.save(itinerary);
    }

    @Transactional
    public Itinerary updateItinerary(Long id, Itinerary update) {
        Itinerary itinerary = getById(id);
        itinerary.setTitle(update.getTitle());
        itinerary.setDescription(update.getDescription());
        itinerary.setStampImageUrl(update.getStampImageUrl());
        
        // RoutePoints 업데이트 로직 (간단화를 위해 기존 삭제 후 재등록 패턴 또는 병합)
        if (update.getRoutePoints() != null) {
            itinerary.getRoutePoints().clear();
            update.getRoutePoints().forEach(rp -> {
                rp.setItinerary(itinerary);
                itinerary.getRoutePoints().add(rp);
            });
        }
        
        // 작성자 정보 보존 (업데이트 시 변경 불가하도록 강화)
        if (update.getAuthor() != null && itinerary.getAuthor() == null) {
            itinerary.setAuthor(update.getAuthor());
        }
        if (update.getAuthorEmail() != null && (itinerary.getAuthorEmail() == null || itinerary.getAuthorEmail().isEmpty())) {
            itinerary.setAuthorEmail(update.getAuthorEmail());
        }
        
        return itineraryRepository.save(itinerary);
    }

    @Transactional
    public void deleteItinerary(Long id) {
        itineraryRepository.deleteById(id);
    }
}
