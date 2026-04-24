package com.example.backend1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "user_id"})
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String userPrenom;

    @Column(nullable = false)
    private String userNom;

    public Review() {}

    public Long getId() { return id; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getUserPrenom() { return userPrenom; }
    public void setUserPrenom(String userPrenom) { this.userPrenom = userPrenom; }
    public String getUserNom() { return userNom; }
    public void setUserNom(String userNom) { this.userNom = userNom; }
}