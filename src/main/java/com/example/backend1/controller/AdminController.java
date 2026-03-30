package com.example.backend1.controller;

import com.example.backend1.model.User;
import com.example.backend1.repository.EventRepository;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import com.example.backend1.repository.ParticipantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {
    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganisateurRepository organisateurRepository ;


    public AdminController(ParticipantRepository participantRepository,
                           EventRepository eventRepository,UserRepository userRepository,
                           OrganisateurRepository organisateurRepository) {
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.userRepository=userRepository;
        this.organisateurRepository=organisateurRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalUsers = userRepository.count();
        long totalEvents = eventRepository.count();
        long totalParticipants = participantRepository.count();
        long verifiedParticipants = participantRepository.countByVerified(true);

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "totalEvents", totalEvents,
                "totalParticipants", totalParticipants,
                "verifiedParticipants", verifiedParticipants
        ));
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



}
