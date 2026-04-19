package com.example.backend1.controller;

import com.example.backend1.model.*;
import com.example.backend1.repository.EventRepository;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import com.example.backend1.repository.ParticipantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {
    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganisateurRepository organisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AdminController(ParticipantRepository participantRepository,
                           EventRepository eventRepository, UserRepository userRepository,
                           OrganisateurRepository organisateurRepository, PasswordEncoder passwordEncoder,
                           EmailService emailService) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.organisateurRepository = organisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findByRoleNot(Role.ROLE_ADMIN));
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(String.valueOf(id)).map(user -> {
            // Delete all participations of this user
            participantRepository.deleteByEmail(user.getEmail());
            // Delete the user
            userRepository.deleteById(String.valueOf(id));
            return ResponseEntity.ok("Utilisateur et ses participations supprimés");
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/organisateurs")
    public ResponseEntity<?> getAllOrganisateurs() {
        return ResponseEntity.ok(organisateurRepository.findAll());
    }

    // AdminController.java — replace deleteOrganisateur
    @DeleteMapping("/organisateurs/{id}")
    @Transactional
    public ResponseEntity<?> deleteOrganisateur(@PathVariable Long id) {
        return organisateurRepository.findById(id).map(org -> {
            // 1. Delete participations of the organizer as a participant
            participantRepository.deleteByEmail(org.getEmail());

            // 2. Delete participants of each event this organizer created
            List<Event> orgEvents = eventRepository.findByOrganisateurEmail(org.getEmail());
            for (Event event : orgEvents) {
                participantRepository.deleteByEventId(event.getId());
            }

            // 3. Delete the events
            eventRepository.deleteAll(orgEvents);

            // 4. Delete the organizer
            organisateurRepository.deleteById(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Organisateur et toutes ses données supprimés",
                    "eventsDeleted", orgEvents.size()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{id}/participations")
    public ResponseEntity<?> getUserParticipations(@PathVariable Long id) {
        User user = userRepository.findById(String.valueOf(id)).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        List<Participant> participations = participantRepository.findByEmail(user.getEmail());
        List<Long> eventIds = participations.stream().map(Participant::getEventId).toList();
        List<Event> events = eventRepository.findAllById(eventIds);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/organisateur/{id}/events")
    public ResponseEntity<?> getOrganisateurEvents(@PathVariable Long id) {
        return organisateurRepository.findById(id)
                .map(org -> ResponseEntity.ok(eventRepository.findByOrganisateurEmail(org.getEmail())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updated) {
        return userRepository.findById(String.valueOf(id)).map(user -> {
            user.setNom(updated.getNom());
            user.setPrenom(updated.getPrenom());
            user.setEmail(updated.getEmail());
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/organisateurs/{id}")
    public ResponseEntity<?> updateOrganisateur(@PathVariable Long id, @RequestBody Organisateur updated) {
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
        List<Event> events = eventRepository.findAll();
        return events.stream().map(event -> {
            int participantCount = participantRepository.countByEventIdAndVerifiedTrue(event.getId());
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("id", event.getId());
            eventData.put("title", event.getTitle());
            eventData.put("description", event.getDescription());
            eventData.put("date", event.getDate());
            eventData.put("time", event.getTime());
            eventData.put("location", event.getLocation());
            eventData.put("imageUrl", event.getImageUrl());
            eventData.put("category", event.getCategory());
            eventData.put("organisateurEmail", event.getOrganisateurEmail());
            eventData.put("maxParticipants", event.getMaxParticipants());
            eventData.put("participantCount", participantCount);
            eventData.put("approved", event.isApproved());
            boolean isFull = event.getMaxParticipants() != null && participantCount >= event.getMaxParticipants();
            eventData.put("isFull", isFull);
            return eventData;
        }).collect(Collectors.toList());
    }

    @PutMapping("/events/{id}/approve")
    public ResponseEntity<?> approveEvent(@PathVariable Long id) {
        return eventRepository.findById(id).map(event -> {
            event.setApproved(true);
            eventRepository.save(event);

            // Notify organiser their event is now live
            emailService.sendEventApprovedEmail(
                    event.getOrganisateurEmail(),
                    event.getTitle()
            );

            return ResponseEntity.ok(Map.of("approved", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/organisateurs/{id}/verify")
    public ResponseEntity<?> toggleVerifyOrganisateur(@PathVariable Long id) {
        return organisateurRepository.findById(id).map(org -> {
            org.setAdminVerified(!org.isAdminVerified());
            organisateurRepository.save(org);

            if (org.isAdminVerified()) {
                emailService.sendOrganisateurVerifiedEmail(org.getEmail());
            }

            return ResponseEntity.ok(Map.of("adminVerified", org.isAdminVerified()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalUsers = userRepository.count();
        long totalEvents = eventRepository.count();
        long totalParticipants = participantRepository.count();
        long verifiedParticipants = participantRepository.countByVerified(true);
        long pendingEvents = eventRepository.findByApprovedFalse().size();

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalEvents", totalEvents,
                "totalParticipants", totalParticipants,
                "verifiedParticipants", verifiedParticipants,
                "pendingEvents", pendingEvents
        ));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> adminDeleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) return ResponseEntity.notFound().build();
        participantRepository.deleteByEventId(id);
        eventRepository.deleteById(id);
        return ResponseEntity.ok("Event deleted");
    }

    @PutMapping("/update-event/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> adminUpdateEvent(
            @PathVariable Long id,
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
            return ResponseEntity.ok(Map.of("message", "Event updated by admin"));
        }).orElse(ResponseEntity.notFound().build());
    }

// Add to AdminController.java

    @GetMapping("/organisateurs/deactivation-requests")
    public ResponseEntity<?> getDeactivationRequests() {
        List<Organisateur> pending = organisateurRepository
                .findByDeactivationRequestedTrue();
        return ResponseEntity.ok(pending);
    }

    // AdminController.java — replace approveDeactivation
    @PutMapping("/organisateurs/{id}/deactivate/approve")
    @Transactional
    public ResponseEntity<?> approveDeactivation(@PathVariable Long id) {
        return organisateurRepository.findById(id).map(org -> {
            // Mark account as inactive
            org.setActive(false);
            org.setDeactivationRequested(false);
            organisateurRepository.save(org);

            // Delete their participations in events (as a participant)
            participantRepository.deleteByEmail(org.getEmail());

            // Notify them
            emailService.sendDeactivationConfirmedEmail(org.getEmail(), "fr");

            return ResponseEntity.ok(Map.of("message", "Compte désactivé."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/organisateurs/{id}/deactivate/reject")
    public ResponseEntity<?> rejectDeactivation(@PathVariable Long id) {
        return organisateurRepository.findById(id).map(org -> {
            org.setDeactivationRequested(false);
            organisateurRepository.save(org);
            emailService.sendDeactivationRejectedEmail(org.getEmail(), "fr"); // ← ADD
            return ResponseEntity.ok(Map.of("message", "Demande refusée."));
        }).orElse(ResponseEntity.notFound().build());
    }
}