package com.example.backend1.repository;

import com.example.backend1.model.Event;
import com.example.backend1.model.Participant;
import com.example.backend1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByEmail(String email);
    List<Participant> findByUser(User user);
    Optional<Participant> findByConfirmationToken(String token);
    int countByEventAndVerifiedTrue(Event event);
    boolean existsByEventIdAndUserIdAndVerifiedTrue(Long eventId, Long userId);
    List<Participant> findByEvent(Event event);
    void deleteByEvent(Event event);
    void deleteByEmail(String email);
    long countByVerified(boolean verified);
    Optional<Participant> findByEmailAndEvent(String email, Event event);}