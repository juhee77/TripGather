package com.example.demo.controller;

import com.example.demo.dto.StampResponse;
import com.example.demo.usecase.StampUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stamps")
@RequiredArgsConstructor
public class StampController {

    private final StampUseCase stampService;

    @GetMapping("/me")
    public ResponseEntity<List<StampResponse>> getMyStamps() {
        return ResponseEntity.ok(stampService.getMyStamps());
    }
}
