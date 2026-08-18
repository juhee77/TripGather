package com.example.demo.repository;

import com.example.demo.domain.TripExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripExpenseRepository extends JpaRepository<TripExpense, Long> {
    List<TripExpense> findByTripIdOrderByExpenseDateDesc(Long tripId);
}
