package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.User;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.backend1.repository.VerificationTokenRepository;
import com.example.backend1.model.VerificationToken;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class HelloController {
    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final OrganisateurRepository organisateurRepository;


    public HelloController(UserService userService, UserRepository userRepository, OrganisateurRepository organisateurRepository, VerificationTokenRepository tokenRepository){
        this.userService=userService;
        this.userRepository = userRepository;
        this.organisateurRepository= organisateurRepository;
        this.tokenRepository = tokenRepository;

    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = userService.login(request.getEmail(), request.getPassword());

            if (token != null) {
                return ResponseEntity.ok(token);
            } else {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Email ou mot de passe incorrect"));
            }

        } catch (RuntimeException e) {
            if (e.getMessage().equals("ACCOUNT_DEACTIVATED")) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "ACCOUNT_DEACTIVATED"));
            }
            if (e.getMessage().equals("ACCOUNT_NOT_VERIFIED")) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "ACCOUNT_NOT_VERIFIED"));
            }


            return ResponseEntity.status(500)
                    .body(Map.of("error", "Erreur serveur"));
        }
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
        try {
            userService.signUp(request);
            return ResponseEntity.ok("Utilisateur créé avec succès. Vérifiez votre email.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'inscription : " + e.getMessage());
        }


    }
    @GetMapping("/verify-account")
    public ResponseEntity<?> verifyAccount(@RequestParam String token) {
        Optional<VerificationToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("verified", false, "error", "Token invalide ou expiré"));
        }

        VerificationToken verificationToken = tokenOpt.get();

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            return ResponseEntity.badRequest()
                    .body(Map.of("verified", false,
                            "error", "Le lien de vérification a expiré. Veuillez vous inscrire à nouveau."));
        }

        String email = verificationToken.getEmail();

        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            Organisateur org = orgOpt.get();
            if (org.isVerified()) {
                tokenRepository.delete(verificationToken);
                return ResponseEntity.ok(Map.of("verified", true,
                        "message", "Compte déjà vérifié ! Vous pouvez vous connecter."));
            }
            org.setVerified(true);
            organisateurRepository.save(org);
            tokenRepository.delete(verificationToken);
            return ResponseEntity.ok(Map.of("verified", true,
                    "message", "Compte vérifié avec succès ! Vous pouvez maintenant vous connecter."));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isVerified()) {
                tokenRepository.delete(verificationToken);
                return ResponseEntity.ok(Map.of("verified", true,
                        "message", "Compte déjà vérifié ! Vous pouvez vous connecter."));
            }
            user.setVerified(true);
            userRepository.save(user);
            tokenRepository.delete(verificationToken);
            return ResponseEntity.ok(Map.of("verified", true,
                    "message", "Compte vérifié avec succès ! Vous pouvez maintenant vous connecter."));
        }

        return ResponseEntity.badRequest()
                .body(Map.of("verified", false, "error", "Compte introuvable"));
    }




}
