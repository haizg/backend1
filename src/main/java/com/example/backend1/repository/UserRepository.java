package com.example.backend1.repository;

import com.example.backend1.model.Role;
import com.example.backend1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleNot(Role role);
}