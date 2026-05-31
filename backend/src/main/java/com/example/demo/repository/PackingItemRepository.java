package com.example.demo.repository;

import com.example.demo.domain.PackingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackingItemRepository extends JpaRepository<PackingItem, Long> {
    List<PackingItem> findByTripIdOrderByCategoryAscNameAsc(Long tripId);
    void deleteAllByTripId(Long tripId);
}
