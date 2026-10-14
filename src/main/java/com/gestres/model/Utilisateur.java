package com.gestres.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entité représentant un Utilisateur effectuant des réservations.
 * Démontre une relation OneToMany bidirectionnelle avec cascade et orphanRemoval.
 */
@Entity
@Table(name = "utilisateurs")
public class Utilisateur implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "nom", nullable = false, length = 80)
    private String nom;

    @Column(name = "departement", length = 50)
    private String departement;

    /**
     * Relation OneToMany : un utilisateur peut détenir plusieurs réservations.
     * - cascade = CascadeType.ALL : propage persist, merge, remove, refresh, detach.
     * - orphanRemoval = true : supprime automatiquement de la BDD toute réservation retirée de la collection.
     */
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();

    public Utilisateur() {
    }

    public Utilisateur(String nom, String email, String departement) {
        this.nom = nom;
        this.email = email;
        this.departement = departement;
    }

    // --- Méthodes utilitaires de synchronisation bidirectionnelle ---

    /**
     * Associe une réservation à cet utilisateur et met à jour l'autre côté de la relation.
     */
    public void addReservation(Reservation reservation) {
        if (reservation != null) {
            this.reservations.add(reservation);
            reservation.setUtilisateur(this);
        }
    }

    /**
     * Dissocie une réservation de cet utilisateur et déclenche la suppression orpheline si activée.
     */
    public void removeReservation(Reservation reservation) {
        if (reservation != null) {
            this.reservations.remove(reservation);
        }
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }

    // --- equals, hashCode & toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilisateur)) return false;
        Utilisateur that = (Utilisateur) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", departement='" + departement + '\'' +
                ", nbReservations=" + (reservations != null ? reservations.size() : 0) +
                '}';
    }
}
