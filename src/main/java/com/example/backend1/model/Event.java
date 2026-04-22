package com.example.backend1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String time;

    @Column(nullable = false)
    private String location;

    private String imageUrl;

    @Column(nullable = false)
    private String category;

    private Integer maxParticipants;

    @Column(length = 3000)
    private String program;

    @Column(nullable = false)
    private boolean approved = false;

    @Column(nullable = false)
    private String organisateurEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisateur_id", nullable = false)
    @JsonIgnore  // prevent infinite recursion in JSON serialization
    private Organisateur organisateur;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Participant> participants = new ArrayList<>();

    public Event() {}

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public String getOrganisateurEmail() { return organisateurEmail; }
    public void setOrganisateurEmail(String email) { this.organisateurEmail = email; }
    public Organisateur getOrganisateur() { return organisateur; }
    public void setOrganisateur(Organisateur organisateur) {
        this.organisateur = organisateur;
        if (organisateur != null) this.organisateurEmail = organisateur.getEmail();
    }
    public List<Participant> getParticipants() { return participants; }
}