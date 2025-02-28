package tn.esprit.Controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.Entities.Offre;
import tn.esprit.Services.OffreService;
import java.sql.Date;
import java.sql.SQLException;

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
    private ChoiceBox<Offre.Status> status;

    @FXML
    private TextField titre;

    private Offre offre;

    // Cette méthode est utilisée pour remplir les champs du formulaire avec les données de l'offre sélectionnée
    public void setOffre(Offre offre) {
        if (offre != null) {
            this.offre = offre;
            titre.setText(offre.getTitre());
            description.setText(offre.getDescription());


            // Remplir le ChoiceBox avec le statut de l'offre
            status.setValue(offre.getStatus());
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune offre sélectionnée.");
        }
    }

    // Cette méthode est appelée lors de l'initialisation pour remplir le ChoiceBox avec les valeurs de l'énumération Status
    @FXML
    private void initialize() {
        status.getItems().setAll(Offre.Status.values());  // Remplir le ChoiceBox avec toutes les valeurs de l'énumération Status
    }

    // Cette méthode est appelée lors de l'appui sur le bouton "Modifier"
    @FXML
    private void handleModifier(ActionEvent event) {
        try {
            // Vérifie si tous les champs sont remplis
            if (titre.getText().isEmpty() || description.getText().isEmpty() ||
                    datepub.getValue() == null || dateexp.getValue() == null ||
                    status.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Champ manquant", "Veuillez remplir tous les champs.");
                return;
            }

            // Récupère les valeurs du formulaire
            String titreValue = titre.getText();
            String descriptionValue = description.getText();
            Date datePub = Date.valueOf(datepub.getValue());  // Conversion de LocalDate en java.sql.Date
            Date dateExp = Date.valueOf(dateexp.getValue());  // Conversion de LocalDate en java.sql.Date

            // Récupère la valeur sélectionnée dans le ChoiceBox
            Offre.Status statusValue = status.getValue();  // Récupération directe de la valeur du ChoiceBox

            // Mettre à jour l'offre avec les nouvelles valeurs
            offre.setTitre(titreValue);
            offre.setDescription(descriptionValue);
            offre.setDatePublication(datePub);
            offre.setDateExpiration(dateExp);
            offre.setStatus(statusValue);

            // Mettre à jour l'offre dans la base de données
            OffreService crud = new OffreService();
            int result = crud.update(offre);  // Assurez-vous que la méthode `update()` dans `OffreService` existe

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
