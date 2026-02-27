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
        String result = userService.login(request.getEmail(),request.getPassword());
        if (result==null){
            return ResponseEntity.status(401).body("Invalid credentials");
        }
        if (result.equals("NOT_VERIFIED")){
            return ResponseEntity.status(403).body("Account not verified yet");
        }
        return ResponseEntity.ok(result);
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request){
        return userService.signUp(request);
    }



}
