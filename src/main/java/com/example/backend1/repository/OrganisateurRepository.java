package com.example.backend1.repository;

import com.example.backend1.model.Event;
import com.example.backend1.model.Organisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganisateurRepository extends JpaRepository <Organisateur,Long> {
    Optional<Organisateur> findByEmail(String email);
}
