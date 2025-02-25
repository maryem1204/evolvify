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
import java.util.List;

public class ModifierProjetController {

    @FXML private TextField nomProjetField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextField abbreviationField;
    @FXML private ComboBox<Utilisateur> employeComboBox;
    @FXML private Button  uploadButton;

    private ProjetService projetService = new ProjetService();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private Projet projetToEdit;
    private ProjectListController listPsController;

    public void setListUsersController(ProjectListController contrôleur) {
        this.listPsController = contrôleur;
    }

    public void initialize() {
        // Remplir le ComboBox des statuts
        statusComboBox.getItems().addAll(
                List.of(Projet.Status.values()).stream().map(Enum::name).toList()
        );

        // Charger les employés dans le ComboBox
        chargerEmployes();
    }

    private void chargerEmployes() {
        employeComboBox.getItems().clear(); // Nettoyer avant de remplir

        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAllEmployees(); // Récupérer la liste des employés
            employeComboBox.getItems().addAll(utilisateurs);

            // Afficher Nom + Prénom dans le ComboBox
            employeComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Utilisateur utilisateur, boolean empty) {
                    super.updateItem(utilisateur, empty);
                    setText(empty ? "" : utilisateur.getFirstname() + " " + utilisateur.getLastname());
                }
            });

            // Définir l'affichage après sélection
            employeComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Utilisateur utilisateur, boolean empty) {
                    super.updateItem(utilisateur, empty);
                    setText(empty ? "" : utilisateur.getFirstname() + " " + utilisateur.getLastname());
                }
            });

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

            // Sélectionner l'employé assigné au projet
            for (Utilisateur user : employeComboBox.getItems()) {
                if (user.getId_employe() == projet.getId_employe()) {
                    employeComboBox.setValue(user);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleUploadFile() {
        System.out.println("🔹 Bouton Upload File cliqué");

        if (projetToEdit == null) {
            showError("Aucun projet sélectionné pour ajouter un fichier.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                projetToEdit.setUploaded_files(fileBytes);

                // 🔄 Mise à jour du projet avec le fichier en base
                projetService.update(projetToEdit);

                System.out.println("✅ Fichier sélectionné : " + selectedFile.getName());
                showConfirmation("Fichier téléchargé avec succès !");
            } catch (IOException e) {
                showError("Erreur lors du chargement du fichier : " + e.getMessage());
            } catch (SQLException e) {
                showError("Erreur lors de l'enregistrement du fichier : " + e.getMessage());
            }
        } else {
            System.out.println("⚠ Aucun fichier sélectionné.");
        }
    }

    private void showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleEdit() {
        System.out.println("🔹 Bouton Enregistrer cliqué");

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
        if (statusComboBox.getValue() == null) {
            showError("Veuillez sélectionner un statut valide.");
            return;
        }

        try {
            status = Projet.Status.valueOf(statusComboBox.getValue());
        } catch (IllegalArgumentException e) {
            showError("Le statut sélectionné est invalide.");
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        Utilisateur selectedUser = employeComboBox.getValue();

        if (startDate == null || endDate == null || selectedUser == null) {
            showError("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Mise à jour des données du projet
        projetToEdit.setName(name);
        projetToEdit.setDescription(description);
        projetToEdit.setStatus(status);
        projetToEdit.setStarter_at(startDate);
        projetToEdit.setEnd_date(endDate);
        projetToEdit.setAbbreviation(abbreviationField.getText().trim());
        projetToEdit.setId_employe(selectedUser.getId_employe()); // Stocker l'ID et non l'objet

        // Vérifier si un fichier a été sélectionné avant d'écraser l'ancien
        if (projetToEdit.getUploaded_files() == null) {
            System.out.println("⚠ Aucun nouveau fichier sélectionné, conservation de l'ancien fichier.");
        }

        System.out.println("🔄 Mise à jour du projet en base...");

        try {
            projetService.update(projetToEdit);
            System.out.println("✅ Projet mis à jour avec succès !");
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

    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomProjetField.getScene().getWindow();
        stage.close();
    }
}
