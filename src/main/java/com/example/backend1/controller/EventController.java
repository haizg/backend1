package com.example.backend1.controller;

import com.example.backend1.model.*;
import com.example.backend1.repository.*;
import com.example.backend1.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final OrganisateurRepository organisateurRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public EventController(ParticipantRepository participantRepository,
                           EventRepository eventRepository,
                           OrganisateurRepository organisateurRepository,
                           UserRepository userRepository,
                           JwtUtil jwtUtil,
                           EmailService emailService,
                           ReviewRepository reviewRepository) {
        this.participantRepository = participantRepository;
        this.eventRepository = eventRepository;
        this.organisateurRepository = organisateurRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.reviewRepository = reviewRepository;

    }

    @GetMapping
    public List<Map<String, Object>> getAllEvents() {
        LocalDate today = LocalDate.now();
        return eventRepository.findByApprovedTrue().stream()
                .filter(event -> {
                    try {
                        LocalDate eventDate = LocalDate.parse(event.getDate());
                        return !eventDate.isBefore(today);
                    } catch (DateTimeParseException e) {
                        return true;
                    }
                })
                .map(this::toEventMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(event -> ResponseEntity.ok(toEventMap(event)))
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

        Optional<Participant> existing = participantRepository.findByEmailAndEvent(email, event);
        if (existing.isPresent()) {
            if (existing.get().isVerified()) {
                return ResponseEntity.badRequest().body(Map.of("error", "ALREADY_CONFIRMED"));
            } else {
                participantRepository.delete(existing.get());
            }
        }

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
        if (request.getRiskScore() != null) event.setRiskScore(request.getRiskScore());
        if (request.getRiskReason() != null) event.setRiskReason(request.getRiskReason());
        if (request.getPredictedParticipation() != null) event.setPredictedParticipation(request.getPredictedParticipation());
        if (request.getPredictedParticipationReason() != null) event.setPredictedParticipationReason(request.getPredictedParticipationReason());

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
                .filter(Participant::isVerified)
                .filter(p -> p.getEvent() != null)
                .map(Participant::getEvent)
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
            if (p.getEvent() != null) {
                emailService.sendQrTicketEmail(
                        p.getEmail(),
                        p.getConfirmationToken(),
                        p.getEvent().getTitle(),
                        p.getEvent().getDate(),
                        p.getEvent().getLocation()
                );
            }
            return ResponseEntity.ok("Participation confirmée !");
        }).orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<?> getEventParticipants(@PathVariable Long id) {
        return eventRepository.findById(id).map(event -> {
            List<Map<String, Object>> result = participantRepository
                    .findByEvent(event).stream().map(p -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("email", p.getEmail());
                        map.put("verified", p.isVerified());
                        map.put("attended", p.isAttended());
                        map.put("confirmationToken", p.getConfirmationToken());
                        return map;
                    }).collect(Collectors.toList());
            return ResponseEntity.ok(result);
        }).orElse(ResponseEntity.notFound().build());
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
        map.put("organisateurId", org != null ? org.getId() : null);
        map.put("maxParticipants", event.getMaxParticipants());
        map.put("participantCount", count);
        map.put("isFull", isFull);
        map.put("approved", event.isApproved());
        map.put("program", event.getProgram());
        map.put("riskScore", event.getRiskScore());
        map.put("riskReason", event.getRiskReason());
        map.put("predictedParticipation", event.getPredictedParticipation());
        map.put("predictedParticipationReason", event.getPredictedParticipationReason());
        return map;
    }

    @DeleteMapping("/{eventId}/unregister")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> unregisterFromEvent(
            @PathVariable Long eventId,
            Authentication authentication) {

        String email = authentication.getName();

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ResponseEntity.notFound().build();

        try {
            LocalDate eventDate = LocalDate.parse(event.getDate());
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            if (!eventDate.isAfter(tomorrow)) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "DEADLINE_PASSED",
                                "message", "Désinscription impossible moins de 24h avant l'événement.")
                );
            }
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "INVALID_DATE"));
        }

        Participant participant = participantRepository
                .findByEmailAndEvent(email, event)
                .orElse(null);

        if (participant == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "NOT_REGISTERED", "message", "Vous n'êtes pas inscrit à cet événement.")
            );
        }
        participantRepository.delete(participant);
        return ResponseEntity.ok(Map.of("message", "Désinscription réussie."));
    }

    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('ROLE_ORGANISATEUR')")
    public ResponseEntity<?> getMyReviews(Authentication authentication) {
        String email = authentication.getName();
        Organisateur org = organisateurRepository.findByEmail(email).orElse(null);
        if (org == null) return ResponseEntity.notFound().build();

        List<Event> myEvents = eventRepository.findByOrganisateur(org);
        List<Map<String, Object>> reviews = new ArrayList<>();

        for (Event event : myEvents) {
            reviewRepository.findByEvent(event).forEach(review -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("eventTitle", event.getTitle());
                map.put("eventId", event.getId());
                map.put("rating", review.getRating());
                map.put("comment", review.getComment());
                map.put("userPrenom", review.getUserPrenom());
                map.put("userNom", review.getUserNom());
                reviews.add(map);
            });
        }
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/scan")
    public ResponseEntity<?> scanQrCode(@RequestParam String token) {
        return participantRepository.findByConfirmationToken(token).map(p -> {
            if (!p.isVerified()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "UNVERIFIED",
                                "message", "Ce participant n'a pas confirmé sa participation."));
            }

            Event event = p.getEvent();
            try {
                LocalDate eventDate = LocalDate.parse(event.getDate());
                LocalDate today = LocalDate.now();
                if (!eventDate.equals(today)) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "NOT_TODAY",
                                    "message", "Le scan n'est disponible que le jour de l'événement."));
                }
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "INVALID_DATE", "message", "Date invalide."));
            }

            if (p.isAttended()) {
                return ResponseEntity.ok(Map.of(
                        "status", "ALREADY_SCANNED",
                        "message", "Ce participant est déjà enregistré comme présent.",
                        "email", p.getEmail()
                ));
            }
            p.setAttended(true);
            participantRepository.save(p);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Présence enregistrée avec succès !",
                    "email", p.getEmail()
            ));
        }).orElse(ResponseEntity.badRequest()
                .body(Map.of("error", "NOT_FOUND", "message", "QR code invalide.")));
    }


    @GetMapping("/{eventId}/my-ticket")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyTicket(@PathVariable Long eventId,
                                         Authentication authentication) {
        String email = authentication.getName();
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ResponseEntity.notFound().build();

        return participantRepository.findByEmailAndEvent(email, event)
                .map(p -> ResponseEntity.ok(Map.of(
                        "token", p.getConfirmationToken(),
                        "attended", p.isAttended(),
                        "verified", p.isVerified()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

}