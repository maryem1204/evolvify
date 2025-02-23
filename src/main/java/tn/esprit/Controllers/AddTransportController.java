package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import tn.esprit.Entities.MoyenTransport;
import tn.esprit.Entities.StatusTransport;
import tn.esprit.Services.MoyenTransportCRUD;

import java.sql.SQLException;

public class AddTransportController {

    @FXML
    private Button Ajouter;

    @FXML
    private TextField Capacité;

    @FXML
    private AnchorPane add_transport;

    @FXML
    private TextField immatriculation;

    @FXML
    private MenuButton status;  // Le MenuButton pour afficher les statuts

    @FXML
    private TextField type;

    private final MoyenTransportCRUD moyenTransportCRUD = new MoyenTransportCRUD();
    private StatusTransport selectedStatus; // Variable pour stocker le statut sélectionné

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

    @FXML
    void ajouterTransport() {
        try {
            // Récupérer les valeurs des champs
            String typeMoyen = type.getText().trim();

            // Vérifier si la capacité est un entier positif
            int capacite;
            try {
                capacite = Integer.parseInt(Capacité.getText().trim());
                if (capacite <= 0) {
                    showAlert("Erreur", "Capacité doit être un nombre positif !");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Capacité doit être un nombre entier !");
                return;
            }

            // Vérifier si l'immatriculation est un entier
            int immatriculationValue;
            try {
                immatriculationValue = Integer.parseInt(immatriculation.getText().trim());
            } catch (NumberFormatException e) {
                showAlert("Erreur", "L'immatriculation doit être un nombre !");
                return;
            }

            // Vérifier si les champs ne sont pas vides
            if (typeMoyen.isEmpty() || selectedStatus == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            // Créer un objet MoyenTransport
            MoyenTransport mt = new MoyenTransport(0, typeMoyen, capacite, immatriculationValue, selectedStatus);

            // Insérer dans la base de données
            int id = moyenTransportCRUD.add(mt);
            if (id != -1) {
                showAlert("Succès", "Moyen de transport ajouté avec succès !");
                clearFields();
            } else {
                showAlert("Erreur", "L'ajout a échoué.");
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur inconnue s'est produite.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        type.clear();
        Capacité.clear();
        immatriculation.clear();
        status.setText("Sélectionner"); // Réinitialiser l'affichage
        selectedStatus = null; // Réinitialiser le statut sélectionné
    }
}
