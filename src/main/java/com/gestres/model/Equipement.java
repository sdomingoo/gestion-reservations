package com.gestres.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entité représentant un équipement pouvant être assigné à plusieurs salles.
 * Côté inverse de la relation ManyToMany avec Salle.
 */
@Entity
@Table(name = "equipements")
public class Equipement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "code_inventaire", unique = true, length = 30)
    private String codeInventaire;

    @Column(name = "etat", length = 30)
    private String etat;

    /**
     * Côté esclave / inverse du ManyToMany.
     * mappedBy pointe vers le champ 'equipements' dans la classe Salle.
     * Pas de CascadeType.REMOVE ici pour éviter de supprimer accidentellement les salles lors du retrait d'un équipement.
     */
    @ManyToMany(mappedBy = "equipements", fetch = FetchType.LAZY)
    private Set<Salle> salles = new HashSet<>();

    public Equipement() {
    }

    public Equipement(String nom, String codeInventaire, String etat) {
        this.nom = nom;
        this.codeInventaire = codeInventaire;
        this.etat = etat;
    }

    // --- Méthodes utilitaires ---

    public void addSalle(Salle salle) {
        if (salle != null && !this.salles.contains(salle)) {
            this.salles.add(salle);
            salle.getEquipements().add(this);
        }
    }

    public void removeSalle(Salle salle) {
        if (salle != null && this.salles.contains(salle)) {
            this.salles.remove(salle);
            salle.getEquipements().remove(this);
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

    public String getCodeInventaire() {
        return codeInventaire;
    }

    public void setCodeInventaire(String codeInventaire) {
        this.codeInventaire = codeInventaire;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public Set<Salle> getSalles() {
        return salles;
    }

    public void setSalles(Set<Salle> salles) {
        this.salles = salles;
    }

    // --- equals, hashCode & toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipement that = (Equipement) o;
        return codeInventaire != null && Objects.equals(codeInventaire, that.codeInventaire);
    }

    @Override
    public int hashCode() {
        return codeInventaire != null ? Objects.hash(codeInventaire) : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Equipement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", codeInventaire='" + codeInventaire + '\'' +
                ", etat='" + etat + '\'' +
                ", nbSalles=" + (salles != null ? salles.size() : 0) +
                '}';
    }
}
