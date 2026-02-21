package com.example.backend1.model;

import jakarta.persistence.*;

@Entity
@Table(name = "participants")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Integer numberOfPeople;

    @Column(nullable = false)
    private String eventName;

    private boolean verified = false;

    // Constructors
    public Participant() {}

    public Participant(String email, Integer numberOfPeople, String eventName) {
        this.email = email;
        this.numberOfPeople = numberOfPeople;
        this.eventName = eventName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getNumberOfPeople() { return numberOfPeople; }
    public void setNumberOfPeople(Integer numberOfPeople) { this.numberOfPeople = numberOfPeople; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}