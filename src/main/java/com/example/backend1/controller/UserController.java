package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.User;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserRepository userRepository;
    private final OrganisateurRepository organisateurRepository;
    private final PasswordEncoder passwordEncoder;


    public UserController(UserRepository userRepository, OrganisateurRepository organisateurRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organisateurRepository = organisateurRepository;
        this.passwordEncoder = passwordEncoder;
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


    @PutMapping("change-password")
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


}
