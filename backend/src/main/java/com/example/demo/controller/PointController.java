package com.example.demo.controller;

import com.example.demo.dto.PointTransactionResponse;
import com.example.demo.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/transactions")
    public ResponseEntity<List<PointTransactionResponse>> getUserPointTransactions(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(pointService.getUserPointTransactions(principal.getName()));
    }
}
