package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Services.TrajetCRUD;
import tn.esprit.Entities.Trajet;

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
    private MenuButton moyen_transport;
    @FXML
    private TextField pointArr;
    @FXML
    private TextField pointDep;
    @FXML
    private TextField status;

    private Trajet trajet; // ✅ Ajouter cette variable d'instance

    public void setTrajet(Trajet trajet) {
        if (trajet != null) {
            this.trajet = trajet; // ✅ Stocker l'objet pour l'utiliser dans handleModifier()
            pointDep.setText(trajet.getPointDep());
            pointArr.setText(trajet.getPointArr());
            distance.setText(String.valueOf(trajet.getDistance()));
            try {
                // Convertir Time en String pour afficher dans le TextField
                dureeEstime.setText(trajet.getDureeEstime().toString());
            } catch (Exception e) {
                dureeEstime.setText(trajet.getDureeEstime().toString()); // Affichage de la durée estimée
            }

            id_employe.setText(String.valueOf(trajet.getIdEmploye()));
            status.setText(trajet.getStatus());
            moyen_transport.setText(String.valueOf(trajet.getIdMoyen()));
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        try {
            if (trajet == null) { // ✅ Vérifier si trajet est null
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun trajet sélectionné.");
                return;
            }

            if (pointDep.getText().isEmpty() || pointArr.getText().isEmpty() ||
                    distance.getText().isEmpty() || dureeEstime.getText().isEmpty() ||
                    id_employe.getText().isEmpty() || status.getText().isEmpty() ||
                    moyen_transport.getText().isEmpty()) {

                showAlert(Alert.AlertType.WARNING, "Champ manquant", "Veuillez remplir tous les champs.");
                return;
            }

            // Validation des entrées numériques
            double distanceValue = 0;
            int employeId = 0;
            int moyenId = 0;
            try {
                distanceValue = Double.parseDouble(distance.getText());
                employeId = Integer.parseInt(id_employe.getText());
                moyenId = Integer.parseInt(moyen_transport.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer des valeurs numériques valides.");
                return;
            }

            // Convertir la durée estimée en Time (Assurez-vous que le format soit correct: HH:MM:SS)
            Time duree = convertStringToTime(dureeEstime.getText());

            // Mettre à jour l'objet existant avec les nouvelles valeurs
            trajet.setPointDep(pointDep.getText());
            trajet.setPointArr(pointArr.getText());
            trajet.setDistance(distanceValue);
            trajet.setDureeEstime(duree); // Set le Time dans l'objet Trajet
            trajet.setIdEmploye(employeId);
            trajet.setIdMoyen(moyenId);
            trajet.setStatus(status.getText());

            // Mise à jour du trajet dans la base de données via TrajetCRUD
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
            // Convertir la chaîne de caractères en Time
            // Assurez-vous que le format de la chaîne soit : "HH:MM:SS"
            return Time.valueOf(timeString);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le format de la durée estimée est incorrect. Veuillez entrer un format valide (HH:MM:SS).");
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
        // Fermer la fenêtre sans effectuer de mise à jour
        ((Stage) annuler.getScene().getWindow()).close();
    }
}
