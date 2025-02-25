package tn.esprit.Tests;

import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Services.AbsenceService;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class MainTestAbsence {
    public static void main(String[] args) {
        AbsenceService absenceService = new AbsenceService();

        try {
            // 1️⃣ Ajouter une nouvelle absence
            Absence absence1 = new Absence(0, StatutAbsence.EN_CONGE, new Date(), 1);
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
