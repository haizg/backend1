package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.Participant;
import com.example.backend1.model.User;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.ParticipantRepository;
import com.example.backend1.repository.UserRepository;
import com.example.backend1.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
                          PasswordEncoder passwordEncoder,ParticipantRepository participantRepository,
                          JwtUtil jwtUtil,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.organisateurRepository = organisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.participantRepository=participantRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String,String> body){
        String email = body.get("email");
        String newNom = body.get("nom");
        String newPrenom = body.get("prenom");
        String newEmail = body.get("newEmail");
        String nomOrganisation = body.get("nomOrganisation");


        User user=userRepository.findUserByEmail(email).orElse(null);
        if (user != null){
            user.setNom(newNom);
            user.setPrenom(newPrenom);
            if (newEmail!=null && !newEmail.isEmpty()) user.setEmail(newEmail);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("nom",user.getNom(),"prenom",user.getPrenom(), "email", user.getEmail(), "role", user.getRole().toString()));
        }

        Organisateur org = organisateurRepository.findByEmail(email).orElse(null);
        if (org != null) {
            org.setNom(newNom);
            org.setPrenom(newPrenom);
            if (newEmail != null && !newEmail.isEmpty()) org.setEmail(newEmail);
            if (nomOrganisation != null) org.setNomOrganisation(nomOrganisation);
            organisateurRepository.save(org);
            return ResponseEntity.ok(Map.of("nom", org.getNom(), "prenom", org.getPrenom(), "email", org.getEmail(), "role", org.getRole().toString(), "nomOrganisation", org.getNomOrganisation() != null ? org.getNomOrganisation() : ""));
        }
        return ResponseEntity.notFound().build();
    }


    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body){
        String email = body.get("email");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");


        User user = userRepository.findUserByEmail(email).orElse(null);
        if (user != null) {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.badRequest().body("Ancien mot de passe incorrect");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return ResponseEntity.ok("Mot de passe modifié avec succès");
        }

        Organisateur org = organisateurRepository.findByEmail(email).orElse(null);
        if (org != null) {
            if (!passwordEncoder.matches(oldPassword, org.getPassword())) {
                return ResponseEntity.badRequest().body("Ancien mot de passe incorrect");
            }
            org.setPassword(passwordEncoder.encode(newPassword));
            organisateurRepository.save(org);
            return ResponseEntity.ok("Mot de passe modifié avec succès");
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/my-participations")
    public ResponseEntity<?> getMyParticipations(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        List<Participant> participations = participantRepository.findByEmail(email);
        List<Long> eventIds = participations.stream()
                .map(Participant::getEventId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(eventIds);
    }


    @PutMapping("/deactivate")
    @Transactional
    public ResponseEntity<?> deactivateUser(HttpServletRequest request) {
        String email = jwtUtil.extractEmail(
                request.getHeader("Authorization").replace("Bearer ", "")
        );

        // Organizer → request admin approval
        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            Organisateur org = orgOpt.get();
            if (org.isDeactivationRequested()) {
                return ResponseEntity.ok(Map.of("status", "ALREADY_PENDING"));
            }
            org.setDeactivationRequested(true);
            organisateurRepository.save(org);

            emailService.sendDeactivationRequestEmail(
                    "invitini.events@gmail.com",
                    org.getPrenom() + " " + org.getNom(),
                    org.getEmail(), "fr"
            );
            return ResponseEntity.ok(Map.of("status", "PENDING"));
        }

        // Regular user → deactivate immediately (keep in DB)
        Optional<User> userOpt = userRepository.findUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(false);
            userRepository.save(user);
            participantRepository.deleteByEmail(email); // clean up participations
            emailService.sendDeactivationConfirmedEmail(email, "fr");
            return ResponseEntity.ok(Map.of("status", "DEACTIVATED"));
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/deactivation-status")
    public ResponseEntity<?> getDeactivationStatus(HttpServletRequest request) {
        String email = jwtUtil.extractEmail(
                request.getHeader("Authorization").replace("Bearer ", "")
        );
        return organisateurRepository.findByEmail(email)
                .map(org -> ResponseEntity.ok(Map.of(
                        "deactivationRequested", org.isDeactivationRequested()
                )))
                .orElse(ResponseEntity.ok(Map.of("deactivationRequested", false)));
    }
}
