package com.example.backend1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from Spring Boot hibaa!";
    }

    private final UserService userService;
    public HelloController(UserService userService){
        this.userService=userService;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        boolean success = userService.login(request.getUsername(),request.getPassword());
        System.out.println("LOGIN HIT: " + request.getUsername());
        if (success){
            return ResponseEntity.ok("Login successfull");
        }
        return ResponseEntity.status(401).body("Invalid username or password");
    }
}
