package com.example.backend1.controller;

import com.example.backend1.model.Participant;
import com.example.backend1.repository.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:4200")
public class EventController {

    @Autowired
    private ParticipantRepository participantRepository;

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
}