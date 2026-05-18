package com.example.backend1.controller;

import com.example.backend1.model.*;
import com.example.backend1.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganisateurRepository organisateurRepository;
    private final EmailService emailService;
    private final ReviewRepository reviewRepository;

    public AdminController(ParticipantRepository participantRepository,
                           EventRepository eventRepository,
                           UserRepository userRepository,
                           OrganisateurRepository organisateurRepository,
                           EmailService emailService,
                           ReviewRepository reviewRepository) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.organisateurRepository = organisateurRepository;
        this.emailService = emailService;
        this.reviewRepository=reviewRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findByRole(Role.ROLE_USER));
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            participantRepository.deleteByEmail(user.getEmail());
            userRepository.delete(user);
            return ResponseEntity.ok("Utilisateur supprimé");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/organisateurs")
    public ResponseEntity<?> getAllOrganisateurs() {
        return ResponseEntity.ok(organisateurRepository.findAll());
    }

    @DeleteMapping("/organisateurs/{id}")
    @Transactional
    public ResponseEntity<?> deleteOrganisateur(@PathVariable Long id) {
        return organisateurRepository.findById(id).map(org -> {
            List<Event> events = eventRepository.findByOrganisateur(org);
            events.forEach(event -> participantRepository.deleteByEvent(event));
            eventRepository.deleteAll(events);
            organisateurRepository.delete(org);
            return ResponseEntity.ok(Map.of(
                    "message", "Organisateur supprimé",
                    "eventsDeleted", events.size()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{id}/participations")
    public ResponseEntity<?> getUserParticipations(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            List<Event> events = participantRepository.findByUser(user).stream()
                    .map(Participant::getEvent)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(events);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/organisateur/{id}/events")
    public ResponseEntity<?> getOrganisateurEvents(@PathVariable Long id) {
        return organisateurRepository.findById(id)
                .map(org -> ResponseEntity.ok(eventRepository.findByOrganisateur(org)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updated) {
        return userRepository.findById(id).map(user -> {
            user.setNom(updated.getNom());
            user.setPrenom(updated.getPrenom());
            user.setEmail(updated.getEmail());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/organisateurs/{id}")
    public ResponseEntity<?> updateOrganisateur(@PathVariable Long id,
                                                @RequestBody Organisateur updated) {
        return organisateurRepository.findById(id).map(org -> {
            org.setNom(updated.getNom());
            org.setPrenom(updated.getPrenom());
            org.setEmail(updated.getEmail());
            org.setNomOrganisation(updated.getNomOrganisation());
            return ResponseEntity.ok(organisateurRepository.save(org));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/events/all")
    public List<Map<String, Object>> getAllEventsAdmin() {
        return eventRepository.findAll().stream().map(event -> {
            int count = participantRepository.countByEventAndVerifiedTrue(event);
            boolean isFull = event.getMaxParticipants() != null && count >= event.getMaxParticipants();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", event.getId());
            map.put("title", event.getTitle());
            map.put("description", event.getDescription());
            map.put("date", event.getDate());
            map.put("time", event.getTime());
            map.put("location", event.getLocation());
            map.put("imageUrl", event.getImageUrl());
            map.put("category", event.getCategory());
            map.put("organisateurEmail", event.getOrganisateurEmail());
            map.put("maxParticipants", event.getMaxParticipants());
            map.put("participantCount", count);
            map.put("approved", event.isApproved());
            map.put("isFull", isFull);
            map.put("program", event.getProgram());
            map.put("riskScore", event.getRiskScore());
            map.put("riskReason", event.getRiskReason());
            map.put("predictedParticipation", event.getPredictedParticipation());
            map.put("predictedParticipationReason", event.getPredictedParticipationReason());
            return map;
        }).collect(Collectors.toList());
    }

    @PutMapping("/events/{id}/approve")
    public ResponseEntity<?> approveEvent(@PathVariable Long id) {
        return eventRepository.findById(id).map(event -> {
            event.setApproved(true);
            eventRepository.save(event);
            emailService.sendEventApprovedEmail(event.getOrganisateurEmail(), event.getTitle());
            return ResponseEntity.ok(Map.of("approved", true));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/events/{id}/reject")
    @Transactional
    public ResponseEntity<?> rejectEvent(@PathVariable Long id) {
        return eventRepository.findById(id).map(event -> {
            emailService.sendEventRejectedEmail(event.getOrganisateurEmail(), event.getTitle());
            participantRepository.deleteByEvent(event);
            eventRepository.delete(event);
            return ResponseEntity.ok(Map.of("rejected", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/organisateurs/{id}/verify")
    public ResponseEntity<?> verifyOrganisateur(@PathVariable Long id) {
        return organisateurRepository.findById(id).map(org -> {
            org.setAdminVerified(!org.isAdminVerified());
            organisateurRepository.save(org);
            if (org.isAdminVerified()) emailService.sendOrganisateurVerifiedEmail(org.getEmail());
            return ResponseEntity.ok(Map.of("adminVerified", org.isAdminVerified()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(Map.of(
                "totalUsers", userRepository.count(),
                "totalEvents", eventRepository.count(),
                "totalParticipants", participantRepository.count(),
                "verifiedParticipants", participantRepository.countByVerified(true),
                "pendingEvents", eventRepository.findByApprovedFalse().size()
        ));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> adminDeleteEvent(@PathVariable Long id) {
        return eventRepository.findById(id).map(event -> {
            participantRepository.deleteByEvent(event);
            eventRepository.delete(event);
            return ResponseEntity.ok("Event deleted");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update-event/{id}")
    public ResponseEntity<?> adminUpdateEvent(@PathVariable Long id,
                                              @RequestBody EventRequest request) {
        return eventRepository.findById(id).map(event -> {
            event.setTitle(request.getTitle());
            event.setDescription(request.getDescription());
            event.setCategory(request.getCategory());
            event.setDate(request.getDate());
            event.setTime(request.getTime());
            event.setLocation(request.getLocation());
            event.setImageUrl(request.getImageUrl());
            event.setMaxParticipants(request.getMaxParticipants());
            event.setProgram(request.getProgram());
            eventRepository.save(event);
            return ResponseEntity.ok(Map.of("message", "Event updated"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/organisateurs/deactivation-requests")
    public ResponseEntity<?> getDeactivationRequests() {
        return ResponseEntity.ok(organisateurRepository.findByDeactivationRequestedTrue());
    }

    @PutMapping("/organisateurs/{id}/deactivate/approve")
    @Transactional
    public ResponseEntity<?> approveDeactivation(@PathVariable Long id) {
        return organisateurRepository.findById(id).map(org -> {
            org.setActive(false);
            org.setDeactivationRequested(false);
            organisateurRepository.save(org);
            emailService.sendDeactivationConfirmedEmail(org.getEmail(), "fr");
            return ResponseEntity.ok(Map.of("message", "Compte désactivé."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/organisateurs/{id}/deactivate/reject")
    public ResponseEntity<?> rejectDeactivation(@PathVariable Long id) {
        return organisateurRepository.findById(id).map(org -> {
            org.setDeactivationRequested(false);
            organisateurRepository.save(org);
            emailService.sendDeactivationRejectedEmail(org.getEmail(), "fr");
            return ResponseEntity.ok(Map.of("message", "Demande refusée."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reviews/flagged")
    public ResponseEntity<?> getFlaggedReviews() {
        List<Review> flagged = reviewRepository.findByFlaggedTrue();
        List<Map<String, Object>> result = flagged.stream().map(r -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", r.getId());
            map.put("comment", r.getComment());
            map.put("rating", r.getRating());
            map.put("userPrenom", r.getUserPrenom());
            map.put("userNom", r.getUserNom());
            map.put("eventTitle", r.getEvent().getTitle());
            map.put("eventId", r.getEvent().getId());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        return reviewRepository.findById(id).map(r -> {
            reviewRepository.delete(r);
            return ResponseEntity.ok(Map.of("message", "Avis supprimé."));
        }).orElse(ResponseEntity.notFound().build());
    }
}