package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.PasswordResetToken;
import com.example.backend1.model.User;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.PasswordResetTokenRepository;
import com.example.backend1.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final OrganisateurRepository organisateurRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(
            UserRepository userRepository,
            OrganisateurRepository organisateurRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.organisateurRepository = organisateurRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email est requis"));
        }

        boolean emailExists = userRepository.findUserByEmail(email).isPresent()
                || organisateurRepository.findByEmail(email).isPresent();

        if (!emailExists) {
            return ResponseEntity.ok(Map.of(
                    "message", "Si cet email existe, un lien de réinitialisation a été envoyé."
            ));
        }

        try {
            String token = UUID.randomUUID().toString();

            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30);

            Optional<PasswordResetToken> existingToken = tokenRepository.findByEmail(email);
            existingToken.ifPresent(tokenRepository::delete);

            PasswordResetToken resetToken = new PasswordResetToken(token, email, expiryDate);
            tokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(email, token);

            return ResponseEntity.ok(Map.of(
                    "message", "Un email de réinitialisation a été envoyé à " + email
            ));

        } catch (Exception e) {
            System.err.println("Error sending password reset email: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Erreur lors de l'envoi de l'email"));
        }
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<?> verifyToken(@RequestParam String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        if (resetToken.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "error", "Token invalide"));
        }

        PasswordResetToken foundToken = resetToken.get();

        if (foundToken.isExpired()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "error", "Token expiré"));
        }

        if (foundToken.isUsed()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "error", "Token déjà utilisé"));
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "email", foundToken.getEmail()
        ));
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token requis"));
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le mot de passe doit contenir au moins 6 caractères"));
        }

        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(request.getToken());

        if (resetToken.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token invalide"));
        }

        PasswordResetToken foundToken = resetToken.get();

        if (!foundToken.isValid()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token expiré ou déjà utilisé"));
        }

        String email = foundToken.getEmail();

        String hashedPassword = passwordEncoder.encode(request.getNewPassword());

        boolean passwordUpdated = false;

        Optional<User> userOpt = userRepository.findUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(hashedPassword);
            userRepository.save(user);
            passwordUpdated = true;
        }

        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            Organisateur org = orgOpt.get();
            org.setPassword(hashedPassword);
            organisateurRepository.save(org);
            passwordUpdated = true;
        }

        if (!passwordUpdated) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Utilisateur introuvable"));
        }

        foundToken.setUsed(true);
        tokenRepository.save(foundToken);

        return ResponseEntity.ok(Map.of(
                "message", "Mot de passe réinitialisé avec succès"
        ));
    }
}

class ResetPasswordRequest {
    private String token;
    private String newPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}