package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Entities.Trajet;
import tn.esprit.Entities.StatusTrajet;
import tn.esprit.Services.TrajetCRUD;
import tn.esprit.Utils.MyDataBase;
import tn.esprit.Utils.TrajetEventBus;

import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddTrajetController {
    @FXML
    private StackPane stackPane; // The root container of your main FXML
    @FXML
    private AnchorPane contentPane; // Your form container
    @FXML
    private StackPane dialogContainer; // Container for embedded dialogs

    @FXML
    private Button ajouter;
    @FXML
    private TextField id_employe, distance, durée_estimé, pointArr, pointDep;
    @FXML
    private ComboBox<String> status, moyen_transport;

    private final Connection cnx = MyDataBase.getInstance().getCnx();
    private final TrajetCRUD trajetCRUD = new TrajetCRUD();


    public AddTrajetController() {
    }

    @FXML
    private void initialize() {
        id_employe.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                    int employeId = Integer.parseInt(newValue);
                    verifierAbonnement(employeId);
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "L'ID employé doit être un nombre valide.", Alert.AlertType.ERROR);
                }
            }
        });
        moyen_transport.setPromptText("Sélectionner");
    }

    private void verifierAbonnement(int employeId) {
        String query = "SELECT status FROM abonnement WHERE id_employe = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, employeId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next() && "actif".equalsIgnoreCase(rs.getString("status"))) {
                    chargerMoyensTransportsDisponibles();
                } else {
                    showAlert("Erreur", "L'employé n'a pas d'abonnement actif.", Alert.AlertType.WARNING);
                    moyen_transport.getItems().clear();
                    moyen_transport.setPromptText("Sélectionner");
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Impossible de vérifier l'abonnement.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void chargerMoyensTransportsDisponibles() {
        String query = "SELECT type_moyen FROM moyentransport WHERE status = 'disponible'";
        moyen_transport.getItems().clear();
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                moyen_transport.getItems().add(rs.getString("type_moyen"));
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Impossible de charger les moyens de transport.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouter() {
        if (!isInputValid()) return;

        try {
            // Fetch input values
            int employeId = Integer.parseInt(id_employe.getText().trim());
            String depart = pointDep.getText().trim();
            String arrivee = pointArr.getText().trim();

            double dist = Double.parseDouble(distance.getText().trim());
            Time duree = parseTime(durée_estimé.getText().trim());
            String moyen = moyen_transport.getValue();
            String statusValue = status.getValue();

            // Convert status to enum
            StatusTrajet statusEnum;
            try {
                statusEnum = StatusTrajet.valueOf(statusValue.toUpperCase());
            } catch (IllegalArgumentException e) {
                showAlert("Erreur", "Statut invalide. Veuillez entrer un statut valide.", Alert.AlertType.ERROR);
                return;
            }

            // Fetch transport ID
            int moyenId = getMoyenId(moyen);
            if (moyenId == -1) {
                showAlert("Erreur", "Le moyen de transport sélectionné n'est pas disponible.", Alert.AlertType.ERROR);
                return;
            }

            // Create a new Trajet object
            Trajet trajet = new Trajet(0, depart, arrivee, dist, duree, moyenId, employeId, statusEnum);
            int id = trajetCRUD.add(trajet);

            if (id > 0) {
                // Fire event for map update (if other components are listening)
              //TrajetEventBus.fireTrajetEvent(trajet);

                // Clear input fields
                clearFields();

                // Show an embedded map popup (using the location from the Trajet, e.g., the departure address)
                // You can modify this to use any address or logic you prefer.
                showMapPopup(trajet.getPointArr());
            } else {
                showAlert("Erreur", "L'ajout du trajet a échoué.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs valides pour l'ID employé et la distance.", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur SQL", "Une erreur est survenue lors de l'ajout du trajet.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Shows an embedded map popup using JFoenix's JFXDialog.
     * The popup loads the map_view.fxml and displays it within the dialogContainer.
     * This implementation ensures mouse events aren't blocked for the main form.
     */
    private void showMapPopup(String location) {
        try {
            // Charge le fichier FXML de la carte (map_view.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/map_view.fxml"));
            Parent mapContent = loader.load();

            // Optionnel : Si votre contrôleur de carte (TrajetMapViewController) a une méthode pour régler l'adresse,
            // vous pouvez la déclencher ici. Par exemple :
            // TrajetMapViewController mapController = loader.getController();
            // mapController.setLocation(location);

            // Créez un nouveau Stage pour le pop-up
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Carte - " + location);
            // Définir la taille souhaitée (par exemple, 600 x 400)
            Scene scene = new Scene(mapContent, 600, 400);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            afficherAlerte("Erreur", "Impossible d'afficher la carte.", Alert.AlertType.ERROR);
        }
    }

    private boolean isInputValid() {
        if (pointDep.getText().isEmpty() || pointArr.getText().isEmpty() || distance.getText().isEmpty() ||
                durée_estimé.getText().isEmpty() || status.getValue() == null || moyen_transport.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs correctement.", Alert.AlertType.WARNING);
            return false;
        }

        try {
            Double.parseDouble(distance.getText());
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La distance doit être un nombre valide.", Alert.AlertType.ERROR);
            return false;
        }

        try {
            parseTime(durée_estimé.getText());
        } catch (IllegalArgumentException e) {
            showAlert("Erreur", "Format de durée invalide. Utilisez HH:mm:ss.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private Time parseTime(String timeString) {
        try {
            LocalTime localTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss"));
            return Time.valueOf(localTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format de durée invalide.");
        }
    }

    private int getMoyenId(String typeMoyen) throws SQLException {
        String query = "SELECT id_moyen FROM moyentransport WHERE type_moyen = ? AND status = 'disponible'";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, typeMoyen);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_moyen");
                }
            }
        }
        return -1;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        id_employe.clear();
        pointDep.clear();
        pointArr.clear();
        distance.clear();
        durée_estimé.clear();
        moyen_transport.getSelectionModel().clearSelection();
        status.setValue(null);
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType error) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
