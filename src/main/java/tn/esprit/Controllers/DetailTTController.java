package tn.esprit.Controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Entities.Conge;
import tn.esprit.Entities.Reason;
import tn.esprit.Entities.Statut;
import tn.esprit.Entities.Type;
import tn.esprit.Services.CongeService;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DetailTTController {
    @FXML
    private Button btnAnnuler, btnSubmit;
    @FXML
    private TextField numberOfDaysField, remainingTT;
    @FXML
    private DatePicker fromDate, toDate;
    @FXML
    private HBox successMessageBox;
    @FXML
    private Label successMessageLabel;
    @FXML
    private ComboBox<Reason> reasonComboBox;
    private static final int TOTAL_LEAVE_DAYS = 23;

    @FXML
    public void initialize() {
        calculateLeaveDays();
        reasonComboBox.setItems(FXCollections.observableArrayList(Reason.values()));
    }

    @FXML
    private void handleClose() {
        ((Stage) btnAnnuler.getScene().getWindow()).close();
    }

    @FXML
    private void calculateLeaveDays() {
        if (fromDate.getValue() != null && toDate.getValue() != null) {
            long daysBetween = ChronoUnit.DAYS.between(fromDate.getValue(), toDate.getValue()) + 1;
            numberOfDaysField.setText(daysBetween > 0 ? String.valueOf(daysBetween) : "0");
            remainingTT.setText(String.valueOf(Math.max(TOTAL_LEAVE_DAYS - daysBetween, 0)));
        }
    }

    @FXML
    private void handleSubmit() {
        if (fromDate.getValue() == null || toDate.getValue() == null) {
            showError("Veuillez sélectionner des dates valides.");
            return;
        }
        Reason selectedReason = reasonComboBox.getValue();

        if (selectedReason == null) {
            showError( "Veuillez sélectionner une raison.");
            return;
        }

        int requestedDays = Integer.parseInt(numberOfDaysField.getText());
        int remaining = Integer.parseInt(remainingTT.getText());

        if (requestedDays <= 0 || requestedDays > remaining) {
            showError("Jours demandés invalides ou supérieurs au solde.");
            return;
        }

        Conge newConge = new Conge();
        newConge.setLeave_start(Date.from(fromDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        newConge.setLeave_end(Date.from(toDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        newConge.setReason(selectedReason);
        newConge.setDescription(null);
        newConge.setNumber_of_days(requestedDays);

        newConge.setStatus(Statut.EN_COURS);

        try {
            int result = new CongeService().add(newConge);
            if (result > 0) showSuccessMessage("TT ajouté avec succès !");
        } catch (SQLException e) {
            showError("Erreur lors de l'ajout du TT : " + e.getMessage());
        }
    }

    private void showError(String message) {
        successMessageLabel.setText(message);
        successMessageLabel.setStyle("-fx-text-fill: red;");
        successMessageBox.setVisible(true);
    }


    private void showSuccess(TextField field, TextField label, String message) {
        field.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        label.setText(message);
        label.setStyle("-fx-text-fill: green;");
        label.setVisible(true);
    }

    // Méthode pour afficher un message de succès
    private void showSuccessMessage(String message) {
        successMessageLabel.setText(message);
        successMessageBox.setVisible(true);
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> successMessageBox.setVisible(false));
        delay.play();
    }

    @FXML
    private void handleCloseSuccessMessage() {
        successMessageBox.setVisible(false);
    }
}
