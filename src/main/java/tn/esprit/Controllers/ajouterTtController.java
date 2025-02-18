package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.Entities.Statut;
import tn.esprit.Entities.Tt;
import tn.esprit.Services.TtService;

import java.sql.SQLException;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.Entities.Tt;
import tn.esprit.Entities.Statut;
import tn.esprit.Services.TtService;

import java.sql.SQLException;
import java.time.LocalDate;

    public class ajouterTtController {

        @FXML
        private DatePicker dpStartDate;
        @FXML
        private DatePicker dpEndDate;
        @FXML
        private ComboBox<String> cbStatus;
        @FXML
        private TextField txtIdEmploye;
        @FXML
        private TextField txtDays;
        @FXML
        private Button btnCancel;
        private TtService ttService = new TtService();

        @FXML
        public void initialize() {

            // Ajouter un écouteur pour calculer automatiquement le nombre de jours
            dpStartDate.valueProperty().addListener((obs, oldDate, newDate) -> calculateDays());
            dpEndDate.valueProperty().addListener((obs, oldDate, newDate) -> calculateDays());
        }

        private void calculateDays() {
            LocalDate start = dpStartDate.getValue();
            LocalDate end = dpEndDate.getValue();

            if (start != null && end != null && !end.isBefore(start)) {
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
                txtDays.setText(String.valueOf(daysBetween));
            } else {
                txtDays.setText("");
            }
        }

        @FXML
        private void handleSave() {
            try {
                LocalDate leaveStart = dpStartDate.getValue();
                LocalDate leaveEnd = dpEndDate.getValue();
                int numberOfDays = Integer.parseInt(txtDays.getText());

                // Vérification des valeurs saisies
                if (leaveStart == null || leaveEnd == null  || txtDays.getText().isEmpty()) {
                    System.out.println("Veuillez remplir tous les champs.");
                    return;
                }

                // Créer un objet Tt
                Tt tt = new Tt();
                tt.setLeave_start(java.sql.Date.valueOf(leaveStart));
                tt.setLeave_end(java.sql.Date.valueOf(leaveEnd));
                tt.setNumber_of_days(numberOfDays);
                tt.setId_employe(1);
                tt.setStatus(Statut.EN_COURS);

                // Ajouter TT dans la base de données
                int result = ttService.add(tt);

                if (result > 0) {
                    System.out.println("TT ajouté avec succès !");
                } else {
                    System.out.println("Erreur lors de l'ajout du TT.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Erreur de connexion à la base de données.");
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer des valeurs valides.");
            }
        }

        private void closeWindow() {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        }

        @FXML
        private void handleCancel() {
            // Logique pour fermer la fenêtre ou effacer les champs
            dpStartDate.setValue(null);
            dpEndDate.setValue(null);
            txtDays.setText("");
            closeWindow();

        }
    }



