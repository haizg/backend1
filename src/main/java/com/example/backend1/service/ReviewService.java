package com.example.backend1.service;

import com.example.backend1.model.*;
import com.example.backend1.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         EventRepository eventRepository,
                         ParticipantRepository participantRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
    }

    public boolean canUserReview(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return false;

        try {
            LocalDate eventDate = LocalDate.parse(event.getDate());
            if (!eventDate.isBefore(LocalDate.now())) return false;
        } catch (Exception e) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        boolean alreadyReviewed = reviewRepository.existsByUserIdAndEventId(userId, eventId);
        if (alreadyReviewed) return false;

        return participantRepository.findByEmailAndEvent(user.getEmail(), event)
                .map(p -> p.isVerified() && p.isAttended())
                .orElse(false);
    }

    private boolean isEventPast(String dateStr) {
        try {
            LocalDate eventDate = LocalDate.parse(dateStr);
            return eventDate.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate eventDate = LocalDate.parse(dateStr, fmt);
                return eventDate.isBefore(LocalDate.now());
            } catch (DateTimeParseException e2) {
                return false;
            }
        }
    }

    public Map<String, Object> submitReview(Long userId, Long eventId, int rating, String comment) {
        if (!canUserReview(userId, eventId)) {
            throw new IllegalStateException("Not eligible to review this event");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        User user = userRepository.findById(userId).orElseThrow();
        Event event = eventRepository.findById(eventId).orElseThrow();

        Review review = new Review();
        review.setEvent(event);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        review.setUserPrenom(user.getPrenom());
        review.setUserNom(user.getNom());

        Review saved = reviewRepository.save(review);
        return toMap(saved);
    }

    public List<Map<String, Object>> getEventReviews(Long eventId) {
        return reviewRepository.findByEventId(eventId)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getOrganizerReviewStats(Long orgId) {
        List<Review> reviews = reviewRepository.findByEvent_Organisateur_Id(orgId);
        double avg = reviews.isEmpty() ? 0.0 :
                reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        avg = Math.round(avg * 10.0) / 10.0;
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", avg);
        stats.put("totalReviews", (long) reviews.size());
        return stats;
    }

    private Map<String, Object> toMap(Review r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("rating", r.getRating());
        m.put("comment", r.getComment());
        m.put("createdAt", r.getCreatedAt().toString());
        m.put("userPrenom", r.getUserPrenom());
        m.put("userNom", r.getUserNom());
        m.put("eventId", r.getEvent().getId());
        m.put("eventTitle", r.getEvent().getTitle());
        return m;
    }
}