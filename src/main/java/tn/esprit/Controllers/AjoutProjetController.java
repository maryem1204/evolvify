package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Entities.Gender;
import tn.esprit.Entities.Projet;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.ProjetService;
import tn.esprit.Utils.MyDataBase;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AjoutProjetController {

    @FXML
    private TextField nomProjetField, abbreviationField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker startDatePicker, endDatePicker;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private ComboBox<Utilisateur> employeComboBox;
    @FXML
    private Button enregistrerButton, annulerButton, uploadButton;
    @FXML
    private Label fileNameLabel;

    private final ProjetService projetService = new ProjetService();
    private final Map<String, Projet.Status> statusMap = new HashMap<>();
    private File selectedFile;

    @FXML
    private void initialize() {
        statusMap.put("In progress", Projet.Status.IN_PROGRESS);
        statusMap.put("Completed", Projet.Status.COMPLETED);
        statusComboBox.getItems().addAll(statusMap.keySet());
        statusComboBox.setValue("In progress");

        uploadButton.setOnAction(event -> handleUploadFile());

        try {
            List<Utilisateur> utilisateurs = getEmployees();
            employeComboBox.getItems().addAll(utilisateurs);
            employeComboBox.setCellFactory(param -> new ListCell<Utilisateur>() {
                @Override
                protected void updateItem(Utilisateur item, boolean empty) {
                    super.updateItem(item, empty);
                    setText((item == null || empty) ? null : item.getFirstname() + " " + item.getLastname());
                }
            });
            employeComboBox.setButtonCell(new ListCell<Utilisateur>() {
                @Override
                protected void updateItem(Utilisateur item, boolean empty) {
                    super.updateItem(item, empty);
                    setText((item == null || empty) ? null : item.getFirstname() + " " + item.getLastname());
                }
            });
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
    }

    private List<Utilisateur> getEmployees() throws SQLException {
        List<Utilisateur> employees = new ArrayList<>();
        Connection cnx = MyDataBase.getInstance().getCnx();
        String query = "SELECT * FROM Users WHERE role = 'EMPLOYEE'";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            employees.add(new Utilisateur(
                    rs.getInt("id_employe"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getBytes("profilePhoto"),
                    rs.getDate("birthdayDate"),
                    rs.getDate("joiningDate"),
                    null,
                    0, 0, null, rs.getString("num_tel"),
                    Gender.valueOf(rs.getString("gender"))

            ));
        }
        return employees;
    }

    @FXML
    private void handleUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"),
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.txt"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) uploadButton.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            fileNameLabel.setText("Fichier sélectionné : " + selectedFile.getName());
        } else {
            fileNameLabel.setText("Aucun fichier sélectionné");
        }
    }

    @FXML
    private void handleEnregistrer() {
        String nomProjet = nomProjetField.getText().trim();
        String description = descriptionField.getText().trim();
        String statusValue = statusComboBox.getValue();
        Projet.Status statut = (statusValue != null) ? statusMap.get(statusValue) : Projet.Status.IN_PROGRESS;
        String abbreviation = abbreviationField.getText().trim();
        Utilisateur employe = employeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (nomProjet.isEmpty() || description.isEmpty() || statusValue == null || statut == null ||
                abbreviation.isEmpty() || employe == null || startDate == null || endDate == null) {
            showError("Tous les champs doivent être remplis !");
            return;
        }

        if (endDate.isBefore(startDate)) {
            showError("La date de fin ne peut pas être antérieure à la date de début.");
            return;
        }

        int id_employe = employe.getId_employe();
        Projet projet = new Projet(nomProjet, description, statut, endDate, startDate, abbreviation, id_employe, new byte[0]);

        if (projet.getStatus() == null) {
            showError("Le statut du projet est invalide.");
            return;
        }

        try {
            int result = projetService.add(projet);
            if (result > 0) {
                showSuccessMessage("Projet ajouté avec succès !");
                closeWindow();
            }
        } catch (SQLException e) {
            showError("Une erreur est survenue lors de l'ajout du projet.");
        }
    }

    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) enregistrerButton.getScene().getWindow();
        stage.close();
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
