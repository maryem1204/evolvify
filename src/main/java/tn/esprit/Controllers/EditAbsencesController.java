package tn.esprit.Controllers;

import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Services.AbsenceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class EditAbsencesController {

    @FXML
    private DatePicker dateField; // Un seul champ date, car Absence a un seul champ 'date'

    @FXML
    private ComboBox<StatutAbsence> statusField; // Utilisation de l'Enum StatutAbsence

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private AbsenceService absenceService;
    private Absence absence;

    public void setAbsenceService(AbsenceService absenceService) {
        this.absenceService = absenceService;
    }

    public void setAbsence(Absence absence) {
        this.absence = absence;
        loadAbsenceDetails();
    }

    private void loadAbsenceDetails() {
        if (absence != null) {
            // Conversion de Date -> LocalDate pour DatePicker
            dateField.setValue(absence.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

            // Met à jour le champ status (statut dans la BDD)
            statusField.setValue(absence.getStatus());
        }
    }

    @FXML
    private void initialize() {
        statusField.getItems().setAll(StatutAbsence.values()); // Remplit la ComboBox avec les valeurs de l'Enum

        saveButton.setOnAction(event -> saveAbsence());
        cancelButton.setOnAction(event -> closeWindow());
    }

    private void saveAbsence() {
        if (validateInput()) {
            absence.setDate(Date.from(dateField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            absence.setStatus(statusField.getValue());

            try {
                absenceService.update(absence);  // Database operation
                showAlert("Succès", "L'absence a été mise à jour avec succès.", Alert.AlertType.INFORMATION);
                closeWindow();
            } catch (SQLException e) {
                showAlert("Erreur", "Une erreur est survenue lors de la mise à jour de l'absence : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }


    private boolean validateInput() {
        if (dateField.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date.", Alert.AlertType.ERROR);
            return false;
        }
        if (statusField.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un statut.", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
