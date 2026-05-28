package com.example.backend1.controller;

import com.example.backend1.model.Event;
import com.example.backend1.repository.EventRepository;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizers")
public class OrganizerPublicController {

    private final OrganisateurRepository organisateurRepository;
    private final EventRepository eventRepository;
    private final ReviewService reviewService;

    public OrganizerPublicController(OrganisateurRepository organisateurRepository,
                                     EventRepository eventRepository,
                                     ReviewService reviewService) {
        this.organisateurRepository = organisateurRepository;
        this.eventRepository = eventRepository;
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrganizerProfile(@PathVariable Long id) {
        return organisateurRepository.findById(id)
                .map(org -> {
                    Map<String, Object> profile = new LinkedHashMap<>();
                    profile.put("id", org.getId());
                    profile.put("nom", org.getNom());
                    profile.put("prenom", org.getPrenom());
                    profile.put("email", org.getEmail());
                    profile.put("nomOrganisation", org.getNomOrganisation());
                    profile.put("adminVerified", org.isAdminVerified());
                    Map<String, Object> stats = reviewService.getOrganizerReviewStats(id);
                    profile.put("averageRating", stats.get("averageRating"));
                    profile.put("totalReviews", stats.get("totalReviews"));

                    List<Map<String, Object>> eventList = eventRepository.findByOrganisateur(org)
                            .stream()
                            .filter(Event::isApproved)
                            .map(e -> {
                                Map<String, Object> em = new LinkedHashMap<>();
                                em.put("id", e.getId());
                                em.put("title", e.getTitle());
                                em.put("date", e.getDate());
                                em.put("time", e.getTime());
                                em.put("location", e.getLocation());
                                em.put("category", e.getCategory());
                                em.put("imageUrl", e.getImageUrl());
                                em.put("maxParticipants", e.getMaxParticipants());
                                return em;
                            }).collect(Collectors.toList());

                    profile.put("events", eventList);
                    return ResponseEntity.ok(profile);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}