package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.Abonnement;
import tn.esprit.Entities.StatusAbonnement;
import tn.esprit.Services.AbonnementCRUD;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ModifierAbonnementController {

    @FXML
    private Button annuler;

    @FXML
    private DatePicker date_deb;

    @FXML
    private DatePicker date_exp;

    @FXML
    private TextField id_employe;

    @FXML
    private Button modifier;

    @FXML
    private TextField prix;

    @FXML
    private ComboBox<String> status;

    @FXML
    private ComboBox<String> type_ab;

    private Abonnement abonnement;

    public void setAbonnement(Abonnement abonnement) {
        if (abonnement == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun abonnement sélectionné.");
            return;
        }

        this.abonnement = abonnement;

        // Initialisation des champs avec les valeurs actuelles
        type_ab.setValue(abonnement.getType_Ab());
        prix.setText(String.valueOf(abonnement.getPrix()));
        date_deb.setValue(convertToLocalDate(abonnement.getDate_debut()));
        date_exp.setValue(convertToLocalDate(abonnement.getDate_exp()));
        status.setValue(abonnement.getStatus().toString());
        id_employe.setText(String.valueOf(abonnement.getId_employe())); // Ajout de l'ID employé
    }

    static LocalDate convertToLocalDate(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        try {
            if (abonnement == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun abonnement sélectionné.");
                return;
            }

            if (type_ab.getValue() == null || prix.getText().isEmpty() ||
                    date_deb.getValue() == null || date_exp.getValue() == null ||
                    status.getValue() == null || id_employe.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champ manquant", "Veuillez remplir tous les champs.");
                return;
            }

            if (!isNumeric(prix.getText())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer un prix valide.");
                return;
            }

            if (!isInteger(id_employe.getText())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer un ID employé valide.");
                return;
            }

            String typeValue = type_ab.getValue();
            double prixValue = Double.parseDouble(prix.getText());
            int employeId = Integer.parseInt(id_employe.getText());
            LocalDate dateDebutValue = date_deb.getValue();
            LocalDate dateExpValue = date_exp.getValue();

            if (dateExpValue.isBefore(dateDebutValue)) {
                showAlert(Alert.AlertType.ERROR, "Erreur de date", "La date d'expiration ne peut pas être avant la date de début.");
                return;
            }

            StatusAbonnement statusEnum = StatusAbonnement.valueOf(status.getValue().toUpperCase());

            abonnement.setType_Ab(typeValue);
            abonnement.setPrix(prixValue);
            abonnement.setId_employe(employeId);
            abonnement.setDate_debut(Date.from(dateDebutValue.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            abonnement.setDate_exp(Date.from(dateExpValue.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            abonnement.setStatus(statusEnum);

            AbonnementCRUD crud = new AbonnementCRUD();
            int result = crud.update(abonnement);

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'abonnement a été mis à jour avec succès !");
                ((Stage) modifier.getScene().getWindow()).close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec", "La mise à jour a échoué.");
            }
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le statut sélectionné est invalide !");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur lors de la mise à jour : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private boolean isInteger(String str) {
        return str.matches("\\d+");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        ((Stage) annuler.getScene().getWindow()).close();
    }
}
