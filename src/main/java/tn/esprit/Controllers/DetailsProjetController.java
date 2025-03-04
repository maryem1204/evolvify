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

import java.io.IOException;
import java.util.Base64;

public class DetailsProjetController {

    @FXML
    private Label lblNomProjet;

    @FXML
    private Label lblDescription;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblStartDate;

    @FXML
    private Label lblEndDate;

    @FXML
    private Label lblAbbreviation;

    @FXML
    private Label lblIdEmploye;

    @FXML
    private Label lblUploadedFiles;

    private Projet projet;

    public void setProjet(Projet projet) {
        this.projet = projet;
        afficherDetails();
    }

    private void afficherDetails() {
        if (projet != null) {
            lblNomProjet.setText(projet.getName());
            lblDescription.setText(projet.getDescription());
            lblStatus.setText(projet.getStatus().name());
            lblStartDate.setText(projet.getStarter_at().toString());
            lblEndDate.setText(projet.getEnd_date().toString());
            lblAbbreviation.setText(projet.getAbbreviation());
            lblIdEmploye.setText(String.valueOf(projet.getId_employe()));

            // Convertir les fichiers en Base64 pour affichage
            //if (projet.getUploaded_files() != null && projet.getUploaded_files().length > 0) {
               // lblUploadedFiles.setText("Fichier attaché (encodé)");
            //} else {
              //  lblUploadedFiles.setText("Aucun fichier attaché");
            //}
        }
    }

    @FXML
    private void fermerFenetre() {
        Stage stage = (Stage) lblNomProjet.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void openListTacheChefP() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListTacheChefP.fxml"));
            Parent root = loader.load();
            // Get the controller and pass the project ID
            KanbanController kanbanController = loader.getController();
            kanbanController.setProjectId(projet.getId_projet());

            // Load tasks for this specific project
            kanbanController.loadTachesForProject(projet.getId_projet());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Liste des Tâches du Chef");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
