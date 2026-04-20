package com.example.backend1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ContactController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<Map<String, String>> sendContactMessage(@RequestBody Map<String, String> body) {
        Map<String, String> response = new HashMap<>();

        try {
            String prenom = body.getOrDefault("prenom", "");
            String nom = body.getOrDefault("nom", "");
            String email = body.getOrDefault("email", "");
            String sujet = body.getOrDefault("sujet", "");
            String message = body.getOrDefault("message", "");

            if (email.isBlank() || message.isBlank() || prenom.isBlank()) {
                response.put("error", "Champs requis manquants.");
                return ResponseEntity.badRequest().body(response);
            }

            String fullName = prenom + " " + nom;
            emailService.sendContactEmail(fullName, email, sujet, message);

            response.put("message", "Message envoyé avec succès.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            response.put("error", "Erreur lors de l'envoi du message.");
            return ResponseEntity.status(500).body(response);
        }
    }
}