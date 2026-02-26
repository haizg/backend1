package com.example.backend1.controller;

import com.example.backend1.model.Organisateur;
import com.example.backend1.model.User;
import com.example.backend1.repository.OrganisateurRepository;
import com.example.backend1.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
        boolean success = userService.login(request.getEmail(),request.getPassword());
        if (success){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(401).build();
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request){
        return userService.signUp(request);
    }



}
