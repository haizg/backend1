package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.User;
import com.example.backend1.model.VerificationToken;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import com.example.backend1.repository.VerificationTokenRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class VerificationController {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final OrganisateurRepository organisateurRepository;

    public VerificationController(
            VerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            OrganisateurRepository organisateurRepository
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.organisateurRepository = organisateurRepository;
    }


    @GetMapping("/verify-account")
    public ResponseEntity<?> verifyAccount(@RequestParam String token) {

        System.out.println("📧 Verification request received for token: " + token);

        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            System.out.println("❌ Token not found: " + token);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "verified", false,
                            "error", "Token invalide ou expiré"
                    ));
        }

        VerificationToken verificationToken = tokenOpt.get();

        if (verificationToken.isExpired()) {
            System.out.println("❌ Token expired: " + token);

            tokenRepository.delete(verificationToken);

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "verified", false,
                            "error", "Le lien de vérification a expiré. Veuillez vous inscrire à nouveau."
                    ));
        }

        String email = verificationToken.getEmail();
        System.out.println("✅ Valid token found for email: " + email);

        boolean accountVerified = false;

        Optional<User> userOpt = userRepository.findUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.isVerified()) {
                System.out.println("ℹ️ User already verified: " + email);

                tokenRepository.delete(verificationToken);

                return ResponseEntity.ok(Map.of(
                        "verified", true,
                        "message", "Compte déjà vérifié! Vous pouvez vous connecter."
                ));
            }

            user.setVerified(true);
            userRepository.save(user);

            accountVerified = true;
            System.out.println("✅ User verified successfully: " + email);
        }

        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            Organisateur org = orgOpt.get();

            if (org.isVerified()) {
                System.out.println("ℹ️ Organisateur already verified: " + email);

                tokenRepository.delete(verificationToken);

                return ResponseEntity.ok(Map.of(
                        "verified", true,
                        "message", "Compte déjà vérifié! Vous pouvez vous connecter."
                ));
            }

            org.setVerified(true);
            organisateurRepository.save(org);

            accountVerified = true;
            System.out.println("✅ Organisateur verified successfully: " + email);
        }

        if (!accountVerified) {
            System.out.println("❌ No account found for email: " + email);
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "verified", false,
                            "error", "Compte introuvable"
                    ));
        }

        tokenRepository.delete(verificationToken);
        System.out.println("🗑️ Token deleted: " + token);

        return ResponseEntity.ok(Map.of(
                "verified", true,
                "message", "Compte vérifié avec succès! Vous pouvez maintenant vous connecter."
        ));
    }
}
