package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Entities.Projet;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.ProjetService;
import tn.esprit.Services.UtilisateurService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ModifierProjetController {

    @FXML private TextField nomProjetField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField abbreviationField;
    @FXML private ListView<CheckBox> employeListView;
    @FXML private Button uploadButton;

    private ProjetService projetService = new ProjetService();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private Projet projetToEdit;
    private ProjectListController listPsController;

    public void setListUsersController(ProjectListController contrôleur) {
        this.listPsController = contrôleur;
    }

    public void initialize() {
        statusComboBox.getItems().addAll(
                List.of(Projet.Status.values()).stream().map(Enum::name).toList()
        );
        chargerEmployes();
    }

    private void chargerEmployes() {
        employeListView.getItems().clear();
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAllEmployees();
            List<Integer> employeIds = (projetToEdit != null) ? projetToEdit.getEmployes() : new ArrayList<>();

            for (Utilisateur user : utilisateurs) {
                CheckBox checkBox = new CheckBox(user.getFirstname() + " " + user.getLastname());
                checkBox.setUserData(user.getId_employe());
                if (employeIds.contains(user.getId_employe())) {
                    checkBox.setSelected(true);
                }
                employeListView.getItems().add(checkBox);
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des employés : " + e.getMessage());
        }
    }

    public void setUserData(Projet projet) {
        if (projet != null) {
            this.projetToEdit = projet;
            nomProjetField.setText(projet.getName());
            descriptionField.setText(projet.getDescription());
            startDatePicker.setValue(projet.getStarter_at());
            endDatePicker.setValue(projet.getEnd_date());
            statusComboBox.setValue(projet.getStatus().name());
            abbreviationField.setText(projet.getAbbreviation());
            chargerEmployes();
        }
    }

    @FXML
    private void handleEdit() {
        if (projetToEdit == null) {
            showError("Aucun projet sélectionné pour modification.");
            return;
        }

        String name = nomProjetField.getText().trim();
        String description = descriptionField.getText().trim();
        if (name.isEmpty() || description.isEmpty()) {
            showError("Les champs Nom et Description sont obligatoires.");
            return;
        }

        Projet.Status status;
        try {
            status = Projet.Status.valueOf(statusComboBox.getValue());
        } catch (IllegalArgumentException e) {
            showError("Le statut sélectionné est invalide.");
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if (startDate == null || endDate == null) {
            showError("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        projetToEdit.setName(name);
        projetToEdit.setDescription(description);
        projetToEdit.setStatus(status);
        projetToEdit.setStarter_at(startDate);
        projetToEdit.setEnd_date(endDate);
        projetToEdit.setAbbreviation(abbreviationField.getText().trim());

        List<Integer> selectedEmployeeIds = new ArrayList<>();
        for (CheckBox checkBox : employeListView.getItems()) {
            if (checkBox.isSelected()) {
                selectedEmployeeIds.add((Integer) checkBox.getUserData());
            }
        }
        projetToEdit.setEmployes(selectedEmployeeIds);

        try {
            projetService.update(projetToEdit);
            showConfirmation("Projet mis à jour avec succès !");
            closeWindow();
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour du projet : " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomProjetField.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void handleUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                showConfirmation("Fichier " + selectedFile.getName() + " chargé avec succès !");
            } catch (IOException e) {
                showError("Erreur lors du chargement du fichier : " + e.getMessage());
            }
        } else {
            showError("Aucun fichier sélectionné.");
        }
    }

}
