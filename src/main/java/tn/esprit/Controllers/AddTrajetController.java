package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import tn.esprit.Entities.Trajet;
import tn.esprit.Services.TrajetCRUD;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;

public class AddTrajetController {

    @FXML
    private Button ajouter;
    @FXML
    private TextField id_employe;
    @FXML
    private TextField distance;
    @FXML
    private TextField dureeEstime;
    @FXML
    private MenuButton moyen_transport;
    @FXML
    private TextField pointArr;
    @FXML
    private TextField pointDep;
    @FXML
    private TextField status;
    @FXML
    private AnchorPane add_trajet;

    private final Connection cnx = MyDataBase.getInstance().getCnx();
    private final TrajetCRUD trajetCRUD = new TrajetCRUD();

    @FXML
    private void initialize() {
        id_employe.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                verifierAbonnement(Integer.parseInt(newValue));
            }
        });
    }

    private void verifierAbonnement(int employeId) {
        String query = "SELECT status FROM abonnement WHERE id_employe = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, employeId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next() && "actif".equalsIgnoreCase(rs.getString("status"))) {
                    chargerMoyensTransportsDisponibles();
                } else {
                    showAlert("Erreur", "L'employé n'a pas d'abonnement actif.");
                    moyen_transport.getItems().clear();
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        }
    }

    private void chargerMoyensTransportsDisponibles() {
        String query = "SELECT id_moyen, type_moyen FROM moyentransport WHERE status = 'disponible'";
        moyen_transport.getItems().clear();
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                MenuItem item = new MenuItem(rs.getString("type_moyen"));
                item.setOnAction(event -> moyen_transport.setText(item.getText()));
                moyen_transport.getItems().add(item);
            }
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        try {
            int employeId = Integer.parseInt(id_employe.getText());
            String depart = pointDep.getText();
            String arrivee = pointArr.getText();
            double dist = Double.parseDouble(distance.getText());
            Time duree = Time.valueOf(dureeEstime.getText());  // Veuillez vous assurer que la durée est au format HH:mm:ss
            String moyen = moyen_transport.getText();
            String statusValue = status.getText();

            if (depart.isEmpty() || arrivee.isEmpty() || moyen.isEmpty() || statusValue.isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            Trajet trajet = new Trajet(0, depart, arrivee, dist, duree, getMoyenId(moyen), employeId, statusValue);
            int id = trajetCRUD.add(trajet);
            if (id > 0) {
                showAlert("Succès", "Trajet ajouté avec succès !");
                clearFields();
            } else {
                showAlert("Erreur", "L'ajout a échoué.");
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs numériques valides !");
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        } catch (IllegalArgumentException e) {
            showAlert("Erreur", "Le format de la durée est invalide. Utilisez HH:mm:ss.");
        }
    }

    private int getMoyenId(String typeMoyen) throws SQLException {
        String query = "SELECT id_moyen FROM moyentransport WHERE type_moyen = ? AND status = 'disponible'";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, typeMoyen);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_moyen");
                }
            }
        }
        return -1;
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
        pointDep.clear();
        pointArr.clear();
        distance.clear();
        dureeEstime.clear();
        moyen_transport.setText("Sélectionner");
        status.clear();
    }
}
