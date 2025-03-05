package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.MoyenTransport;
import tn.esprit.Entities.StatusTransport;
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
    private MenuButton status;
    @FXML
    private Button modifierButton;

    private MoyenTransport moyenTransport;
    private StatusTransport selectedStatus; // Stocke le statut sélectionné

    private final MoyenTransportCRUD moyenTransportCRUD = new MoyenTransportCRUD();

    @FXML
    public void initialize() {
        // Ajouter les valeurs de StatusTransport au MenuButton
        for (StatusTransport s : StatusTransport.values()) {
            MenuItem item = new MenuItem(s.toString());
            item.setOnAction(event -> {
                status.setText(s.toString());
                selectedStatus = s;
            });
            status.getItems().add(item);
        }
    }

    // Initialisation avec les données existantes
    public void setMoyenTransport(MoyenTransport moyenTransport) {
        if (moyenTransport != null) {
            this.moyenTransport = moyenTransport;
            type.setText(moyenTransport.getTypeMoyen());
            immatriculation.setText(String.valueOf(moyenTransport.getImmatriculation()));
            capacité.setText(String.valueOf(moyenTransport.getCapacité()));
            status.setText(moyenTransport.getStatus().toString());
            selectedStatus = moyenTransport.getStatus(); // Assigner le statut actuel
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun moyen de transport sélectionné.");
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        try {
            // Vérifier si tous les champs sont remplis
            if (type.getText().trim().isEmpty() || immatriculation.getText().trim().isEmpty() || capacité.getText().trim().isEmpty() || selectedStatus == null) {
                showAlert(Alert.AlertType.WARNING, "Champ manquant", "Veuillez remplir tous les champs.");
                return;
            }

            // Vérification des entrées numériques
            int immatriculationValue, capacitéValue;
            try {
                immatriculationValue = Integer.parseInt(immatriculation.getText().trim());
                capacitéValue = Integer.parseInt(capacité.getText().trim());
                if (capacitéValue <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Capacité doit être un nombre positif !");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "L'immatriculation et la capacité doivent être des nombres !");
                return;
            }

            // Mettre à jour l'objet moyenTransport
            moyenTransport.setTypeMoyen(type.getText().trim());
            moyenTransport.setImmatriculation(immatriculationValue);
            moyenTransport.setCapacité(capacitéValue);
            moyenTransport.setStatus(selectedStatus);

            // Mise à jour dans la BDD
            int result = moyenTransportCRUD.update(moyenTransport);

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le moyen de transport a été mis à jour avec succès !");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec", "La mise à jour a échoué.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Une erreur est survenue lors de la mise à jour.");
            e.printStackTrace();
        }
    }

    // Gestion du choix de statut
    @FXML
    private void handleStatusSelection(ActionEvent event) {
        MenuItem selectedMenuItem = (MenuItem) event.getSource();
        status.setText(selectedMenuItem.getText());
        selectedStatus = StatusTransport.valueOf(selectedMenuItem.getText().toUpperCase());
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) annuler.getScene().getWindow()).close(); // Ferme la fenêtre
    }
}
