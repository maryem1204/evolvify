package tn.esprit.Tests;

import tn.esprit.Entities.Abonnement;
import tn.esprit.Entities.StatusAbonnement;
import tn.esprit.Services.AbonnementCRUD;
import tn.esprit.Utils.MyDataBase;

import java.sql.SQLException;
import java.util.List;

public class MainTestA {
    public static void main(String[] args) {
        // Connexion à la base de données (Assurez-vous que MyDataBase.getInstance() fonctionne bien)
        MyDataBase db1 = MyDataBase.getInstance();
        AbonnementCRUD abonnementCRUD = new AbonnementCRUD();

        // Ajouter un nouvel abonnement
        Abonnement ab1 = new Abonnement(0, "Mensuel", new java.sql.Date(System.currentTimeMillis()),
                new java.sql.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000),
                50.0, 1, StatusAbonnement.ACTIF);

        try {
            int abonnementId = abonnementCRUD.add(ab1);
            if (abonnementId > 0) {
                System.out.println("Abonnement ajouté avec succès avec l'ID : " + abonnementId);
            } else {
                System.out.println("L'ajout de l'abonnement a échoué.");
            }

            // Afficher tous les abonnements
            List<Abonnement> abonnements = abonnementCRUD.showAll();
            System.out.println("\nListe des abonnements :");
            for (Abonnement ab : abonnements) {
                System.out.println(ab);
            }

            // Modifier un abonnement existant (exemple avec l'ID du premier abonnement)
            if (!abonnements.isEmpty()) {
                Abonnement abToUpdate = abonnements.get(0);
                abToUpdate.setPrix(60.0);  // Changer le prix
                int rowsUpdated = abonnementCRUD.update(abToUpdate);
                if (rowsUpdated > 0) {
                    System.out.println("Abonnement mis à jour avec succès !");
                } else {
                    System.out.println("La mise à jour a échoué.");
                }
            }

            // Supprimer un abonnement (exemple avec l'ID du premier abonnement)
            if (!abonnements.isEmpty()) {
                Abonnement abToDelete = abonnements.get(0);
                int rowsDeleted = abonnementCRUD.delete(abToDelete);
                if (rowsDeleted > 0) {
                    System.out.println("Abonnement supprimé avec succès !");
                } else {
                    System.out.println("La suppression a échoué.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
        }
    }
}
