package tn.esprit.Tests;

import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Utils.MyDataBase;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class MainTest {
    public static void main(String[] args) {
        // Connexion à la base de données
        MyDataBase db = MyDataBase.getInstance();
        UtilisateurService userService = new UtilisateurService();

        try {
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
    }
}
