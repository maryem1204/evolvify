package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.temporal.ChronoUnit;

public class CarteConge {

    @FXML
    private AnchorPane rootPane;


    @FXML
    private Button btnNext, btnSubmit;



    @FXML
    private TextField numberOfDays, remainingTT;

    private int totalLeaveDays = 23; // Nombre total de jours de congé restants

    @FXML
    private Button btnAnnuler;

    // Gérer le bouton "Congé"
    @FXML
    private void handleConge(ActionEvent event) {
        System.out.println("Bouton Congé cliqué !");
        afficherFenetre("/fxml/ajouterConge.fxml");
    }
    // Gérer le bouton "Télétravail"
    @FXML
    private void handleTT(ActionEvent event) {
        System.out.println("Bouton Télétravail cliqué !");
        afficherFenetre("/fxml/detailTT.fxml");
    }


    // Gérer le bouton "Suivant"
   /* @FXML
    private void handleNext(ActionEvent event) throws IOException {
        System.out.println("Bouton Suivant cliqué !");
        Parent secondView = FXMLLoader.load(getClass().getResource("detailTT.fxml"));
        Scene newScene = new Scene(secondView);
        Stage currentStage = (Stage) rootPane.getScene().getWindow();
        currentStage.setScene(newScene);
    }*/

    // Méthode pour calculer le nombre de jours de TT
   /* @FXML
    private void calculateLeaveDays() {
        if (fromDate.getValue() != null && toDate.getValue() != null) {
            long daysBetween = ChronoUnit.DAYS.between(fromDate.getValue(), toDate.getValue()) + 1;
            if (daysBetween > 0) {
                numberOfDays.setText(String.valueOf(daysBetween));
                remainingTT.setText(String.valueOf(totalLeaveDays - daysBetween));
            } else {
                numberOfDays.setText("0");
            }
        }
    }*/

    // Méthode appelée lors du clic sur "Submit"
    @FXML
    private void handleSubmit(ActionEvent event) {
        int requestedDays = Integer.parseInt(numberOfDays.getText());

        if (requestedDays > 0 && requestedDays <= totalLeaveDays) {
            System.out.println("Leave request submitted: " + requestedDays + " days.");
            totalLeaveDays -= requestedDays; // Mettre à jour les jours restants
        } else {
            System.out.println("Invalid leave request.");
        }
    }

    @FXML
    public void initialize() {
        if (rootPane != null) {
            // Attendre que la scène soit prête
            rootPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.getStylesheets().add(getClass().getResource("/Styles/CarteConge.css").toExternalForm());
                }
            });
        }


    }

    @FXML
    private void handleClose() {
        ((Stage) btnAnnuler.getScene().getWindow()).close();
    }

    private void afficherFenetre(String cheminFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(cheminFXML));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement du fichier FXML : " + cheminFXML);
        }
    }
    @FXML
    private void showTTPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouterTt.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
handleClose();
            // refreshUserList(); // Mettre à jour la liste après l'ajout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void showCongePopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouterConge.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
handleClose();
            // refreshUserList(); // Mettre à jour la liste après l'ajout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
