package com.example.backend1.controller;

import com.example.backend1.model.Event;
import com.example.backend1.model.Participant;
import com.example.backend1.repository.EventRepository;
import com.example.backend1.repository.ParticipantRepository;
import jakarta.servlet.http.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id){
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }



}