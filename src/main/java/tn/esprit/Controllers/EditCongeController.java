package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.Conge;
import tn.esprit.Entities.Type;
import tn.esprit.Entities.Reason;
import tn.esprit.Services.CongeService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class EditCongeController {
    @FXML
    private DatePicker dpStartDate, dpEndDate;
    @FXML
    private TextField txtDays, txtDescription;
    @FXML
    private ComboBox<Reason> cbReason;
    @FXML
    private Button btnSave, btnCancel;

    private Conge conge; // Objet à modifier
    private final CongeService congeService = new CongeService();

    @FXML
    public void initialize() {
        // Initialiser les choix du ComboBox
        cbReason.getItems().setAll(Reason.values());

        // Écouter le changement des dates
        dpStartDate.valueProperty().addListener((obs, oldVal, newVal) -> calculateDays());
        dpEndDate.valueProperty().addListener((obs, oldVal, newVal) -> calculateDays());
    }

    // 📌 Cette méthode est appelée depuis DashboardCongeRh pour passer le congé à modifier
    public void setConge(Conge conge) {
        this.conge = conge;

        // Pré-remplir les champs avec les valeurs actuelles
        if (conge.getLeave_start() != null) {
            dpStartDate.setValue(convertToLocalDate(conge.getLeave_start()));
        }
        if (conge.getLeave_end() != null) {
            dpEndDate.setValue(convertToLocalDate(conge.getLeave_end()));
        }


        cbReason.setValue(conge.getReason());
        txtDescription.setText(conge.getDescription());

        // Calculer et afficher le nombre de jours
        calculateDays();
    }

    // 📌 Méthode pour calculer le nombre de jours
    private void calculateDays() {
        if (dpStartDate.getValue() != null && dpEndDate.getValue() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(dpStartDate.getValue(), dpEndDate.getValue()) + 1;
            txtDays.setText(String.valueOf(days > 0 ? days : 0));
        }
    }

    // 📌 Action lors du clic sur "Enregistrer"
    @FXML
    private void handleSave() {
        if (dpStartDate.getValue() == null || dpEndDate.getValue() == null || cbReason.getValue() == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.", Alert.AlertType.ERROR);
            return;
        }

        conge.setLeave_start(convertToDate(dpStartDate.getValue()));
        conge.setLeave_end(convertToDate(dpEndDate.getValue()));
        conge.setReason(cbReason.getValue());
        conge.setDescription(txtDescription.getText());

        try {
            congeService.update(conge);
            showAlert("Succès", "Le congé a été mis à jour avec succès.", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de mettre à jour le congé.", Alert.AlertType.ERROR);
        }
    }

    // 📌 Action lors du clic sur "Annuler"
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    // 📌 Fermer la fenêtre actuelle
    private void closeWindow() {
        ((Stage) btnSave.getScene().getWindow()).close();
    }

    // 📌 Afficher une alerte
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 📌 Convertir java.sql.Date en LocalDate
    private LocalDate convertToLocalDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.toInstant().atZone(calendar.getTimeZone().toZoneId()).toLocalDate();
    }

    // 📌 Convertir LocalDate en java.sql.Date
    private Date convertToDate(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate);
    }
}
