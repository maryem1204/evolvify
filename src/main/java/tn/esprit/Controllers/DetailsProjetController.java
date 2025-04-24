package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.Entities.Projet;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;

import static tn.esprit.Controllers.ProjectListWithCardsController.stripHtmlTags;

public class DetailsProjetController {

    @FXML private Label lblNomProjet;
    @FXML private Label lblDescription;
    @FXML private Label lblStatus;
    @FXML private Label lblStartDate;
    @FXML private Label lblEndDate;
    @FXML private Label lblAbbreviation;
    @FXML private Label lblIdEmploye;
    @FXML private Label lblIdEmployeTitle;
    @FXML private Label lblUploadedFiles;

    private Projet projet;

    public void setProjet(Projet projet) {
        this.projet = projet;
        afficherDetails();
    }

    private void afficherDetails() {
        if (projet != null) {
            lblNomProjet.setText(stripHtmlTags(projet.getName()));
            lblDescription.setText(stripHtmlTags(projet.getDescription()));
            lblStatus.setText(projet.getStatus().name());
            lblStartDate.setText(projet.getStarter_at().toString());
            lblEndDate.setText(projet.getEnd_date().toString());
            lblAbbreviation.setText(projet.getAbbreviation());

            UtilisateurService utilisateurService = new UtilisateurService();

            if (projet.getEmployes() != null && !projet.getEmployes().isEmpty()) {
                lblIdEmployeTitle.setText("Employés Assignés :");

                StringBuilder employesNames = new StringBuilder();

                for (int i = 0; i < projet.getEmployes().size(); i++) {
                    Integer id = projet.getEmployes().get(i);
                    try {
                        Utilisateur user = utilisateurService.getUserById(id);
                        if (user != null) {
                            employesNames.append(user.getFirstname()).append(" ").append(user.getLastname());
                        } else {
                            employesNames.append("Utilisateur #").append(id);
                        }

                        // Add comma only between names, not after the last one
                        if (i < projet.getEmployes().size() - 1) {
                            employesNames.append(", ");
                        }
                    } catch (SQLException e) {
                        employesNames.append("Inconnu");
                        if (i < projet.getEmployes().size() - 1) {
                            employesNames.append(", ");
                        }
                        e.printStackTrace();
                    }
                }

                // Set the complete text without truncation
                lblIdEmploye.setText(employesNames.toString());

                // Make sure the label can wrap text if needed
                lblIdEmploye.setWrapText(true);
            } else {
                lblIdEmployeTitle.setText("Employés Assignés :");
                lblIdEmploye.setText("Aucun employé assigné");
            }
        }
    }
    @FXML
    private void fermerFenetre() {
        Stage stage = (Stage) lblNomProjet.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void openListTacheChefP() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListTacheChefP.fxml"));
            Parent root = loader.load();
            // Récupérer le contrôleur et passer l'ID du projet
            KanbanController kanbanController = loader.getController();
            kanbanController.setProjectId(projet.getId_projet());

            // Charger les tâches pour ce projet spécifique
            kanbanController.loadTachesForProject(projet.getId_projet());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Liste des Tâches du Projet");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture de la fenêtre ListTacheChefP.fxml");
            e.printStackTrace();
        }
    }
}