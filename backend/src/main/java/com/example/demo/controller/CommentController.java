package com.example.demo.controller;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Gathering;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.GatheringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gatherings/{gatheringId}/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CommentController {

    private final CommentRepository commentRepository;
    private final GatheringRepository gatheringRepository;

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(commentRepository.findAllByGatheringIdOrderByCreatedAtAsc(gatheringId));
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@PathVariable Long gatheringId, @RequestBody Comment comment) {
        Gathering gathering = gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid gathering ID"));
        
        comment.setGathering(gathering);
        // By default set standard test user if not provided from frontend auth
        if(comment.getAuthor() == null || comment.getAuthor().isEmpty()) {
            comment.setAuthor("Jihyun (지현)");
        }
        
        return ResponseEntity.ok(commentRepository.save(comment));
    }
}
