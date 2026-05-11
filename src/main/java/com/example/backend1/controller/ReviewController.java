package com.example.backend1.controller;

import com.example.backend1.repository.ReviewRepository;
import com.example.backend1.service.ReviewService;
import com.example.backend1.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;
    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewService reviewService, JwtUtil jwtUtil, ReviewRepository reviewRepository) {
        this.reviewService = reviewService;
        this.jwtUtil = jwtUtil;
        this.reviewRepository=reviewRepository;
    }

    @GetMapping("/events/{eventId}/reviews")
    public ResponseEntity<List<Map<String, Object>>> getEventReviews(@PathVariable Long eventId) {
        return ResponseEntity.ok(reviewService.getEventReviews(eventId));
    }

    @GetMapping("/events/{eventId}/reviews/can-review")
    public ResponseEntity<Map<String, Object>> canReview(
            @PathVariable Long eventId,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();
        boolean eligible = reviewService.canUserReview(userId, eventId);
        return ResponseEntity.ok(Map.of("canReview", eligible));
    }

    @PostMapping("/events/{eventId}/reviews")
    public ResponseEntity<?> submitReview(
            @PathVariable Long eventId,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        if (userId == null) return ResponseEntity.status(401).build();

        int rating = Integer.parseInt(body.get("rating").toString());
        String comment = body.getOrDefault("comment", "").toString();

        try {
            Map<String, Object> saved = reviewService.submitReview(userId, eventId, rating, comment);
            return ResponseEntity.ok(saved);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/organizers/{orgId}/review-stats")
    public ResponseEntity<Map<String, Object>> getOrganizerReviewStats(@PathVariable Long orgId) {
        return ResponseEntity.ok(reviewService.getOrganizerReviewStats(orgId));
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7);
        try {
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    @PutMapping("/reviews/{reviewId}/flag")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> flagReview(@PathVariable Long reviewId) {
        return reviewRepository.findById(reviewId).map(review -> {
            review.setFlagged(true);
            reviewRepository.save(review);
            return ResponseEntity.ok(Map.of("message", "Avis signalé."));
        }).orElse(ResponseEntity.notFound().build());
    }
}