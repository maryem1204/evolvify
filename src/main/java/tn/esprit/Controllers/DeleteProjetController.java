package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.Entities.Projet;
import tn.esprit.Services.ProjetService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DeleteProjetController {

    @FXML
    private Label confirmationMessage;  // Label pour afficher le message

    private Projet projet;
    private final ProjetService projetService = new ProjetService(); // Éviter de recréer un service à chaque appel
    private static final Logger LOGGER = Logger.getLogger(DeleteProjetController.class.getName());

    private ProjectListController projectListController; // Référence vers le contrôleur parent

    /**
     * Setter pour passer le contrôleur parent.
     */
    public void setProjectListController(ProjectListController projectListController) {
        this.projectListController = projectListController;
    }

    /**
     * Initialise le contrôleur avec le projet sélectionné.
     */
    public void setProjet(Projet projet) {
        if (projet == null) {
            LOGGER.log(Level.WARNING, "Projet null passé à DeleteProjetController");
            showErrorAlert("Aucun projet sélectionné.");
            return;
        }
        this.projet = projet;
        confirmationMessage.setText("Êtes-vous sûr de vouloir supprimer le projet : "
                + projet.getName() + " ? Cette action est irréversible.");
    }

    /**
     * Méthode appelée lorsque l'utilisateur confirme la suppression du projet.
     */
    @FXML
    private void handleConfirmDelete() {
        if (projet == null) {
            showErrorAlert("Aucun projet sélectionné.");
            return;
        }

        try {
            projetService.delete(projet);
            showInfoAlert("Projet supprimé avec succès !");

            // Rafraîchir la liste des projets immédiatement
            if (projectListController != null) {
                projectListController.refreshProjetList();
            }

            closeWindow();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression du projet : " + projet.getId_projet(), e);
            showErrorAlert("Erreur lors de la suppression du projet. Veuillez réessayer.");
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
