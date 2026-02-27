package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.Role;
import com.example.backend1.model.User;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import com.example.backend1.util.JwtUtil;
import org.hibernate.sql.ast.tree.from.CorrelatedTableGroup;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganisateurRepository organisateurRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganisateurRepository organisateurRepository, JwtUtil jwtUtil)
    {
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.organisateurRepository = organisateurRepository;
        this.jwtUtil = jwtUtil;
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
                return jwtUtil.generateToken(user.getEmail(), user.getRole());
            }
            return null;
        }
        Optional<Organisateur> optionalOrg = organisateurRepository.findByEmail(email);
        if (optionalOrg.isPresent()){
            Organisateur org=optionalOrg.get();
            if (!org.isVerified()){
                return "NOT_VERIFIED";
            }
            if (passwordEncoder.matches(password, org.getPassword())){
                return jwtUtil.generateToken(org.getEmail(),org.getRole());
            }
            return null;
        }
        return null;

    }


    public ResponseEntity<?> signUp(SignUpRequest request){
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        if (request.getRole().equals("ROLE_ORGANISATEUR")){
            Organisateur newOrg= new Organisateur();
            newOrg.setNom(request.getNom());
            newOrg.setPrenom(request.getPrenom());
            newOrg.setEmail(request.getEmail());
            newOrg.setPassword(encodedPassword);
            newOrg.setRole(Role.ROLE_ORGANISATEUR);
            newOrg.setVerified(false);
            organisateurRepository.save(newOrg);
        }else {
            User newUser = new User();
            newUser.setNom(request.getNom());
            newUser.setPrenom(request.getPrenom());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(encodedPassword);
            newUser.setRole(Role.ROLE_USER);
            userRepository.save(newUser);
        }
        return ResponseEntity.ok("Signup successful");
    }

}
