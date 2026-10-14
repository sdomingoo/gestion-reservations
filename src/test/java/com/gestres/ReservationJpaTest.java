package com.gestres;

import com.gestres.model.Equipement;
import com.gestres.model.Reservation;
import com.gestres.model.Salle;
import com.gestres.model.Utilisateur;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationJpaTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeAll
    public static void setUpFactory() {
        emf = Persistence.createEntityManagerFactory("gestion-reservations-pu");
    }

    @AfterAll
    public static void tearDownFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @BeforeEach
    public void setUp() {
        em = emf.createEntityManager();
    }

    @AfterEach
    public void tearDown() {
        if (em != null && em.isOpen()) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Persistance en cascade de l'Utilisateur et de ses Réservations")
    public void testCascadePersistUtilisateurAndReservations() {
        em.getTransaction().begin();

        Salle salle = new Salle("Salle Blaise Pascal", 25, "Bâtiment B");
        Equipement eq = new Equipement("Écran Interactif 75p", "EQ-SCR-01", "Neuf");
        salle.addEquipement(eq);
        em.persist(salle);

        Utilisateur user = new Utilisateur("Marc Dupont", "marc.dupont@test.com", "R&D");
        Reservation res = new Reservation("Séminaire Technique", LocalDateTime.now(), LocalDateTime.now().plusHours(2));
        res.setSalle(salle);

        user.addReservation(res);

        // Persist de l'utilisateur qui cascade sur la réservation
        em.persist(user);
        em.getTransaction().commit();

        assertNotNull(user.getId(), "L'utilisateur doit avoir un ID généré");
        assertNotNull(res.getId(), "La réservation doit avoir été persistée en cascade");

        // Vérification en base
        em.clear();
        Reservation foundRes = em.find(Reservation.class, res.getId());
        assertNotNull(foundRes, "La réservation doit exister en BDD");
        assertEquals("Marc Dupont", foundRes.getUtilisateur().getNom());
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Validation de orphanRemoval = true sur la suppression d'une réservation")
    public void testOrphanRemoval() {
        em.getTransaction().begin();

        Salle salle = new Salle("Salle Claude Shannon", 40, "Bâtiment C");
        em.persist(salle);

        Utilisateur user = new Utilisateur("Claire Bernard", "claire.bernard@test.com", "Informatique");
        Reservation res1 = new Reservation("Atelier Algorithmique", LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        Reservation res2 = new Reservation("Revue de code", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

        res1.setSalle(salle);
        res2.setSalle(salle);

        user.addReservation(res1);
        user.addReservation(res2);

        em.persist(user);
        em.getTransaction().commit();

        Long res1Id = res1.getId();
        Long userId = user.getId();

        em.clear();

        // Retrait de res1 de la collection
        em.getTransaction().begin();
        Utilisateur managedUser = em.find(Utilisateur.class, userId);
        Reservation resToRemove = em.find(Reservation.class, res1Id);

        managedUser.removeReservation(resToRemove);
        em.getTransaction().commit();

        // Vérification orphan removal
        em.clear();
        Reservation deletedRes = em.find(Reservation.class, res1Id);
        assertNull(deletedRes, "La réservation orpheline doit être automatiquement supprimée par orphanRemoval");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: ManyToMany entre Salle et Equipement (Préservation des équipements partagés)")
    public void testManyToManyEquipementPreservedOnSalleDelete() {
        em.getTransaction().begin();

        Equipement micro = new Equipement("Microphone HF Shure", "EQ-MIC-99", "Opérationnel");
        Salle salleConf = new Salle("Auditorium Pasteur", 150, "Bâtiment Central");
        salleConf.addEquipement(micro);

        em.persist(salleConf);
        em.getTransaction().commit();

        Long equipId = micro.getId();
        Long salleId = salleConf.getId();

        // Suppression de la salle
        em.getTransaction().begin();
        Salle managedSalle = em.find(Salle.class, salleId);
        em.remove(managedSalle);
        em.getTransaction().commit();

        // L'équipement ne doit PAS être supprimé
        em.clear();
        Equipement persistedEquip = em.find(Equipement.class, equipId);
        assertNotNull(persistedEquip, "L'équipement partagé ne doit pas être supprimé quand la salle est supprimée");
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: CascadeType.REMOVE lors de la suppression de l'utilisateur")
    public void testCascadeRemoveUserDeletesReservations() {
        em.getTransaction().begin();

        Salle salle = new Salle("Salle Grace Hopper", 30, "Bâtiment A");
        em.persist(salle);

        Utilisateur user = new Utilisateur("Thomas Edison", "t.edison@test.com", "Physique");
        Reservation res = new Reservation("Session de Laboratoire", LocalDateTime.now(), LocalDateTime.now().plusHours(3));
        res.setSalle(salle);
        user.addReservation(res);

        em.persist(user);
        em.getTransaction().commit();

        Long userId = user.getId();
        Long resId = res.getId();

        // Suppression de l'utilisateur
        em.getTransaction().begin();
        Utilisateur managedUser = em.find(Utilisateur.class, userId);
        em.remove(managedUser);
        em.getTransaction().commit();

        // Vérification que la réservation a disparu en cascade
        em.clear();
        assertNull(em.find(Utilisateur.class, userId), "L'utilisateur doit être supprimé");
        assertNull(em.find(Reservation.class, resId), "Les réservations de l'utilisateur doivent être supprimées en cascade");
        assertNotNull(em.find(Salle.class, salle.getId()), "La salle ne doit pas être impactée par la suppression de l'utilisateur");
    }
}
