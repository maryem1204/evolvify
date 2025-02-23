package tn.esprit.Tests;

import tn.esprit.Entities.Trajet;
import tn.esprit.Entities.StatusTrajet; // Import de l'Enum StatusTrajet
import tn.esprit.Services.TrajetCRUD;

import java.sql.Time;
import java.sql.SQLException;
import java.util.List;

public class MainTestT {
    public static void main(String[] args) {
        // Créer une instance de TrajetCRUD
        TrajetCRUD trajetCRUD = new TrajetCRUD();

        try {
            // Ajouter un trajet
            System.out.println("Ajout d'un nouveau trajet :");
            Trajet t1 = new Trajet(1, "Tunis", "Sousse", 150.5, new Time(2, 30, 0), 2, 1, StatusTrajet.EN_COURS); // Utilisation de l'Enum
            int trajetId = trajetCRUD.add(t1);
            if (trajetId > 0) {
                System.out.println("✅ Trajet ajouté avec succès, ID: " + trajetId);
            } else {
                System.out.println("❌ L'ajout du trajet a échoué.");
            }

            // Afficher tous les trajets
            System.out.println("\nAffichage de tous les trajets :");
            List<Trajet> trajets = trajetCRUD.showAll();
            for (Trajet t : trajets) {
                System.out.println(t);
            }

            // Modifier un trajet (modifier le premier trajet récupéré)
            if (!trajets.isEmpty()) {
                System.out.println("\nModification du premier trajet...");
                Trajet tModif = trajets.get(0);
                tModif.setDistance(160.0);  // Modification de la distance
                tModif.setStatus(StatusTrajet.TERMINE); // Modification du statut

                int rowsUpdated = trajetCRUD.update(tModif);
                if (rowsUpdated > 0) {
                    System.out.println("✅ Trajet mis à jour avec succès !");
                } else {
                    System.out.println("❌ La mise à jour a échoué.");
                }
            }

            // Afficher les trajets après modification
            System.out.println("\nAffichage des trajets après modification :");
            trajets = trajetCRUD.showAll();
            for (Trajet t : trajets) {
                System.out.println(t);
            }

            // Supprimer un trajet (supprimer le premier trajet récupéré)
            if (!trajets.isEmpty()) {
                System.out.println("\nSuppression du premier trajet...");
                int rowsDeleted = trajetCRUD.delete(trajets.get(0)); // Suppression par objet Trajet
                if (rowsDeleted > 0) {
                    System.out.println("✅ Trajet supprimé avec succès !");
                } else {
                    System.out.println("❌ La suppression a échoué.");
                }
            }

            // Afficher les trajets après suppression
            System.out.println("\nAffichage des trajets après suppression :");
            trajets = trajetCRUD.showAll();
            for (Trajet t : trajets) {
                System.out.println(t);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur de base de données : " + e.getMessage());
        }
    }
}
