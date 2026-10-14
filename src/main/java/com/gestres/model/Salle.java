package com.gestres.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entité représentant une Salle pouvant accueillir des réservations et posséder des équipements.
 * Démontre :
 * - Une relation ManyToMany propriétaire vers Equipement avec table de jointure explicite.
 * - Une relation OneToMany vers Reservation.
 */
@Entity
@Table(name = "salles")
public class Salle implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 80)
    private String nom;

    @Column(name = "capacite", nullable = false)
    private int capacite;

    @Column(name = "batiment", length = 50)
    private String batiment;

    /**
     * Relation OneToMany : Une salle est associée à plusieurs réservations.
     */
    @OneToMany(mappedBy = "salle", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();

    /**
     * Relation ManyToMany : Une salle dispose de plusieurs équipements partagés.
     * Table de jointure : salle_equipement.
     * IMPORTANT : Cascade PERSIST et MERGE uniquement (on ne propage JAMAIS REMOVE sur un équipement partagé).
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "salle_equipement",
        joinColumns = @JoinColumn(name = "salle_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "equipement_id", referencedColumnName = "id")
    )
    private Set<Equipement> equipements = new HashSet<>();

    public Salle() {
    }

    public Salle(String nom, int capacite, String batiment) {
        this.nom = nom;
        this.capacite = capacite;
        this.batiment = batiment;
    }

    // --- Méthodes utilitaires de synchronisation de relations ---

    public void addEquipement(Equipement equipement) {
        if (equipement != null) {
            this.equipements.add(equipement);
            equipement.getSalles().add(this);
        }
    }

    public void removeEquipement(Equipement equipement) {
        if (equipement != null) {
            this.equipements.remove(equipement);
            equipement.getSalles().remove(this);
        }
    }

    public void addReservation(Reservation reservation) {
        if (reservation != null) {
            this.reservations.add(reservation);
            reservation.setSalle(this);
        }
    }

    public void removeReservation(Reservation reservation) {
        if (reservation != null) {
            this.reservations.remove(reservation);
            reservation.setSalle(null);
        }
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getBatiment() {
        return batiment;
    }

    public void setBatiment(String batiment) {
        this.batiment = batiment;
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Set<Equipement> getEquipements() {
        return equipements;
    }

    public void setEquipements(Set<Equipement> equipements) {
        this.equipements = equipements;
    }

    // --- equals, hashCode & toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Salle)) return false;
        Salle salle = (Salle) o;
        return Objects.equals(nom, salle.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom);
    }

    @Override
    public String toString() {
        return "Salle{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", capacite=" + capacite +
                ", batiment='" + batiment + '\'' +
                ", nbEquipements=" + (equipements != null ? equipements.size() : 0) +
                '}';
    }
}
