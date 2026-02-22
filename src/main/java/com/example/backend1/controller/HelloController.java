package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.User;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class HelloController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final OrganisateurRepository organisateurRepository;

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from Spring Boot hibaa!";
    }


    public HelloController(UserService userService, UserRepository userRepository, OrganisateurRepository organisateurRepository){
        this.userService=userService;
        this.userRepository = userRepository;
        this.organisateurRepository= organisateurRepository;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        boolean success = userService.login(request.getEmail(),request.getPassword());
        if (success){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/api/signUpUser")
    public User signUpUser(@RequestBody User user){
        System.out.println("Received signup request!");
        System.out.println("User data: " + user);
        User savedUser = userRepository.save(user);
        System.out.println("Saved user: " + savedUser);

        return savedUser;
    }

    @PostMapping("/api/signUpOrg")
    public Organisateur signUpOrg(@RequestBody Organisateur organisateur){
        System.out.println("Received signup organizer request!");
        System.out.println("organizer data: " + organisateur);
        Organisateur savedOrganisateur = organisateurRepository.save(organisateur);
        System.out.println("Saved organizer: " + savedOrganisateur);

        return savedOrganisateur;
    }


}
