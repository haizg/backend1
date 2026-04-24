package com.example.backend1.controller;

import com.example.backend1.model.*;
import com.example.backend1.model.User;
import com.example.backend1.repository.*;
import com.example.backend1.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final OrganisateurRepository organisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, OrganisateurRepository organisateurRepository,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       VerificationTokenRepository verificationTokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.organisateurRepository = organisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            Organisateur org = orgOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    org.getEmail(), org.getPassword(),
                    List.of(new SimpleGrantedAuthority(org.getRole().name())));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), user.getPassword(),
                    List.of(new SimpleGrantedAuthority(user.getRole().name())));
        }

        throw new UsernameNotFoundException("User not found: " + email);
    }

    public String login(String email, String password) {
        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()) {
            Organisateur org = orgOpt.get();
            if (!passwordEncoder.matches(password, org.getPassword())) return null;
            if (!org.isActive()) throw new RuntimeException("ACCOUNT_DEACTIVATED");
            if (!org.isVerified()) throw new RuntimeException("ACCOUNT_NOT_VERIFIED");
            return jwtUtil.generateToken(org.getEmail(), org.getRole(),
                    org.getNom(), org.getPrenom(), org.isVerified(), org.isAdminVerified(), org.getId());
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!passwordEncoder.matches(password, user.getPassword())) return null;
            if (!user.isActive()) throw new RuntimeException("ACCOUNT_DEACTIVATED");
            if (!user.isVerified()) throw new RuntimeException("ACCOUNT_NOT_VERIFIED");
            return jwtUtil.generateToken(user.getEmail(), user.getRole(),
                    user.getNom(), user.getPrenom(), user.isVerified(), false, user.getId());
        }

        return null;
    }

    public ResponseEntity<?> signUp(SignUpRequest request) {
        String email = request.getEmail();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String userType;

        if ("ROLE_ORGANISATEUR".equals(request.getRole())) {
            Organisateur org = new Organisateur();
            org.setNom(request.getNom());
            org.setPrenom(request.getPrenom());
            org.setEmail(email);
            org.setPassword(encodedPassword);
            org.setNomOrganisation(request.getNomOrganisation());
            organisateurRepository.save(org);
            userType = "Organisateur";
        } else {
            User user = new User();
            user.setNom(request.getNom());
            user.setPrenom(request.getPrenom());
            user.setEmail(email);
            user.setPassword(encodedPassword);
            userRepository.save(user);
            userType = "Utilisateur";
        }

        try {
            String token = UUID.randomUUID().toString();
            verificationTokenRepository.findByEmail(email).ifPresent(verificationTokenRepository::delete);
            verificationTokenRepository.save(new VerificationToken(token, email, LocalDateTime.now().plusHours(24)));
            emailService.sendVerificationEmail(email, token, userType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(Map.of(
                "message", "Inscription réussie ! Vérifiez votre email pour activer votre compte.",
                "success", true,
                "email", email));
    }
}