package com.example.backend1.controller;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <User,String> {
    Optional<User> findUserByUsername(String username);
}

