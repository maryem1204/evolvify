package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.*;
import tn.esprit.Services.CongeService;
import tn.esprit.Entities.Utilisateur; // Importation de la classe Employe

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class ajouterCongeController implements Initializable {

    @FXML
    private DatePicker dpStartDate;

    @FXML
    private DatePicker dpEndDate;

    @FXML
    private ComboBox<Type> cbType;

    @FXML
    private ComboBox<Reason> cbReason;

    @FXML
    private TextField txtDays;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private final CongeService congeService = new CongeService(); // Instance du service pour gérer les congés

    private Utilisateur currentUser; // L'utilisateur connecté

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation des ComboBox avec les valeurs de l'énumération
        cbReason.getItems().setAll(Reason.values());

        // Écouteurs pour mettre à jour le nombre de jours lorsque les dates changent
        dpStartDate.setOnAction(event -> calculateDays());
        dpEndDate.setOnAction(event -> calculateDays());
    }

    // Méthode pour définir l'utilisateur connecté
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;

    }


    @FXML
    private void handleSave() {
        // Vérification des champs obligatoires
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        Reason selectedReason = cbReason.getValue();

        if (startDate == null || endDate == null  || selectedReason == null ) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.", Alert.AlertType.ERROR);
            return;
        }

        // Vérification que l'utilisateur est bien connecté
        /*if (currentUser == null) {
            showAlert("Erreur", "Impossible d'ajouter un congé sans employé.", Alert.AlertType.ERROR);
            return;
        }*/

        // Vérification que la date de début est avant la date de fin
        if (startDate.isAfter(endDate)) {
            showAlert("Erreur", "La date de début doit être antérieure à la date de fin.", Alert.AlertType.ERROR);
            return;
        }

        // Calcul du nombre de jours
        int numberOfDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (numberOfDays <= 0) {
            showAlert("Erreur", "Le nombre de jours doit être supérieur à zéro.", Alert.AlertType.ERROR);
            return;
        }

        // Création de l'objet Conge
        Conge newConge = new Conge();
        newConge.setLeave_start(java.sql.Date.valueOf(startDate));
        newConge.setLeave_end(java.sql.Date.valueOf(endDate));
        newConge.setNumber_of_days(numberOfDays);



        newConge.setReason(selectedReason);
        newConge.setStatus(Statut.EN_COURS); // Statut par défaut "EN_COURS"
        newConge.setDescription(null); // Ne pas stocker de description
       // newConge.setId_employe(currentUser.getId_employe()); // Assigner l'ID de l'employé connecté
         newConge.setId_employe(1);

        // Enregistrement dans la base de données
        try {
            int result = congeService.add(newConge);
            if (result > 0) {
                showAlert("Succès", "Le congé a été ajouté avec succès.", Alert.AlertType.INFORMATION);
                closeWindow();
            } else {
                showAlert("Erreur", "Impossible d'ajouter le congé.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Erreur lors de l'ajout du congé : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void calculateDays() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        if (startDate != null && endDate != null && !startDate.isAfter(endDate)) {
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            txtDays.setText(String.valueOf(daysBetween));
        } else {
            txtDays.setText("0");
        }
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
