package tn.esprit.Controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import tn.esprit.Entities.MoyenTransport;
import tn.esprit.Services.MoyenTransportCRUD;

import java.sql.SQLException;

public class AddTransportController {

    @FXML
    private Button Ajouter;

    @FXML
    private TextField Capacité;

    @FXML
    private AnchorPane add_transport;

    @FXML
    private TextField immatriculation;

    @FXML
    private SplitMenuButton status;

    @FXML
    private TextField type;




    private final MoyenTransportCRUD moyenTransportCRUD = new MoyenTransportCRUD();


   @FXML
    void ajouterTransport() {
        try {
            // Récupérer les valeurs des champs
            String typeMoyen = type.getText();
            int capacite = Integer.parseInt(Capacité.getText());
            int immatriculationValue = Integer.parseInt(immatriculation.getText());
            String statusValue = status.getText();  // Assurez-vous qu'une valeur est sélectionnée

            // Vérifier si les champs ne sont pas vides
            if (typeMoyen.isEmpty() || statusValue.isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            // Créer un objet MoyenTransport
            MoyenTransport mt = new MoyenTransport(0, typeMoyen, capacite, immatriculationValue, statusValue);

            // Insérer dans la base de données
            int id = moyenTransportCRUD.add(mt);
            if (id != -1) {
                showAlert("Succès", "Moyen de transport ajouté avec succès !");
                clearFields();
            } else {
                showAlert("Erreur", "L'ajout a échoué.");
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Capacité et immatriculation doivent être des nombres !");
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur inconnue s'est produite.");
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
        type.clear();
        Capacité.clear();
        immatriculation.clear();
        status.setText("Sélectionner"); // Remettre le menu à son état initial
    }


}

