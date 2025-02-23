package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.Entities.Abonnement;
import tn.esprit.Entities.StatusAbonnement;
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
    private TextField id_employe;
    @FXML
    private DatePicker date_exp;
    @FXML
    private TextField prix;
    @FXML
    private ComboBox<String> status;  // Remplacement du SplitMenuButton
    @FXML
    private ComboBox<String> type_ab; // Remplacement du SplitMenuButton

    private final AbonnementCRUD abonnementCRUD = new AbonnementCRUD();

    @FXML
    public void initialize() {
        // Remplit automatiquement la date du jour
        date_deb.setValue(LocalDate.now());

        // Initialisation des valeurs des ComboBox
        type_ab.getItems().addAll("Mensuel", "Trimestriel", "Annuel");
        status.getItems().addAll("ACTIF", "INACTIF");

        // Sélection par défaut
        type_ab.setValue("Mensuel");
        status.setValue("ACTIF");
    }

    @FXML
    void ajouterAbonnement() {
        try {
            // Vérification des champs vides
            if (id_employe.getText().isEmpty() || prix.getText().isEmpty() ||
                    date_deb.getValue() == null || date_exp.getValue() == null ||
                    type_ab.getValue() == null || status.getValue() == null) {

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
            String typeValue = type_ab.getValue();
            double prixValue = Double.parseDouble(prix.getText());
            String statusValue = status.getValue();

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

            // Conversion du statut en Enum
            StatusAbonnement statusEnum = StatusAbonnement.valueOf(statusValue.toUpperCase());

            // Création d'un objet Abonnement
            Abonnement abonnement = new Abonnement(0, typeValue, dateDebutValue, dateExpValue, prixValue, idEmployeValue, statusEnum);

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
        } catch (IllegalArgumentException e) {
            showAlert("Erreur", "Le statut sélectionné est invalide !");
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
        type_ab.setValue("Mensuel");
        prix.clear();
        date_deb.setValue(LocalDate.now());
        date_exp.setValue(null);
        status.setValue("ACTIF");
    }
}
