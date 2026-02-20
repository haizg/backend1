package com.example.backend1.controller;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public boolean login(String username, String password){
        Optional<User> optionalUser=userRepository.findUserByUsername(username);

        if (optionalUser.isPresent()){
            User user =optionalUser.get();
            return user.getPassword().equals(password);
        }
        return false;
    }

}
