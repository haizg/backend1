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

    public PasswordResetController(UserRepository userRepository,
                                   OrganisateurRepository organisateurRepository,
                                   PasswordResetTokenRepository tokenRepository,
                                   EmailService emailService,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organisateurRepository = organisateurRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Email est requis"));

        // With JOINED inheritance, organisateurRepository covers organisateurs,
        // userRepository.findByEmail covers plain users (dtype = USER)
        boolean emailExists = organisateurRepository.findByEmail(email).isPresent()
                || userRepository.findByEmail(email).isPresent();

        if (!emailExists)
            return ResponseEntity.ok(Map.of(
                    "message", "Si cet email existe, un lien de réinitialisation a été envoyé."));

        try {
            String token = UUID.randomUUID().toString();
            tokenRepository.findByEmail(email).ifPresent(tokenRepository::delete);
            tokenRepository.save(new PasswordResetToken(token, email, LocalDateTime.now().plusMinutes(30)));
            emailService.sendPasswordResetEmail(email, token);
            return ResponseEntity.ok(Map.of("message", "Un email de réinitialisation a été envoyé à " + email));
        } catch (Exception e) {
            System.err.println("Error sending password reset email: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de l'envoi de l'email"));
        }
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<?> verifyToken(@RequestParam String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        if (resetToken.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Token invalide"));

        PasswordResetToken found = resetToken.get();

        if (found.isExpired())
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Token expiré"));

        if (found.isUsed())
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Token déjà utilisé"));

        return ResponseEntity.ok(Map.of("valid", true, "email", found.getEmail()));
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Token requis"));

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6)
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le mot de passe doit contenir au moins 6 caractères"));

        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(request.getToken());

        if (resetToken.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Token invalide"));

        PasswordResetToken found = resetToken.get();

        if (!found.isValid())
            return ResponseEntity.badRequest().body(Map.of("error", "Token expiré ou déjà utilisé"));

        String email = found.getEmail();
        String hashed = passwordEncoder.encode(request.getNewPassword());

        // Check organisateur first — if the email belongs to an organisateur,
        // we must NOT also update via userRepository, because with JOINED inheritance
        // userRepository.findByEmail would find the same row in the users table
        // and we'd be saving the parent twice unnecessarily.
        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            orgOpt.get().setPassword(hashed);
            organisateurRepository.save(orgOpt.get());
            found.setUsed(true);
            tokenRepository.save(found);
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            userOpt.get().setPassword(hashed);
            userRepository.save(userOpt.get());
            found.setUsed(true);
            tokenRepository.save(found);
            return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
        }

        return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur introuvable"));
    }
}

class ResetPasswordRequest {
    private String token;
    private String newPassword;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}