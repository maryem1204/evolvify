package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Services.AbsenceService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjouterAbsenceController implements Initializable {

    @FXML
    private DatePicker dpDateAbsence;

    @FXML
    private ComboBox<StatutAbsence> cbStatutAbsence;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private final AbsenceService absenceService = new AbsenceService();

    private int idEmploye; // L'ID de l'employé concerné

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Remplissage du ComboBox avec les valeurs de l'énumération StatutAbsence
        cbStatutAbsence.getItems().setAll(StatutAbsence.values());
    }

    // Permet de définir l'ID de l'employé concerné
    public void setIdEmploye(int idEmploye) {
        this.idEmploye = idEmploye;
    }

    @FXML
    private void handleSave() {
        // Vérification des champs
        LocalDate dateAbsence = dpDateAbsence.getValue();
        StatutAbsence statut = cbStatutAbsence.getValue();

        if (dateAbsence == null || statut == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        // Création d'un objet Absence
        Absence absence = new Absence();
        absence.setDate(java.sql.Date.valueOf(dateAbsence));
        absence.setStatus(statut);
        absence.setIdEmployee(idEmploye); // Assigner l'ID de l'employé

        // Enregistrement dans la base de données
        try {
            int result = absenceService.add(absence);
            if (result > 0) {
                showAlert("Succès", "Absence ajoutée avec succès.", Alert.AlertType.INFORMATION);
                closeWindow();
            } else {
                showAlert("Erreur", "Impossible d'ajouter l'absence.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
