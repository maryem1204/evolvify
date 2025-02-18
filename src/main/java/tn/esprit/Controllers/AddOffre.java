package tn.esprit.Controllers;
import tn.esprit.Entities.Offre;
import tn.esprit.Services.OffreService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class AddOffre {

    @FXML
    private Button buttonAdd;

    @FXML
    private Label dateExpir;

    @FXML
    private DatePicker datepub;

    @FXML
    private DatePicker dateexp;

    @FXML
    private TextArea description;

    @FXML
    private TextField titre;
    @FXML
    void add(ActionEvent event) throws IOException, SQLException {
        // Vérification si les champs sont vides
        if (titre.getText().isEmpty() || description.getText().isEmpty() || datepub.getValue() == null || dateexp.getValue() == null) {
            // Affichage d'un message d'alerte en cas de champ vide
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error");
            alert.setContentText("Tous les champs doivent être remplis !");
            alert.show();
        } else {
            // Récupérer les dates depuis les DatePickers
            LocalDate localDatePublication = datepub.getValue();
            LocalDate localDateExpiration = dateexp.getValue();

            // Convertir LocalDate en java.sql.Date
            Date datePublication = Date.valueOf(localDatePublication);
            Date dateExpiration = Date.valueOf(localDateExpiration);

            // Créer l'objet Offre
            Offre offre = new Offre(
                    titre.getText(),
                    description.getText(),
                    datePublication,
                    dateExpiration
            );

            // Création d'un service pour ajouter l'offre à la base de données
            OffreService offreService = new OffreService();

            try {
                // Ajouter l'offre à la base de données
                int rowsAffected = offreService.add(offre);

                // Affichage du résultat en fonction du nombre de lignes affectées
                if (rowsAffected > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setContentText("Offre ajoutée avec succès !");
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Échec de l'ajout de l'offre.");
                    alert.show();
                }
            } catch (SQLException e) {
                // Gestion d'une erreur de base de données
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setContentText("Erreur lors de l'ajout de l'offre : " + e.getMessage());
                alert.show();
            }
        }
    }

}
