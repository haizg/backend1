package com.example.backend1.controller;

import com.example.backend1.model.Event;
import com.example.backend1.model.Participant;
import com.example.backend1.repository.EventRepository;
import com.example.backend1.repository.ParticipantRepository;
import jakarta.servlet.http.Part;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:4200")
public class EventController {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;

    public EventController(ParticipantRepository participantRepository,
                           EventRepository eventRepository){
        this.eventRepository=eventRepository;
        this.participantRepository=participantRepository;
    }

    @GetMapping
    public List<Event> getAllEvents(){
        return eventRepository.findAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id){
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/join")
    public ResponseEntity<?> joinEvent(@RequestBody Participant participant) {
        if (participant.getEmail() == null || participant.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (participant.getNumberOfPeople() == null || participant.getNumberOfPeople() < 0) {
            return ResponseEntity.badRequest().body("Number of people cannot be negative");
        }

        Participant savedParticipant = participantRepository.save(participant);
        return ResponseEntity.ok("Successfully registered! Check your email for verification.");
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ORGANISATEUR')")
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request){
        try {
            Event event = new Event();
            event.setTitle(request.getTitle());
            event.setDescription(request.getDescription());
            event.setCategory(request.getCategory());
            event.setDate(request.getDate());
            event.setTime(request.getTime());
            event.setLocation(request.getLocation());
            event.setImageUrl(request.getImageUrl());
            event.setMaxParticipants(request.getMaxParticipants());

            Event savedEvent = eventRepository.save(event);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Event created successfully");
            response.put("event", savedEvent);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.badRequest()
                    .body(Map.of("error","failed to create event :"+e.getMessage()));
        }}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ORGANISATEUR')")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody EventRequest request) {
        return eventRepository.findById(id)
                .map(event -> {
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
                })
                .orElse(ResponseEntity.notFound().build());

                }

                @DeleteMapping("/{id}")
                @PreAuthorize("hasRole('ROLE_ORGANISATEUR')")
                public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
                    if (eventRepository.existsById(id)) {
                        eventRepository.deleteById(id);
                        return ResponseEntity.ok(Map.of("message", "Event deleted successfully"));


                    }
                    return ResponseEntity.notFound().build();


                }


}


