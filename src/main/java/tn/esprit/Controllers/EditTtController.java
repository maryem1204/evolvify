package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.Tt;
import tn.esprit.Services.TtService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class EditTtController {
    @FXML
    private DatePicker dpStartDate, dpEndDate;
    @FXML
    private TextField txtDays;
    @FXML
    private Button btnSave, btnCancel;

    private Tt currentTt; // Objet à modifier
    private final TtService ttService = new TtService();

    @FXML
    public void initialize() {
        // Écouter les changements des dates
        dpStartDate.valueProperty().addListener((obs, oldVal, newVal) -> calculateDays());
        dpEndDate.valueProperty().addListener((obs, oldVal, newVal) -> calculateDays());
    }

    // 📌 Cette méthode est appelée depuis DashboardTt pour passer le télétravail à modifier
    public void setTt(Tt tt) {
        this.currentTt = tt;

        // Pré-remplir les champs avec les valeurs actuelles
        if (tt.getLeave_start() != null) {
            dpStartDate.setValue(convertToLocalDate(tt.getLeave_start()));
        }
        if (tt.getLeave_end() != null) {
            dpEndDate.setValue(convertToLocalDate(tt.getLeave_end()));
        }

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
        if (dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
            showAlert("Erreur", "Les dates doivent être sélectionnées.", Alert.AlertType.ERROR);
            return;
        }

        currentTt.setLeave_start(convertToDate(dpStartDate.getValue()));
        currentTt.setLeave_end(convertToDate(dpEndDate.getValue()));
        currentTt.setNumber_of_days(Integer.parseInt(txtDays.getText()));

        try {
            ttService.update(currentTt);
            showAlert("Succès", "Le télétravail a été mis à jour avec succès.", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de mettre à jour le télétravail.", Alert.AlertType.ERROR);
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
