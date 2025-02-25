package tn.esprit.Tests;

import tn.esprit.Entities.Absence;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.AbsenceService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Utils.MyDataBase;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class MainTest {
    public static void main(String[] args) {
        // Connexion à la base de données
        MyDataBase db = MyDataBase.getInstance();
        UtilisateurService userService = new UtilisateurService();

        /*try {
            // ✅ Ajout d'utilisateurs avec les nouvelles règles
            Utilisateur user1 = new Utilisateur(
                    "John", "Doe", "john.doe@example.com", null, null, // Pas de password ni photo
                    null, // birthdayDate laissé vide, l'employé le remplira
                    Date.valueOf(LocalDate.now()), // joiningDate défini automatiquement
                    Role.EMPLOYEE, // ✅ Correction ici : utilisation de l'Enum Role
                    0, 0, null, null // Autres champs non renseignés
            );

            Utilisateur user2 = new Utilisateur(
                    "Jane", "Smith", "jane.smith@example.com", null, null, // Pas de password ni photo
                    null, // birthdayDate laissé vide, l'employé le remplira
                    Date.valueOf(LocalDate.now()), // joiningDate défini automatiquement
                    Role.RESPONSABLE_RH, // ✅ Correction ici
                    0, 0, null, null // Autres champs non renseignés
            );



            // Ajout des utilisateurs à la base de données
            userService.add(user1);
            userService.add(user2);

            System.out.println("**** Liste des utilisateurs après ajout ****");
            System.out.println(userService.showAll());

            // ✅ Mise à jour d'un utilisateur (RH peut modifier firstname, lastname, email, role)
            Utilisateur updatedUser = userService.getUserByEmail("jane.smith@example.com");
            if (updatedUser != null) {
                updatedUser.setFirstname("Janet");
                updatedUser.setLastname("Johnson");
                updatedUser.setEmail("janet.johnson@example.com");
                updatedUser.setRole(Role.CHEF_PROJET); // ✅ Correction ici : on utilise directement l'Enum

                userService.update(updatedUser); // On utilise updateUser (pas updateProfile)
            }

            System.out.println("**** Utilisateurs après mise à jour ****");
            System.out.println(userService.showAll());

            // ✅ Suppression d'un utilisateur
            Utilisateur userToDelete = userService.getUserByEmail("john.doe@example.com");
            if (userToDelete != null) {
                userService.delete(userToDelete);
            }

            System.out.println("**** Utilisateurs après suppression ****");
            System.out.println(userService.showAll());

        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace(); // ✅ Pour un meilleur débogage
        }
    }*/
        AbsenceService absenceService = new AbsenceService();

        try {
            // 1️⃣ Ajouter une nouvelle absence
            Absence absence1 = new Absence(0, StatutAbsence.EN_CONGE, new java.util.Date(), 1);
            int addResult = absenceService.add(absence1);
            System.out.println("Ajout de l'absence : " + (addResult > 0 ? "Succès" : "Échec"));

            // 2️⃣ Afficher toutes les absences
            List<Absence> absences = absenceService.showAll();
            System.out.println("\n📋 Liste des absences :");
            for (Absence absence : absences) {
                System.out.println(absence);
            }

            // 2️⃣ Mettre à jour une absence (par ID)
            try {
                Absence updatedAbsence = new Absence(2, StatutAbsence.EN_CONGE, new Date(), 1);
                int updateResult = absenceService.update(updatedAbsence);
                System.out.println(updateResult > 0 ? "✅ Mise à jour réussie" : "❌ Échec de la mise à jour");
            } catch (SQLException e) {
                System.out.println("❌ Erreur lors de la mise à jour : " + e.getMessage());
            }

            // 3️⃣ Afficher toutes les absences après modification
            System.out.println("\n📋 Liste des absences APRÈS modification :");
            absences = absenceService.showAll();
            for (Absence absence : absences) {
                System.out.println(absence);
            }

            // 4️⃣ Supprimer une absence (par ID)
            try {
                Absence toDelete = new Absence();
                toDelete.setIdAbsence(3);
                int deleteResult = absenceService.delete(toDelete);
                System.out.println(deleteResult > 0 ? "🗑️ Suppression réussie" : "❌ Échec de la suppression");
            } catch (SQLException e) {
                System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
            }

            // 5️⃣ Afficher toutes les absences après suppression
            System.out.println("\n📋 Liste des absences APRÈS suppression :");
            absences = absenceService.showAll();
            for (Absence absence : absences) {
                System.out.println(absence);
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur générale : " + e.getMessage());
        }
    }
}

