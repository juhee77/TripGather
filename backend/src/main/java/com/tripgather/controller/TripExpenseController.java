package com.tripgather.controller;

import jakarta.validation.Valid;

import com.tripgather.dto.TripExpenseRequest;
import com.tripgather.dto.TripExpenseResponse;
import com.tripgather.dto.TripSettlementResponse;
import com.tripgather.service.TripExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripExpenseController {

    private final TripExpenseService tripExpenseService;

    @PostMapping("/expenses")
    public ResponseEntity<TripExpenseResponse> addExpense(
            Principal principal,
            @Valid @RequestBody TripExpenseRequest request) {
        return ResponseEntity.ok(tripExpenseService.addExpense(principal.getName(), request));
    }

    @GetMapping("/{tripId}/expenses")
    public ResponseEntity<List<TripExpenseResponse>> getExpensesByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripExpenseService.getExpensesByTrip(tripId));
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, Principal principal) {
        tripExpenseService.deleteExpense(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tripId}/settlement")
    public ResponseEntity<TripSettlementResponse> calculateSettlement(
            @PathVariable Long tripId,
            @RequestParam(defaultValue = "1") int memberCount) {
        return ResponseEntity.ok(tripExpenseService.calculateSettlement(tripId, memberCount));
    }
}
