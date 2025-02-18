package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.Entities.Offre;
import tn.esprit.Services.OffreService;
import javafx.scene.control.SplitMenuButton;
import java.sql.Date;
import java.sql.SQLException;
import java.time.ZoneId;

public class ModifierOffre {

    @FXML
    private Button buttonAdd;

    @FXML
    private Label dateExpir;

    @FXML
    private DatePicker dateexp;

    @FXML
    private DatePicker datepub;

    @FXML
    private TextArea description;

    @FXML
    private SplitMenuButton status;
    @FXML
    private TextField titre;

    private  Offre offre;
    public void setOffre(Offre offre) {
        if (offre != null) {
            this.offre = offre;
            titre.setText(offre.getTitre());
            description.setText(offre.getDescription());
            datepub.setValue(offre.getDatePublication().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());

            dateexp.setValue(offre.getDateExpiration().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());

            status.setText(offre.getStatus().toString());
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune offre sélectionnée.");
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        try {
            if (titre.getText().isEmpty() || description.getText().isEmpty() ||
                    datepub.getValue() == null || dateexp.getValue() == null ||
                    status.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champ manquant", "Veuillez remplir tous les champs.");
                return;
            }

            String titreValue = titre.getText();
            String descriptionValue = description.getText();
            Date datePub = Date.valueOf(datepub.getValue());
            Date dateExp = Date.valueOf(dateexp.getValue());
            Offre.Status statusValue = Offre.Status.valueOf(status.getText().toUpperCase());

            offre.setTitre(titreValue);
            offre.setDescription(descriptionValue);
            offre.setDatePublication(datePub);
            offre.setDateExpiration(dateExp);
            offre.setStatus(statusValue);

            OffreService crud = new OffreService();
            int result = crud.update(offre);

            if (result > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'offre a été mise à jour avec succès !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec", "La mise à jour a échoué.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Une erreur est survenue lors de la mise à jour.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Le statut entré est invalide.");
        }
    }
    // Méthode pour afficher une alerte
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
