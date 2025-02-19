package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.MoyenTransport;
import tn.esprit.Services.MoyenTransportCRUD;

import java.sql.SQLException;

public class ModifierTransportController {
    @FXML
    private Button annuler;
    @FXML
    private TextField type;
    @FXML
    private TextField immatriculation;
    @FXML
    private TextField capacité;
    @FXML
    private SplitMenuButton status;
    @FXML
    private Button modifierButton;

    private MoyenTransport moyenTransport;

    // Initialisation du formulaire avec les données actuelles
    public void setMoyenTransport(MoyenTransport moyenTransport) {
        if (moyenTransport != null) {
            this.moyenTransport = moyenTransport;
            type.setText(moyenTransport.getTypeMoyen());
            immatriculation.setText(String.valueOf(moyenTransport.getImmatriculation()));
            capacité.setText(String.valueOf(moyenTransport.getCapacité()));
            status.setText(moyenTransport.getStatus());
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun moyen de transport sélectionné.");
        }
    }

    // Gestion du bouton Modifier
    @FXML
    private void handleModifier(ActionEvent event) {
        try {
            // Vérification des valeurs saisies
            if (type.getText().isEmpty() || immatriculation.getText().isEmpty() || capacité.getText().isEmpty() || status.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champ manquant", "Veuillez remplir tous les champs.");
                return;
            }

            // Vérification que l'immatriculation et la capacité sont des nombres valides
            if (!isNumeric(immatriculation.getText()) || !isNumeric(capacité.getText())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer des valeurs numériques valides pour immatriculation et capacité.");
                return;
            }

            String typeMoyen = type.getText();
            int immatriculationValue = Integer.parseInt(immatriculation.getText());
            int capacitéValue = Integer.parseInt(capacité.getText());
            String statusValue = status.getText();

            // Mise à jour de l'objet moyenTransport
            moyenTransport.setTypeMoyen(typeMoyen);
            moyenTransport.setImmatriculation(immatriculationValue);
            moyenTransport.setCapacité(capacitéValue);
            moyenTransport.setStatus(statusValue);

            // Mise à jour dans la BDD
            MoyenTransportCRUD crud = new MoyenTransportCRUD();
            int result = crud.update(moyenTransport);

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le moyen de transport a été mis à jour avec succès !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec", "La mise à jour a échoué.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Une erreur est survenue lors de la mise à jour.");
            e.printStackTrace();
        }
    }

    // Vérifie si une chaîne de caractères est un nombre
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Gestion du choix de statut
    @FXML
    private void handleStatusSelection(ActionEvent event) {
        MenuItem selectedMenuItem = (MenuItem) event.getSource();
        status.setText(selectedMenuItem.getText());
    }

    // Méthode pour afficher une alerte
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        ((Stage) annuler.getScene().getWindow()).close(); // Ferme la fenêtre
    }
}
