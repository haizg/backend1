package com.example.backend1.repository;

import com.example.backend1.model.Event;
import com.example.backend1.model.Organisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByApprovedTrue();
    List<Event> findByApprovedFalse();
    List<Event> findByOrganisateur(Organisateur organisateur);
    List<Event> findByOrganisateurEmail(String email);
}