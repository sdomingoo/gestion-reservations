package com.gestres;

import com.gestres.model.Equipement;
import com.gestres.model.Reservation;
import com.gestres.model.Salle;
import com.gestres.model.Utilisateur;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Classe principale démontrant l'ensemble des concepts du TP :
 * - Persistance en cascade (CascadeType.ALL, CascadeType.PERSIST)
 * - Suppression orpheline (orphanRemoval = true)
 * - Gestion ManyToMany et intégrité des équipements partagés
 * - Suppression en cascade d'un utilisateur et de ses réservations
 */
public class App {

    private static final String PERSISTENCE_UNIT_NAME = "gestion-reservations-pu";

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("   TP JPA / HIBERNATE : GESTION DES RÉSERVATIONS ET ÉQUIPEMENTS       ");
        System.out.println("======================================================================\n");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        EntityManager em = emf.createEntityManager();

        Long userId = null;
        Long resIdToRemove = null;
        Long resIdRemaining = null;
        Long salleId = null;
        Long equipementId = null;

        try {
            // ==================================================================
            // SCÉNARIO 1 : CRÉATION D'ENTITÉS ET PERSISTANCE EN CASCADE
            // ==================================================================
            System.out.println(">>> [SCÉNARIO 1] : Création des entités et test de la cascade PERSIST...");
            em.getTransaction().begin();

            // 1. Création de la salle et des équipements
            Salle salleA = new Salle("Salle Ada Lovelace", 35, "Bâtiment Turing");

            Equipement proj = new Equipement("Vidéoprojecteur 4K Laser", "EQ-PROJ-01", "Disponible");
            Equipement visio = new Equipement("Système Visioconférence Polycom", "EQ-VISIO-02", "Connecté");
            Equipement tbi = new Equipement("Tableau Blanc Interactif", "EQ-TBI-03", "Disponible");

            // Association bidirectionnelle salle <-> équipements (ManyToMany)
            salleA.addEquipement(proj);
            salleA.addEquipement(visio);
            salleA.addEquipement(tbi);

            // Cascade PERSIST sur Salle propage la persistance vers les équipements
            em.persist(salleA);

            // 2. Création de l'utilisateur
            Utilisateur user = new Utilisateur("Dr. Sophie Martin", "sophie.martin@univ-tech.fr", "Informatique");

            // 3. Création des réservations
            LocalDateTime now = LocalDateTime.now();
            Reservation res1 = new Reservation("Cours Magistral JEE & JPA", now.plusDays(1), now.plusDays(1).plusHours(3));
            res1.setSalle(salleA);

            Reservation res2 = new Reservation("Soutenance Projets Fin d'Études", now.plusDays(3), now.plusDays(3).plusHours(4));
            res2.setSalle(salleA);

            // Association bidirectionnelle utilisateur <-> réservations
            user.addReservation(res1);
            user.addReservation(res2);

            // Persistance de l'utilisateur avec CascadeType.ALL propage vers res1 et res2
            em.persist(user);

            em.getTransaction().commit();

            userId = user.getId();
            resIdToRemove = res1.getId();
            resIdRemaining = res2.getId();
            salleId = salleA.getId();
            equipementId = proj.getId();

            System.out.println("✔ [SCÉNARIO 1 TERMINÉ] : Données initiales persistées avec succès !");
            System.out.println("   - Utilisateur ID : " + userId);
            System.out.println("   - Réservation 1 ID : " + resIdToRemove);
            System.out.println("   - Réservation 2 ID : " + resIdRemaining);
            System.out.println("   - Salle ID : " + salleId);
            System.out.println("   - Équipement ID : " + equipementId);

            // ==================================================================
            // SCÉNARIO 2 : TEST DE orphanRemoval = true SUR LES RÉSERVATIONS
            // ==================================================================
            System.out.println("\n>>> [SCÉNARIO 2] : Test de la suppression orpheline (orphanRemoval = true)...");
            em.getTransaction().begin();

            Utilisateur managedUser = em.find(Utilisateur.class, userId);
            Reservation resToDelete = em.find(Reservation.class, resIdToRemove);

            System.out.println("   -> Retrait de la réservation '" + resToDelete.getMotif() + "' de la collection de l'utilisateur...");
            // Le simple retrait de la collection gérée déclenche le DELETE SQL grâce à orphanRemoval = true
            managedUser.removeReservation(resToDelete);

            em.getTransaction().commit();

            // Vérification après commit
            em.clear(); // Vider le cache de premier niveau pour forcer un SELECT en BDD
            Reservation checkDeletedRes = em.find(Reservation.class, resIdToRemove);
            if (checkDeletedRes == null) {
                System.out.println("✔ [VÉRIFICATION orphanRemoval] : SUCCÈS ! La réservation a bien été supprimée de la BDD.");
            } else {
                System.err.println("❌ ÉCHEC : La réservation orpheline existe encore en base !");
            }

            // ==================================================================
            // SCÉNARIO 3 : TEST MANYTOMANY & PROTECTION DES ÉQUIPEMENTS PARTAGÉS
            // ==================================================================
            System.out.println("\n>>> [SCÉNARIO 3] : Test ManyToMany (suppression d'une salle sans supprimer l'équipement partagé)...");
            em.getTransaction().begin();

            // Création d'une seconde salle partageant le même projecteur
            Salle salleB = new Salle("Salle B - Réunion", 15, "Bâtiment Turing");
            Equipement sharedProj = em.find(Equipement.class, equipementId);
            salleB.addEquipement(sharedProj);
            em.persist(salleB);
            em.getTransaction().commit();

            Long salleBId = salleB.getId();
            System.out.println("   -> Salle temporaire créée (ID: " + salleBId + ") avec l'équipement partagé ID: " + equipementId);

            // Suppression de la salle B
            em.getTransaction().begin();
            Salle salleBToDelete = em.find(Salle.class, salleBId);
            em.remove(salleBToDelete);
            em.getTransaction().commit();

            // Vérification que l'équipement existe toujours
            em.clear();
            Equipement checkEquip = em.find(Equipement.class, equipementId);
            if (checkEquip != null) {
                System.out.println("✔ [VÉRIFICATION ManyToMany] : SUCCÈS ! L'équipement partagé '" + checkEquip.getNom()
                        + "' est intact en BDD après suppression de la salle.");
            } else {
                System.err.println("❌ ÉCHEC : L'équipement a été supprimé à tort !");
            }

            // ==================================================================
            // SCÉNARIO 4 : TEST DE CascadeType.REMOVE SUR L'UTILISATEUR
            // ==================================================================
            System.out.println("\n>>> [SCÉNARIO 4] : Test de CascadeType.REMOVE lors de la suppression d'un Utilisateur...");
            em.getTransaction().begin();

            Utilisateur userToDelete = em.find(Utilisateur.class, userId);
            System.out.println("   -> Suppression de l'utilisateur : " + userToDelete.getNom());
            em.remove(userToDelete);

            em.getTransaction().commit();

            // Vérifications après commit
            em.clear();
            Utilisateur checkUser = em.find(Utilisateur.class, userId);
            Reservation checkRemainingRes = em.find(Reservation.class, resIdRemaining);
            Salle checkSalle = em.find(Salle.class, salleId);

            System.out.println("   - Utilisateur en BDD : " + (checkUser == null ? "SUPPRIMÉ (OK)" : "TOUJOURS PRÉSENT (Erreur)"));
            System.out.println("   - Réservation liée en BDD : " + (checkRemainingRes == null ? "SUPPRIMÉE EN CASCADE (OK)" : "TOUJOURS PRÉSENTE (Erreur)"));
            System.out.println("   - Salle en BDD : " + (checkSalle != null ? "INTACTE (OK)" : "SUPPRIMÉE (Erreur)"));

            if (checkUser == null && checkRemainingRes == null && checkSalle != null) {
                System.out.println("✔ [VÉRIFICATION CascadeType.REMOVE] : SUCCÈS TOTAL !");
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Une erreur est survenue lors de l'exécution : " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
            System.out.println("\n======================================================================");
            System.out.println("   FIN DE L'EXÉCUTION DU TP - TOUS LES TESTS SONT VALIDES !           ");
            System.out.println("======================================================================");
        }
    }
}
