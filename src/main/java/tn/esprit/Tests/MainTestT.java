package tn.esprit.Tests;

import tn.esprit.Entities.Trajet;
import tn.esprit.Services.TrajetCRUD;
import tn.esprit.Utils.MyDataBase;
import java.sql.Time;
import java.util.List;
import java.sql.SQLException;

public class MainTestT {
    public static void main(String[] args) {
        // Créer une instance de TrajetCRUD
        TrajetCRUD trajetCRUD = new TrajetCRUD();

        try {
            // Ajouter un trajet
            System.out.println("Ajout d'un nouveau trajet :");
            Trajet t1 = new Trajet(1, "Tunis", "Sousse", 150.5, new Time(2, 30, 0), 2, 1, "En cours");
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

            // Modifier un trajet (modifier le trajet avec l'ID 1)
            if (!trajets.isEmpty()) {
                System.out.println("\nModification du premier trajet...");
                Trajet tModif = trajets.get(0);
                tModif.setDistance(160.0);  // Modification de la distance
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

            // Supprimer un trajet (supprimer le trajet avec l'ID 1)
            if (!trajets.isEmpty()) {
                System.out.println("\nSuppression du premier trajet...");
                int rowsDeleted = trajetCRUD.delete(trajets.get(0)); // Pass the whole Trajet object
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
