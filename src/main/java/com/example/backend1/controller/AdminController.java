package com.example.backend1.controller;

import com.example.backend1.model.*;
import com.example.backend1.repository.EventRepository;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import com.example.backend1.repository.ParticipantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final OrganisateurRepository organisateurRepository ;
    private final PasswordEncoder passwordEncoder;

    public AdminController(ParticipantRepository participantRepository,
                           EventRepository eventRepository, UserRepository userRepository,
                           OrganisateurRepository organisateurRepository, PasswordEncoder passwordEncoder) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.userRepository=userRepository;
        this.organisateurRepository=organisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }



    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(String.valueOf(id))) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(String.valueOf(id));
        return ResponseEntity.ok("User deleted");
    }

    @GetMapping("/organisateurs")
    public ResponseEntity<?> getAllOrganisateurs() {
        return ResponseEntity.ok(organisateurRepository.findAll());
    }

    @DeleteMapping("/organisateurs/{id}")
    public ResponseEntity<?> deleteOrganisateur(@PathVariable Long id) {
        if (!organisateurRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        organisateurRepository.deleteById(id);
        return ResponseEntity.ok("Organisateur deleted");
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



    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(true);
        user.setRole(Role.ROLE_USER);
        return ResponseEntity.ok(userRepository.save(user));
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

    @PostMapping("/organisateurs")
    public ResponseEntity<?> createOrganisateur(@RequestBody Organisateur org) {
        org.setPassword(passwordEncoder.encode(org.getPassword()));
        org.setVerified(true);
        org.setRole(Role.ROLE_ORGANISATEUR);
        return ResponseEntity.ok(organisateurRepository.save(org));
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
            event.setApproved(!event.isApproved()); // toggle
            eventRepository.save(event);
            return ResponseEntity.ok(Map.of("approved", event.isApproved()));
        }).orElse(ResponseEntity.notFound().build());
    }

// AdminController.java

    @PutMapping("/organisateurs/{id}/verify")
    public ResponseEntity<?> toggleVerifyOrganisateur(@PathVariable Long id) {
        return organisateurRepository.findById(id).map(org -> {
            org.setAdminVerified(!org.isAdminVerified()); // toggle adminVerified only
            organisateurRepository.save(org);
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
}
