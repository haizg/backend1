package com.example.backend1.model;

import jakarta.persistence.*;

@Entity
@Table(name="organisateurs")
public class Organisateur {
    @Id
    private Long cin;
    private String nom;
    private String prenom;
    private String adresse;
    @Column(unique = true, nullable = false)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
    public Organisateur() {}
    public Organisateur(String nom, String prenom,String adresse, String email, Role role, Long cin) {
        this.cin = cin;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.adresse = adresse;
        this.role = role;
    }

    public Long getCin() {
        return cin;
    }

    public void setCin(Long cin) {
        this.cin = cin;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public Role  getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
