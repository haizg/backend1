package com.example.backend1.controller;

import com.example.backend1.model.*;
import com.example.backend1.repository.*;
import com.example.backend1.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserRepository userRepository;
    private final OrganisateurRepository organisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParticipantRepository participantRepository;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public UserController(UserRepository userRepository, OrganisateurRepository organisateurRepository,
                          PasswordEncoder passwordEncoder, ParticipantRepository participantRepository,
                          JwtUtil jwtUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.organisateurRepository = organisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.participantRepository = participantRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            Organisateur org = orgOpt.get();
            org.setNom(body.get("nom"));
            org.setPrenom(body.get("prenom"));
            if (body.get("newEmail") != null && !body.get("newEmail").isEmpty())
                org.setEmail(body.get("newEmail"));
            if (body.get("nomOrganisation") != null)
                org.setNomOrganisation(body.get("nomOrganisation"));
            organisateurRepository.save(org);
            return ResponseEntity.ok(Map.of(
                    "nom", org.getNom(), "prenom", org.getPrenom(),
                    "email", org.getEmail(), "role", org.getRole().toString(),
                    "nomOrganisation", org.getNomOrganisation() != null ? org.getNomOrganisation() : ""));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setNom(body.get("nom"));
            user.setPrenom(body.get("prenom"));
            if (body.get("newEmail") != null && !body.get("newEmail").isEmpty())
                user.setEmail(body.get("newEmail"));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                    "nom", user.getNom(), "prenom", user.getPrenom(),
                    "email", user.getEmail(), "role", user.getRole().toString()));
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            Organisateur org = orgOpt.get();
            if (!passwordEncoder.matches(oldPassword, org.getPassword()))
                return ResponseEntity.badRequest().body("Ancien mot de passe incorrect");
            org.setPassword(passwordEncoder.encode(newPassword));
            organisateurRepository.save(org);
            return ResponseEntity.ok("Mot de passe modifié avec succès");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!passwordEncoder.matches(oldPassword, user.getPassword()))
                return ResponseEntity.badRequest().body("Ancien mot de passe incorrect");
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return ResponseEntity.ok("Mot de passe modifié avec succès");
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/my-participations")
    public ResponseEntity<?> getMyParticipations(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) return ResponseEntity.status(401).build();
        String email = jwtUtil.extractEmail(token);
        var ids = participantRepository.findByEmail(email).stream()
                .map(Participant::getEventId).collect(Collectors.toList());
        return ResponseEntity.ok(ids);
    }

    @PutMapping("/deactivate")
    @Transactional
    public ResponseEntity<?> deactivateUser(HttpServletRequest request) {
        String email = jwtUtil.extractEmail(extractToken(request));

        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            Organisateur org = orgOpt.get();
            if (org.isDeactivationRequested())
                return ResponseEntity.ok(Map.of("status", "ALREADY_PENDING"));
            org.setDeactivationRequested(true);
            organisateurRepository.save(org);
            emailService.sendDeactivationRequestEmail(
                    "invitini.events@gmail.com",
                    org.getPrenom() + " " + org.getNom(), org.getEmail(), "fr");
            return ResponseEntity.ok(Map.of("status", "PENDING"));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            userRepository.save(user);
            participantRepository.deleteByEmail(email);
            emailService.sendDeactivationConfirmedEmail(email, "fr");
            return ResponseEntity.ok(Map.of("status", "DEACTIVATED"));
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/deactivation-status")
    public ResponseEntity<?> getDeactivationStatus(HttpServletRequest request) {
        String email = jwtUtil.extractEmail(extractToken(request));
        return organisateurRepository.findByEmail(email)
                .map(org -> ResponseEntity.ok(Map.of("deactivationRequested", org.isDeactivationRequested())))
                .orElse(ResponseEntity.ok(Map.of("deactivationRequested", false)));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    @GetMapping("/my-organizer-id")
    @PreAuthorize("hasRole('ROLE_ORGANISATEUR')")
    public ResponseEntity<?> getMyOrganisateurId(Authentication authentication) {
        String email = authentication.getName();
        return organisateurRepository.findByEmail(email)
                .map(org -> ResponseEntity.ok(Map.of("id", org.getId())))
                .orElse(ResponseEntity.notFound().build());
    }
}