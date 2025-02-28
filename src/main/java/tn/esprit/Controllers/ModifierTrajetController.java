package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Services.TrajetCRUD;
import tn.esprit.Entities.Trajet;
import tn.esprit.Entities.StatusTrajet;

import java.sql.SQLException;
import java.sql.Time;

public class ModifierTrajetController {

    @FXML
    private Button annuler;
    @FXML
    private TextField distance;
    @FXML
    private TextField dureeEstime;
    @FXML
    private TextField id_employe;
    @FXML
    private Button modifier;
    @FXML
    private ComboBox<String> moyen_transport; // Correction: MenuButton -> ComboBox
    @FXML
    private TextField pointArr;
    @FXML
    private TextField pointDep;
    @FXML
    private ComboBox<String> status; // Correction: TextField -> ComboBox

    private Trajet trajet;

    public void setTrajet(Trajet trajet) {
        if (trajet != null) {
            this.trajet = trajet;
            pointDep.setText(trajet.getPointDep());
            pointArr.setText(trajet.getPointArr());
            distance.setText(String.valueOf(trajet.getDistance()));
            dureeEstime.setText(trajet.getDuréeEstimé().toString());
            id_employe.setText(String.valueOf(trajet.getIdEmploye()));

            // Sélection du statut correspondant dans la ComboBox
            status.setValue(trajet.getStatus().name());

            // Conversion de l'ID moyen de transport en son libellé (si nécessaire)
            moyen_transport.setValue(convertIdToTransportName(trajet.getIdMoyen()));
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        try {
            if (trajet == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun trajet sélectionné.");
                return;
            }

            if (pointDep.getText().isEmpty() || pointArr.getText().isEmpty() ||
                    distance.getText().isEmpty() || dureeEstime.getText().isEmpty() ||
                    id_employe.getText().isEmpty() || status.getValue() == null ||
                    moyen_transport.getValue() == null) {

                showAlert(Alert.AlertType.WARNING, "Champ manquant", "Veuillez remplir tous les champs.");
                return;
            }

            double distanceValue;
            int employeId;
            try {
                distanceValue = Double.parseDouble(distance.getText());
                employeId = Integer.parseInt(id_employe.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer des valeurs numériques valides.");
                return;
            }

            Time duree = convertStringToTime(dureeEstime.getText());

            // Conversion du statut en Enum
            StatusTrajet statusTrajet;
            try {
                statusTrajet = StatusTrajet.valueOf(status.getValue().toUpperCase());
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Statut invalide. Veuillez choisir un statut valide.");
                return;
            }

            // Conversion du moyen de transport (si l'ID est stocké ailleurs, adapter cette partie)
            int moyenId = convertTransportNameToId(moyen_transport.getValue());

            trajet.setPointDep(pointDep.getText());
            trajet.setPointArr(pointArr.getText());
            trajet.setDistance(distanceValue);
            trajet.setDuréeEstimé(duree);
            trajet.setIdEmploye(employeId);
            trajet.setIdMoyen(moyenId);
            trajet.setStatus(statusTrajet);

            TrajetCRUD trajetCRUD = new TrajetCRUD();
            int result = trajetCRUD.update(trajet);

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Trajet mis à jour avec succès !");
                ((Stage) modifier.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la mise à jour du trajet.");
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Format de durée estimée incorrect.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Problème lors de la mise à jour du trajet.");
            e.printStackTrace();
        }
    }

    private Time convertStringToTime(String timeString) {
        try {
            return Time.valueOf(timeString);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le format de la durée estimée est incorrect. Format attendu : HH:MM:SS.");
            throw new IllegalArgumentException("Format de durée incorrect");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleAnnuler(ActionEvent event) {
        ((Stage) annuler.getScene().getWindow()).close();
    }

    // Simuler la conversion ID -> Nom pour le moyen de transport
    private String convertIdToTransportName(int id) {
        switch (id) {
            case 1: return "Voiture";
            case 2: return "Bus";
            case 3: return "Train";
            case 4: return "Vélo";
            default: return "Inconnu";
        }
    }

    // Simuler la conversion Nom -> ID pour le moyen de transport
    private int convertTransportNameToId(String name) {
        switch (name) {
            case "Voiture": return 1;
            case "Bus": return 2;
            case "Train": return 3;
            case "Vélo": return 4;
            default: return -1;
        }
    }
}