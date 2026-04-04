package com.example.backend1.repository;
import java.util.List;
import java.util.Optional;

import com.example.backend1.model.Role;
import com.example.backend1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <User,String> {
    Optional<User> findUserByEmail(String email);
    List<User> findByRoleNot(Role role);

}

