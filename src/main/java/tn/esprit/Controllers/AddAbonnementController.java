package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.Entities.Abonnement;
import tn.esprit.Services.AbonnementCRUD;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class AddAbonnementController {

    @FXML
    private Button ajouter;
    @FXML
    private DatePicker date_deb;
    @FXML
    private TextField id_employe;  // Maintien du nom avec accent
    @FXML
    private DatePicker date_exp;
    @FXML
    private TextField prix;
    @FXML
    private SplitMenuButton status;
    @FXML
    private SplitMenuButton type_ab;

    private final AbonnementCRUD abonnementCRUD = new AbonnementCRUD();

    @FXML
    public void initialize() {
        date_deb.setValue(LocalDate.now()); // Remplit automatiquement la date du jour
    }
    @FXML
    void ajouterAbonnement() {
        try {
            // Vérification des champs vides
            if (id_employe.getText().isEmpty() || type_ab.getText().isEmpty() || prix.getText().isEmpty() ||
                    date_deb.getValue() == null || date_exp.getValue() == null || status.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            // Vérification que id_employé est un nombre valide
            if (!isInteger(id_employe.getText())) {
                showAlert("Erreur", "L'ID de l'employé doit être un nombre entier valide !");
                return;
            }
            int idEmployeValue = Integer.parseInt(id_employe.getText());

            // Conversion des valeurs
            String typeValue = type_ab.getText();
            double prixValue = Double.parseDouble(prix.getText());
            String statusValue = status.getText();

            // Conversion de LocalDate en Date
            LocalDate localDateDebut = date_deb.getValue();
            LocalDate localDateExp = date_exp.getValue();
            Date dateDebutValue = Date.from(localDateDebut.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dateExpValue = Date.from(localDateExp.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Vérification des dates
            if (dateDebutValue.after(dateExpValue)) {
                showAlert("Erreur", "La date de début doit être antérieure à la date d'expiration !");
                return;
            }

            // Création d'un objet Abonnement avec id_employé
            Abonnement abonnement = new Abonnement(0, typeValue, dateDebutValue, dateExpValue, prixValue, idEmployeValue, statusValue);

            // Ajout dans la base de données
            int id = abonnementCRUD.add(abonnement);
            if (id != -1) {
                showAlert("Succès", "Abonnement ajouté avec succès !");
                clearFields();
            } else {
                showAlert("Erreur", "L'ajout a échoué.");
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix doit être un nombre valide !");
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur inconnue s'est produite.");
        }
    }

    // Vérifie si une chaîne est un entier valide
    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        id_employe.clear();
        type_ab.setText("Sélectionner");
        prix.clear();
        date_deb.setValue(null);
        date_exp.setValue(null);
        status.setText("Sélectionner");
    }
}
