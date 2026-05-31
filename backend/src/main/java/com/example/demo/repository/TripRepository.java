package com.example.demo.repository;

import com.example.demo.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByOwnerEmailOrderByCreatedAtDesc(String email);
    List<Trip> findByDestinationContainingIgnoreCase(String destination);
}
