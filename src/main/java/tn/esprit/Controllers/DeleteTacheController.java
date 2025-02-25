package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.Entities.Tache;
import tn.esprit.Services.TacheService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteTacheController {

    @FXML
    private Label confirmationMessage;
    @FXML
    private KanbanController kanbanController; // Utilisation d'une seule variable pour KanbanController
    // Label pour afficher le message de confirmation

    private Tache tache;
    private final TacheService tacheService = new TacheService(); // Service pour gérer la suppression
    private static final Logger LOGGER = Logger.getLogger(DeleteTacheController.class.getName());
    private tacheListController tacheListController; // Correction de la casse du nom du contrôleur parent

    /**
     * Setter pour passer le contrôleur parent.
     */
    public void setTacheListController(tacheListController tacheListController) {
        this.tacheListController = tacheListController;
    }

    public void setKanbanController(KanbanController kanbanController) {
        this.kanbanController = kanbanController;
    }

    /**
     * Initialise le contrôleur avec la tâche sélectionnée.
     */
    public void setTache(Tache tache) {
        if (tache == null) {
            LOGGER.log(Level.WARNING, "Tâche null passée à DeleteTacheController");
            showErrorAlert("Aucune tâche sélectionnée.");
            return;
        }
        this.tache = tache;
        confirmationMessage.setText("Êtes-vous sûr de vouloir supprimer la tâche : "
                + tache.getDescription() + " ? Cette action est irréversible.");
    }

    /**
     * Méthode appelée lorsque l'utilisateur confirme la suppression de la tâche.
     */
    @FXML
    private void handleConfirmDelete() {
        if (tache == null) {
            showErrorAlert("Aucune tâche sélectionnée.");
            return;
        }

        try {
            tacheService.delete(tache); // Suppression de la tâche via le service
            showInfoAlert("Tâche supprimée avec succès !");

            // Rafraîchir l'affichage des tâches dans le Kanban
            if (kanbanController != null) {
                kanbanController.refreshKanban(); // Mettre à jour l'interface Kanban
            }

            // Rafraîchir la liste des tâches dans la vue des tâches si nécessaire
            if (tacheListController != null) {
                tacheListController.refreshTacheList(); // Mettre à jour la table des tâches
            }

            closeWindow(); // Fermer la fenêtre de confirmation
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la tâche : " + tache.getId_tache(), e);
            showErrorAlert("Erreur lors de la suppression de la tâche. Détails : " + e.getMessage());
        }
    }

    /**
     * Méthode appelée lorsque l'utilisateur annule la suppression.
     */
    @FXML
    private void handleCancelDelete() {
        LOGGER.info("Suppression annulée.");
        closeWindow();
    }

    /**
     * Affiche une alerte d'information.
     */
    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'erreur.
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Ferme la fenêtre de confirmation.
     */
    private void closeWindow() {
        Stage stage = (Stage) confirmationMessage.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
