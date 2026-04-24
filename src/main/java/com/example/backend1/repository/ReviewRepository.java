package com.example.backend1.repository;

import com.example.backend1.model.Event;
import com.example.backend1.model.Review;
import com.example.backend1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEventId(Long eventId);
    Optional<Review> findByEventAndUser(Event event, User user);
    boolean existsByEventAndUser(Event event, User user);
    List<Review> findByEvent_Organisateur_Id(Long orgId);
}