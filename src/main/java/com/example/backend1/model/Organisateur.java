package com.example.backend1.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organisateurs")
@PrimaryKeyJoinColumn(name = "id")
public class Organisateur extends User {

    @Column(nullable = false)
    private boolean adminVerified = false;

    @Column(nullable = false)
    private boolean deactivationRequested = false;

    private String nomOrganisation;

    @OneToMany(mappedBy = "organisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events = new ArrayList<>();

    public Organisateur() {
        setRole(Role.ROLE_ORGANISATEUR);
    }

    public boolean isAdminVerified() { return adminVerified; }
    public void setAdminVerified(boolean adminVerified) { this.adminVerified = adminVerified; }
    public boolean isDeactivationRequested() { return deactivationRequested; }
    public void setDeactivationRequested(boolean v) { this.deactivationRequested = v; }
    public String getNomOrganisation() { return nomOrganisation; }
    public void setNomOrganisation(String n) { this.nomOrganisation = n; }
    public List<Event> getEvents() { return events; }
}