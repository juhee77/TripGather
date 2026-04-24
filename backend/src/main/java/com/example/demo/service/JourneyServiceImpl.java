package com.example.demo.service;

import com.example.demo.domain.Itinerary;
import com.example.demo.domain.JourneyEntry;
import com.example.demo.domain.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ItineraryRepository;
import com.example.demo.repository.JourneyEntryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.usecase.JourneyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JourneyServiceImpl implements JourneyUseCase {

    private final JourneyEntryRepository journeyEntryRepository;
    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Itinerary> getMyJourneys(String email) {
        User user = getUserByEmail(email);
        return journeyEntryRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(JourneyEntry::getItinerary)
                .toList();
    }

    @Override
    @Transactional
    public Itinerary addToMyJourney(Long itineraryId, String email) {
        User user = getUserByEmail(email);
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new CustomException(ErrorCode.ITINERARY_NOT_FOUND));

        journeyEntryRepository.findByUserIdAndItineraryId(user.getId(), itineraryId)
                .orElseGet(() -> journeyEntryRepository.save(
                        JourneyEntry.builder().user(user).itinerary(itinerary).build()
                ));
        return itinerary;
    }

    @Override
    @Transactional
    public void removeFromMyJourney(Long itineraryId, String email) {
        User user = getUserByEmail(email);
        journeyEntryRepository.findByUserIdAndItineraryId(user.getId(), itineraryId)
                .ifPresent(journeyEntryRepository::delete);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}

