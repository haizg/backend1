package com.example.backend1.model;

import jakarta.persistence.*;

@Entity
@Table(name="organisateurs")
public class Organisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String prenom;
    private String nomOrganisation;


    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_ORGANISATEUR;


    private boolean isVerified = false;

    public Organisateur() {}
    public Organisateur(String nom, String prenom,String adresse,String nomOrganisation, String email, Role role, Long cin) {
        this.nom = nom;
        this.prenom = prenom;
        this.nomOrganisation = nomOrganisation;
        this.email = email;
        this.role = role;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }


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

    public String getEmail() {
        return email;
    }
    public String getNomOrganisation() {
        return nomOrganisation;
    }
    public void setNomOrganisation(String nomOrganisation) {
        this.nomOrganisation = nomOrganisation;
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
