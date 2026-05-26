package com.example.demo.repository;

import com.example.demo.domain.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    List<Itinerary> findAllByOrderByCreatedAtDesc();
    List<Itinerary> findByPublicStatusTrueOrderByCreatedAtDesc();
    List<Itinerary> findByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.data.jpa.repository.Query("UPDATE Itinerary i SET i.deleted = true WHERE i.id = :id")
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Long id);
}
