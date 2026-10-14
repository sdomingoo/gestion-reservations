# TP JPA / Hibernate : Gestion des Réservations et des Équipements

Ce projet implémente une solution complète et robuste pour le TP d'ingénierie logicielle JEE consacré à **JPA (Java Persistence API)** et **Hibernate**, avec une base de données relationnelle en mémoire **H2**.

---

## 1. Objectifs d'Apprentissage Développés

1. **Relations d'entités JPA complexes** :
   - **`@OneToMany`** et **`@ManyToOne`** bidirectionnelle : Association entre `Utilisateur` et `Reservation`.
   - **`@ManyToOne`** et **`@OneToMany`** : Association entre `Reservation` et `Salle`.
   - **`@ManyToMany`** : Association entre `Salle` et `Equipement` avec table de jointure personnalisée (`salle_equipement`).
2. **Gestion fine du cycle de vie et des cascades** :
   - Différenciation rigoureuse entre `CascadeType.ALL` et `orphanRemoval = true`.
   - Préservation des entités partagées en évitant `CascadeType.REMOVE` sur les relations `ManyToMany`.
3. **Méthodes utilitaires de synchronisation défensive** :
   - Maintien automatique de la cohérence des deux côtés de chaque relation bidirectionnelle (`addReservation`, `removeReservation`, `addEquipement`, `removeEquipement`).
4. **Validation automatisée et manuelle** :
   - Classe de démonstration (`App.java`) scénarisée avec logs SQL détaillés.
   - Suite de tests unitaires JUnit 5 (`ReservationJpaTest.java`) validant chaque comportement critique.

---

## 2. Diagramme de Modélisation des Entités

```mermaid
classDiagram
    direction LR

    class Utilisateur {
        +Long id
        +String nom
        +String email
        +String departement
        +Set~Reservation~ reservations
        +addReservation(Reservation r)
        +removeReservation(Reservation r)
    }

    class Reservation {
        +Long id
        +String motif
        +LocalDateTime dateDebut
        +LocalDateTime dateFin
        +Utilisateur utilisateur
        +Salle salle
    }

    class Salle {
        +Long id
        +String nom
        +int capacite
        +String batiment
        +Set~Reservation~ reservations
        +Set~Equipement~ equipements
        +addEquipement(Equipement e)
        +removeEquipement(Equipement e)
    }

    class Equipement {
        +Long id
        +String nom
        +String codeInventaire
        +String etat
        +Set~Salle~ salles
    }

    Utilisateur "1" --> "*" Reservation : 1. @OneToMany (cascade=ALL, orphanRemoval=true)
    Reservation "*" --> "1" Utilisateur : @ManyToOne (FK: utilisateur_id)
    Salle "1" --> "*" Reservation : @OneToMany (mappedBy=salle)
    Reservation "*" --> "1" Salle : @ManyToOne (FK: salle_id)
    Salle "*" <--> "*" Equipement : @ManyToMany (Table de jointure: salle_equipement)
```

---

## 3. Analyse Théorique & Pratique des Concepts JPA

### A. Différence entre `CascadeType.REMOVE` et `orphanRemoval = true`
- **`CascadeType.REMOVE`** :
  Si l'entité parente (`Utilisateur`) est supprimée via `em.remove(user)`, Hibernate supprime récursivement toutes ses réservations associées. En revanche, si on retire simplement une réservation de la collection `user.getReservations().remove(res)`, `CascadeType.REMOVE` **ne fait rien** (la réservation reste en base de données sans parent ou avec une clé étrangère orpheline).
- **`orphanRemoval = true`** :
  Permet de combler ce cas : dès qu'une entité enfant est **détachée de la collection** de son parent propriétaire dans le contexte de persistance, Hibernate détecte qu'elle est devenue "orpheline" et émet immédiatement un `DELETE FROM reservations WHERE id = ?` lors du flush.

