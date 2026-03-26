package com.example.backend1.controller;

import com.example.backend1.model.Event;
import com.example.backend1.model.Participant;
import com.example.backend1.repository.EventRepository;
import com.example.backend1.repository.ParticipantRepository;
import com.example.backend1.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:4200")
public class EventController {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public EventController(ParticipantRepository participantRepository,
                           EventRepository eventRepository, JwtUtil jwtUtil, EmailService emailService) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @GetMapping
    public List<Map<String, Object>> getAllEvents() {
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

            boolean isFull = false;
            if (event.getMaxParticipants() != null) {
                isFull = participantCount >= event.getMaxParticipants();
            }
            eventData.put("isFull", isFull);

            return eventData;

        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }





    @PostMapping("/join")
    public ResponseEntity<?> joinEvent(@RequestBody Participant participant) {

        if (participant.getEmail() == null || participant.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }


        String token= UUID.randomUUID().toString();
        participant.setConfirmationToken(token);
        participant.setVerified(false);

        participantRepository.save(participant);

        emailService.sendConfirmationEmail(participant.getEmail(),token);

        return ResponseEntity.ok("Successfully registered! Check your email for verification.");
    }







    @PostMapping
    @PreAuthorize("hasRole('ROLE_ORGANISATEUR')")
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request,Authentication authentication) {

        try {
            String organisateurEmail = authentication.getName();


            Event event = new Event();
            event.setTitle(request.getTitle());
            event.setDescription(request.getDescription());
            event.setCategory(request.getCategory());
            event.setDate(request.getDate());
            event.setTime(request.getTime());
            event.setLocation(request.getLocation());
            event.setImageUrl(request.getImageUrl());
            event.setMaxParticipants(request.getMaxParticipants());
            event.setOrganisateurEmail(organisateurEmail);

            Event savedEvent = eventRepository.save(event);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event created successfully");
            response.put("event", savedEvent);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "failed to create event :" + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ORGANISATEUR')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody EventRequest request, Authentication authentication) {
        String currentUserEmail = authentication.getName();

        return eventRepository.findById(id)
                .map(event -> {
                    if (!event.getOrganisateurEmail().equals(currentUserEmail)) {
                        return ResponseEntity.status(403)
                                .body(Map.of("error", "Vous ne pouvez modifier que vos propres événements"));
                    }

                    event.setTitle(request.getTitle());
                    event.setDescription(request.getDescription());
                    event.setCategory(request.getCategory());
                    event.setDate(request.getDate());
                    event.setTime(request.getTime());
                    event.setLocation(request.getLocation());
                    event.setImageUrl(request.getImageUrl());
                    event.setMaxParticipants(request.getMaxParticipants());

                    Event updatedEvent = eventRepository.save(event);

                    return ResponseEntity.ok(Map.of(
                            "message", "Event updated successfully",
                            "event", updatedEvent
                    ));

                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ORGANISATEUR')")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id, Authentication authentication) {
        String currentUserEmail = authentication.getName();

        return eventRepository.findById(id)
                .map(event -> {

                    if (!event.getOrganisateurEmail().equals(currentUserEmail)) {
                        return ResponseEntity.status(403)
                                .body(Map.of("error", "Vous ne pouvez supprimer que vos propres événements"));
                    }

                    eventRepository.deleteById(id);
                    return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/my-events")
    public List<Event> getMyEvents(@RequestParam String email){
        List<Participant> participations=participantRepository.findByEmail(email);
        List<Long> eventIds=participations.stream()
                .map(p-> p.getEventId())
                .collect(Collectors.toList());
        return eventRepository.findAllById(eventIds);

    }

    @GetMapping("/created")
    public List<Event> getCreatedEvents(@RequestParam String email){
        return eventRepository.findByOrganisateurEmail(email);
    }


    @GetMapping("/confirm")
    public ResponseEntity<?> confirmPArticipant (@RequestParam String token){
        return participantRepository.findByConfirmationToken(token)
                .map(participant -> {
                    participant.setVerified(true);
                    participantRepository.save(participant);
                    return ResponseEntity.ok("Participation confirmée avec succès!");
                })
                .orElse(ResponseEntity.badRequest().build());
    }



}






