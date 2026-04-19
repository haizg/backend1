package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.User;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class HelloController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final OrganisateurRepository organisateurRepository;


    public HelloController(UserService userService, UserRepository userRepository, OrganisateurRepository organisateurRepository){
        this.userService=userService;
        this.userRepository = userRepository;
        this.organisateurRepository= organisateurRepository;
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


    }}