### B. Pourquoi exclure `CascadeType.REMOVE` du `@ManyToMany` entre Salle et Equipement ?
Un équipement (ex: *Vidéoprojecteur 4K Laser*) est une ressource physique partagée pouvant être affectée à plusieurs salles simultanément ou successivement.
Si nous configurions `CascadeType.REMOVE` (ou `CascadeType.ALL`) sur la relation `@ManyToMany` de `Salle` :
> **Supprimer une salle détruirait physiquement l'équipement de la base**, brisant l'intégrité référentielle des autres salles qui l'utilisent également !

Pour cette raison, la relation `ManyToMany` est configurée avec uniquement :
```java
@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
```
Lorsqu'une salle est supprimée, Hibernate supprime uniquement les lignes correspondantes dans la table de jointure `salle_equipement`, tout en préservant l'enregistrement dans la table `equipements`.

### C. Le Piège d'`equals()` et `hashCode()` dans les Collections de JPA (`Set`)
Lorsqu'on utilise un `HashSet<Reservation>` :
- Si `hashCode()` est calculé à partir de l'identifiant (`id`), au moment de l'instanciation de l'objet, `id == null`. L'objet est placé dans le seau `hash(null)`.
- Après `em.persist()`, Hibernate assigne un identifiant numérique auto-incrémenté. Le `hashCode` de l'objet change alors en cours de session !
- Lorsque l'application tente d'exécuter `set.remove(reservation)`, le `HashSet` recalcule le hash avec la nouvelle valeur et cherche dans le mauvais seau, échouant silencieusement.
- **Bonne pratique appliquée** : `hashCode()` renvoie une valeur stable liée à la classe (`getClass().hashCode()`) ou à une clé métier immuable (`codeInventaire` / `email`), garantissant la cohérence des collections.

---

## 4. Structure du Projet

```text
gestion-reservations/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/gestres/
    │   │   ├── App.java                   # Démonstration des 4 scénarios
    │   │   └── model/
    │   │       ├── Utilisateur.java       # Entité 1-N (cascade ALL, orphanRemoval)
    │   │       ├── Reservation.java       # Entité centrale (N-1 vers Utilisateur & Salle)
    │   │       ├── Salle.java             # Entité N-M vers Equipement
    │   │       └── Equipement.java        # Entité partagée
    │   └── resources/
    │       └── META-INF/
    │           └── persistence.xml        # Configuration de l'Unité de Persistance H2
    └── test/
        └── java/com/gestres/
            └── ReservationJpaTest.java    # Suite de tests automatisés JUnit 5
```

---

## 5. Exécution et Validation

### 1. Lancement de la démonstration console
Exécutez dans un terminal à la racine du projet :
```bash
mvn compile exec:java
```

### 2. Exécution des tests unitaires automatisés
```bash
mvn test
```

Résultat attendu :
```text
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 6. Analyse des Requêtes SQL Générées

Grâce aux propriétés `show_sql=true` et `format_sql=true` dans [persistence.xml](file:///C:/Users/pc/.gemini/antigravity-ide/scratch/gestion-reservations/src/main/resources/META-INF/persistence.xml), les opérations suivantes sont observables en direct dans la console :

1. **Création en cascade (`CascadeType.PERSIST`)** :
   ```sql
   insert into salles (id, batiment, capacite, nom) values (default, ?, ?, ?)
   insert into equipements (id, code_inventaire, etat, nom) values (default, ?, ?, ?)
   insert into salle_equipement (salle_id, equipement_id) values (?, ?)
   insert into utilisateurs (id, departement, email, nom) values (default, ?, ?, ?)
   insert into reservations (id, date_debut, date_fin, motif, salle_id, utilisateur_id) values (default, ?, ?, ?, ?, ?)
   ```
2. **Suppression orpheline (`orphanRemoval=true`)** :
   ```sql
   delete from reservations where id=?
   ```
3. **Suppression d'une salle avec ManyToMany** :
   ```sql
   delete from salle_equipement where salle_id=?
   delete from salles where id=?
   -- REMARQUE : Aucun "delete from equipements" n'est émis, l'équipement reste disponible !
   ```
4. **Suppression d'un utilisateur en cascade (`CascadeType.REMOVE`)** :
   ```sql
   delete from reservations where id=?
   delete from utilisateurs where id=?
   ```
