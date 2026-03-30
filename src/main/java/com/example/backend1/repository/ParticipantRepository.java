package com.example.backend1.repository;

import com.example.backend1.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.nio.channels.FileChannel;
import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByEmail(String email);

    Optional<Participant> findByConfirmationToken(String token);
    int countByEventIdAndVerifiedTrue(Long eventId);

    List<Participant> findByEventId(Long id);

    long countByVerified(boolean b);
}