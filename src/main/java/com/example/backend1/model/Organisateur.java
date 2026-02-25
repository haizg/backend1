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
    @Column(nullable = false)
    private String role = "ORGANISATEUR";

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
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
