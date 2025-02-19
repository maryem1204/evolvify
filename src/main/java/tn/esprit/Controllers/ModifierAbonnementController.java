package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.Abonnement;
import tn.esprit.Services.AbonnementCRUD;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private TextField id_employé;

    @FXML
    private Button modifier;

    @FXML
    private TextField prix;

    @FXML
    private SplitMenuButton status;

    @FXML
    private SplitMenuButton type_ab;

    private Abonnement abonnement;

    // Définition du format de date
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Initialisation du formulaire avec les données actuelles
    public void setAbonnement(Abonnement abonnement) {
        if (abonnement == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun abonnement sélectionné.");
            return;
        }

        this.abonnement = abonnement;
        type_ab.setText(abonnement.getType_Ab());
        prix.setText(String.valueOf(abonnement.getPrix()));

        // Convertir Date en LocalDate avant affichage
        date_deb.setValue(convertToLocalDateViaInstant(abonnement.getDate_debut()));
        date_exp.setValue(convertToLocalDateViaInstant(abonnement.getDate_exp()));
    }

    // Conversion de Date en LocalDate
    private static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    // Gestion du bouton Modifier
    @FXML
    private void handleModifier(ActionEvent event) {
        try {
            // Vérification que l'abonnement est bien sélectionné
            if (abonnement == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun abonnement sélectionné.");
                return;
            }

            // Vérification des valeurs saisies
            if (type_ab.getText().isEmpty() || prix.getText().isEmpty() || date_deb.getValue() == null || date_exp.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Champ manquant", "Veuillez remplir tous les champs.");
                return;
            }

            // Vérification que le prix est un nombre valide
            if (!isNumeric(prix.getText())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer un prix valide.");
                return;
            }

            // Conversion des valeurs saisies
            String typeValue = type_ab.getText();
            double prixValue = Double.parseDouble(prix.getText());  // Modification : double au lieu de float
            LocalDate dateDebutValue = date_deb.getValue();
            LocalDate dateExpValue = date_exp.getValue();

            // Vérification de la cohérence des dates
            if (dateExpValue.isBefore(dateDebutValue)) {
                showAlert(Alert.AlertType.ERROR, "Erreur de date", "La date d'expiration ne peut pas être antérieure à la date de début.");
                return;
            }

            // Mise à jour de l'objet abonnement
            abonnement.setType_Ab(typeValue);
            abonnement.setPrix(prixValue);  // Mise à jour avec prix de type double
            abonnement.setDate_debut(Date.from(dateDebutValue.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            abonnement.setDate_exp(Date.from(dateExpValue.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            // Mise à jour dans la BDD
            AbonnementCRUD crud = new AbonnementCRUD();
            int result = crud.update(abonnement);

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'abonnement a été mis à jour avec succès !");
                ((Stage) modifier.getScene().getWindow()).close(); // Ferme la fenêtre après succès
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec", "La mise à jour a échoué.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Une erreur est survenue lors de la mise à jour : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Vérifie si une chaîne de caractères est un nombre valide
    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?"); // Gère les entiers et les nombres à virgule flottante
    }

    // Méthode pour afficher une alerte
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        ((Stage) annuler.getScene().getWindow()).close(); // Ferme la fenêtre
    }
}
