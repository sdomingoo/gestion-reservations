package com.gestres.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité représentant une Réservation effectuée par un Utilisateur pour une Salle.
 * Démontre des relations ManyToOne vers Utilisateur et vers Salle.
 */
@Entity
@Table(name = "reservations")
public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "motif", nullable = false, length = 150)
    private String motif;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    /**
     * Relation ManyToOne : Chaque réservation appartient obligatoirement à un utilisateur.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    /**
     * Relation ManyToOne : Chaque réservation concerne une salle précise.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    public Reservation() {
    }

    public Reservation(String motif, LocalDateTime dateDebut, LocalDateTime dateFin) {
        this.motif = motif;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Reservation(String motif, LocalDateTime dateDebut, LocalDateTime dateFin, Utilisateur utilisateur, Salle salle) {
        this.motif = motif;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.utilisateur = utilisateur;
        this.salle = salle;
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    // --- equals, hashCode & toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", motif='" + motif + '\'' +
                ", debut=" + dateDebut +
                ", fin=" + dateFin +
                ", utilisateur=" + (utilisateur != null ? utilisateur.getNom() : "null") +
                ", salle=" + (salle != null ? salle.getNom() : "null") +
                '}';
    }
}
