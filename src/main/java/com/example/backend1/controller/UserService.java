package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.Role;
import com.example.backend1.model.User;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganisateurRepository organisateurRepository)
    {
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.organisateurRepository = organisateurRepository;
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
            if (!org.isVerified()){
                throw new UsernameNotFoundException("Account not verified yet");
            }
            return new org.springframework.security.core.userdetails.User(
                    org.getEmail(),
                    org.getPassword(),
                    List.of(new SimpleGrantedAuthority(org.getRole().name()))
            );
        }

        throw new UsernameNotFoundException("User not found "+ email);

    }


    public boolean login(String email, String password){
        Optional<User> optionalUser=userRepository.findUserByEmail(email);


        if (optionalUser.isPresent()){
            return passwordEncoder.matches(password,optionalUser.get().getPassword());
        }
        return false;
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
