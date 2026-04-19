package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.Role;
import com.example.backend1.model.User;
import com.example.backend1.model.VerificationToken;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import com.example.backend1.repository.VerificationTokenRepository;
import com.example.backend1.util.JwtUtil;
import org.hibernate.sql.ast.tree.from.CorrelatedTableGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganisateurRepository organisateurRepository;
    private final JwtUtil jwtUtil;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganisateurRepository organisateurRepository, JwtUtil jwtUtil,VerificationTokenRepository verificationTokenRepository,
                       EmailService emailService)
    {
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.organisateurRepository = organisateurRepository;
        this.jwtUtil = jwtUtil;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailService = emailService;

    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional <User> userOpt = userRepository.findUserByEmail(email);
        if (userOpt.isPresent()){
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    List.of(new SimpleGrantedAuthority(user.getRole().name()))
            );
        }

        Optional<Organisateur> orgOpt = organisateurRepository.findByEmail(email);
        if (orgOpt.isPresent()){
            Organisateur org = orgOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    org.getEmail(),
                    org.getPassword(),
                    List.of(new SimpleGrantedAuthority(org.getRole().name()))
            );
        }

        throw new UsernameNotFoundException("User not found "+ email);

    }


    public String login(String email, String password){
        Optional<User> optionalUser=userRepository.findUserByEmail(email);


        if (optionalUser.isPresent()){
            User user = optionalUser.get();
            if (passwordEncoder.matches(password,user.getPassword())){
                if (!user.isActive()) {
                    throw new RuntimeException("ACCOUNT_DEACTIVATED");
                }
                if (!user.isVerified()) {
                    throw new RuntimeException("ACCOUNT_NOT_VERIFIED");
                }
                return jwtUtil.generateToken(user.getEmail(), user.getRole(),  user.getNom(), user.getPrenom(),user.isVerified(),false);
            }
            return null;
        }
        Optional<Organisateur> optionalOrg = organisateurRepository.findByEmail(email);
        if (optionalOrg.isPresent()){
            Organisateur org=optionalOrg.get();
            if (passwordEncoder.matches(password, org.getPassword())){
                if (!org.isActive()) {
                    throw new RuntimeException("ACCOUNT_DEACTIVATED");
                }
                if (!org.isVerified()) {
                    throw new RuntimeException("ACCOUNT_NOT_VERIFIED");
                }
                return jwtUtil.generateToken(org.getEmail(),org.getRole(), org.getNom(), org.getPrenom(),org.isVerified(),org.isAdminVerified());
            }
            return null;
        }
        return null;

    }


    public ResponseEntity<?> signUp(SignUpRequest request){
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String email = request.getEmail();
        String userType;

        if (request.getRole().equals("ROLE_ORGANISATEUR")){
            Organisateur newOrg= new Organisateur();
            newOrg.setNom(request.getNom());
            newOrg.setPrenom(request.getPrenom());
            newOrg.setEmail(request.getEmail());
            newOrg.setPassword(encodedPassword);
            newOrg.setRole(Role.ROLE_ORGANISATEUR);
            newOrg.setVerified(false);
            newOrg.setAdminVerified(false);
            newOrg.setNomOrganisation(request.getNomOrganisation());
            newOrg.setVerified(false);
            organisateurRepository.save(newOrg);
            userType = "Organisateur";

        }else {
            User newUser = new User();
            newUser.setNom(request.getNom());
            newUser.setPrenom(request.getPrenom());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(encodedPassword);
            newUser.setRole(Role.ROLE_USER);
            newUser.setVerified(false);
            userRepository.save(newUser);
            userType = "Utilisateur";
        }
        try {
            String token = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);

            Optional<VerificationToken> existingToken = verificationTokenRepository.findByEmail(email);
            existingToken.ifPresent(verificationTokenRepository::delete);

            VerificationToken verificationToken = new VerificationToken(token, email, expiryDate);
            verificationTokenRepository.save(verificationToken);

            emailService.sendVerificationEmail(email, token, userType);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Inscription réussie! Veuillez vérifier votre email pour activer votre compte.");
            response.put("success", true);
            response.put("email", email);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Inscription réussie! Un email de vérification a été envoyé.");
            response.put("success", true);
            response.put("email", email);

            return ResponseEntity.ok(response);
        }
    }}