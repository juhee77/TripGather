package com.example.demo.controller;

import com.example.demo.dto.PackingItemResponse;
import com.example.demo.service.PackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips/{tripId}/packing")
@RequiredArgsConstructor
public class PackingController {

    private final PackingService packingService;

    @PostMapping("/init")
    public ResponseEntity<List<PackingItemResponse>> initDefaults(@PathVariable Long tripId) {
        return ResponseEntity.ok(packingService.initDefaultItems(tripId));
    }

    @GetMapping
    public ResponseEntity<List<PackingItemResponse>> getItems(@PathVariable Long tripId) {
        return ResponseEntity.ok(packingService.getItems(tripId));
    }

    @PostMapping
    public ResponseEntity<PackingItemResponse> addItem(
            @PathVariable Long tripId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(packingService.addItem(tripId, body.get("name"), body.get("category")));
    }

    @PatchMapping("/{itemId}/toggle")
    public ResponseEntity<PackingItemResponse> toggleCheck(@PathVariable Long tripId, @PathVariable Long itemId) {
        return ResponseEntity.ok(packingService.toggleCheck(itemId));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long tripId, @PathVariable Long itemId) {
        packingService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
