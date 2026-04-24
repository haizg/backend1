package com.example.backend1.controller;

import com.example.backend1.model.*;
import com.example.backend1.repository.*;
import com.example.backend1.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:4200")
public class EventController {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final OrganisateurRepository organisateurRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public EventController(ParticipantRepository participantRepository,
                           EventRepository eventRepository,
                           OrganisateurRepository organisateurRepository,
                           UserRepository userRepository,
                           JwtUtil jwtUtil,
                           EmailService emailService) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
        this.organisateurRepository = organisateurRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @GetMapping
    public List<Map<String, Object>> getAllEvents() {
        return eventRepository.findByApprovedTrue().stream()
                .map(this::toEventMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinEvent(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        Long eventId = Long.valueOf(body.get("eventId").toString());

        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body("Email is required");

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ResponseEntity.notFound().build();

        if (event.getMaxParticipants() != null) {
            int count = participantRepository.countByEventAndVerifiedTrue(event);
            if (count >= event.getMaxParticipants())
                return ResponseEntity.badRequest().body(Map.of("error", "EVENT_FULL"));
        }

        Participant participant = new Participant();
        participant.setEmail(email);
        participant.setEvent(event);
        participant.setConfirmationToken(UUID.randomUUID().toString());
        participant.setVerified(false);

        userRepository.findByEmail(email).ifPresent(participant::setUser);

        participantRepository.save(participant);
        emailService.sendConfirmationEmail(email, participant.getConfirmationToken());
        return ResponseEntity.ok("Successfully registered! Check your email for verification.");
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ORGANISATEUR')")
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request,
                                         Authentication authentication) {
        String email = authentication.getName();
        Organisateur org = organisateurRepository.findByEmail(email).orElse(null);

        if (org == null || !org.isAdminVerified())
            return ResponseEntity.status(403).body(Map.of("error", "ACCOUNT_NOT_VERIFIED"));

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setDate(request.getDate());
        event.setTime(request.getTime());
        event.setLocation(request.getLocation());
        event.setImageUrl(request.getImageUrl());
        event.setMaxParticipants(request.getMaxParticipants());
        event.setProgram(request.getProgram());
        event.setOrganisateur(org);
        event.setApproved(false);
        if (request.getRiskScore() != null) {
            event.setRiskScore(request.getRiskScore());
        }
        if (request.getRiskReason() != null) {
            event.setRiskReason(request.getRiskReason());
        }
        if (request.getPredictedParticipation() != null) {
            event.setPredictedParticipation(request.getPredictedParticipation());
        }
        if (request.getPredictedParticipationReason() != null) {
            event.setPredictedParticipationReason(request.getPredictedParticipationReason());
        }
        return ResponseEntity.ok(Map.of("message", "Event created", "event", eventRepository.save(event)));
    }

    @PutMapping("/{id}/capacity-and-program")
    @PreAuthorize("hasRole('ROLE_ORGANISATEUR')")
    public ResponseEntity<?> updateCapacityAndProgram(@PathVariable Long id,
                                                      @RequestBody Map<String, Object> body,
                                                      Authentication authentication) {
        String email = authentication.getName();
        return eventRepository.findById(id).map(event -> {
            if (!event.getOrganisateurEmail().equals(email))
                return ResponseEntity.status(403).body("Unauthorized");
            if (body.containsKey("maxParticipants") && body.get("maxParticipants") != null)
                event.setMaxParticipants((Integer) body.get("maxParticipants"));
            if (body.containsKey("program"))
                event.setProgram((String) body.get("program"));
            eventRepository.save(event);
            return ResponseEntity.ok(Map.of("message", "Updated successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-events")
    public List<Map<String, Object>> getMyEvents(@RequestParam String email) {
        return participantRepository.findByEmail(email).stream()
                .map(Participant::getEvent)
                .filter(Objects::nonNull)
                .map(this::toEventMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/created")
    public List<Event> getCreatedEvents(@RequestParam String email) {
        return eventRepository.findByOrganisateurEmail(email);
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmParticipant(@RequestParam String token) {
        return participantRepository.findByConfirmationToken(token).map(p -> {
            p.setVerified(true);
            participantRepository.save(p);
            return ResponseEntity.ok("Participation confirmée !");
        }).orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<?> getEventParticipants(@PathVariable Long id) {
        return eventRepository.findById(id).map(event ->
                ResponseEntity.ok(participantRepository.findByEvent(event))
        ).orElse(ResponseEntity.notFound().build());
    }


    private Map<String, Object> toEventMap(Event event) {
        int count = participantRepository.countByEventAndVerifiedTrue(event);
        boolean isFull = event.getMaxParticipants() != null && count >= event.getMaxParticipants();
        Organisateur org = event.getOrganisateur();

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
        map.put("organisateurVerified", org != null && org.isAdminVerified());
        map.put("maxParticipants", event.getMaxParticipants());
        map.put("participantCount", count);
        map.put("isFull", isFull);
        map.put("approved", event.isApproved());
        return map;
    }
}