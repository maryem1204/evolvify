package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Entities.Gender;
import tn.esprit.Entities.Projet;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.ProjetService;
import tn.esprit.Utils.MyDataBase;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

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
    private ListView<HBox> employeListView;
    @FXML
    private Button enregistrerButton, annulerButton, uploadButton;
    @FXML
    private Label fileNameLabel;

    private final ProjetService projetService = new ProjetService();
    private final Map<String, Projet.Status> statusMap = new HashMap<>();
    private File selectedFile;
    private final List<Utilisateur> employes = new ArrayList<>();

    @FXML
    private void initialize() {
        statusMap.put("In progress", Projet.Status.IN_PROGRESS);
        statusMap.put("Completed", Projet.Status.COMPLETED);
        statusComboBox.getItems().addAll(statusMap.keySet());
        statusComboBox.setValue("In progress");

        uploadButton.setOnAction(event -> handleUploadFile());

        try {
            loadEmployees();
        } catch (SQLException e) {
            showError("Erreur lors de la récupération des employés : " + e.getMessage());
        }
    }

    private void loadEmployees() throws SQLException {
        employes.clear();
        employes.addAll(getEmployees());

        ObservableList<HBox> items = FXCollections.observableArrayList();
        for (Utilisateur employe : employes) {
            CheckBox checkBox = new CheckBox(employe.getFirstname() + " " + employe.getLastname());
            checkBox.setUserData(employe);
            items.add(new HBox(checkBox));
        }
        employeListView.setItems(items);
    }

    private List<Utilisateur> getEmployees() throws SQLException {
        List<Utilisateur> employees = new ArrayList<>();
        String query = "SELECT * FROM Users WHERE role = 'EMPLOYEE'";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                employees.add(new Utilisateur(
                        rs.getInt("id_employe"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("profilePhoto"),
                        rs.getDate("birthdayDate"),
                        rs.getDate("joiningDate"),
                        null,
                        0, 0, null, rs.getString("num_tel"),
                        Gender.valueOf(rs.getString("gender"))
                ));
            }
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

        fileNameLabel.setText((selectedFile != null) ? "Fichier sélectionné : " + selectedFile.getName() : "Aucun fichier sélectionné");
    }

    @FXML
    private void handleEnregistrer() {
        String nomProjet = nomProjetField.getText().trim();
        String description = descriptionField.getText().trim();
        String statusValue = statusComboBox.getValue();
        Projet.Status statut = (statusValue != null) ? statusMap.get(statusValue) : Projet.Status.IN_PROGRESS;
        String abbreviation = abbreviationField.getText().trim();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        List<Integer> selectedEmployees = new ArrayList<>();
        for (HBox hbox : employeListView.getItems()) {
            CheckBox checkBox = (CheckBox) hbox.getChildren().get(0);
            if (checkBox.isSelected()) {
                Utilisateur employe = (Utilisateur) checkBox.getUserData();
                selectedEmployees.add(employe.getId_employe());
            }
        }

        if (nomProjet.isEmpty() || description.isEmpty() || statusValue == null || statut == null ||
                abbreviation.isEmpty() || selectedEmployees.isEmpty() || startDate == null || endDate == null) {
            showError("Tous les champs doivent être remplis et au moins un employé doit être sélectionné !");
            return;
        }

        if (endDate.isBefore(startDate)) {
            showError("La date de fin ne peut pas être antérieure à la date de début.");
            return;
        }

        try {
            byte[] fileBytes = (selectedFile != null) ? Files.readAllBytes(selectedFile.toPath()) : new byte[0];
            Projet projet = new Projet(nomProjet, description, statut, endDate, startDate, abbreviation, fileBytes, selectedEmployees);

            int result = projetService.add(projet);
            if (result > 0) {
                showSuccessMessage("Projet ajouté avec succès !");
                closeWindow();
            }
        } catch (Exception e) {
            showError("Une erreur est survenue lors de l'ajout du projet: " + e.getMessage());
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
