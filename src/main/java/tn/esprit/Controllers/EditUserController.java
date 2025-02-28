package tn.esprit.Controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EditUserController {

    @FXML
    private TextField firstNameField, lastNameField, emailField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Label firstNameErrorLabel, lastNameErrorLabel, emailErrorLabel;
    @FXML
    private HBox successMessageBox;
    @FXML
    private Label successMessageLabel;
    @FXML
    private Button closeButton, submitButton;

    private Utilisateur currentUser;
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final Map<String, Role> roleMap = new HashMap<>();

    private ListUsersController listUsersController;

    public void setListUsersController(ListUsersController controller) {
        this.listUsersController = controller;
    }


    @FXML
    private void initialize() {
        // 🔹 Associer les libellés de rôle aux valeurs de l'enum Role
        roleMap.put("Employé", Role.EMPLOYEE);
        roleMap.put("Responsable RH", Role.RESPONSABLE_RH);
        roleMap.put("Chef de projet", Role.CHEF_PROJET);
        roleComboBox.getItems().addAll(roleMap.keySet());

        // 🔹 Cacher les messages d'erreur et de succès au démarrage
        successMessageBox.setVisible(false);
        firstNameErrorLabel.setVisible(false);
        lastNameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);

        // 🔹 Ajout des validations en temps réel
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> validateFirstName());
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> validateLastName());
        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmail());

        updateSubmitButtonState();
    }

    public void setUserData(Utilisateur user) {
        if (user != null) {
            this.currentUser = user;
            firstNameField.setText(user.getFirstname());
            lastNameField.setText(user.getLastname());
            emailField.setText(user.getEmail());

            // 🔹 Récupérer le libellé du rôle actuel pour éviter les erreurs
            roleComboBox.setValue(getRoleLabel(user.getRole()));

            validateFirstName();
            validateLastName();
            validateEmail();
        }
    }

    private String getRoleLabel(Role role) {
        for (Map.Entry<String, Role> entry : roleMap.entrySet()) {
            if (entry.getValue() == role) {
                return entry.getKey();
            }
        }
        return "Employé"; // Valeur par défaut si non trouvée
    }

    private void validateFirstName() {
        String firstName = firstNameField.getText().trim();
        if (firstName.isEmpty()) {
            showError(firstNameField, firstNameErrorLabel, "❌ Prénom requis !");
        } else {
            showSuccess(firstNameField, firstNameErrorLabel, "✅ Prénom valide !");
        }
        updateSubmitButtonState();
    }

    private void validateLastName() {
        String lastName = lastNameField.getText().trim();
        if (lastName.isEmpty()) {
            showError(lastNameField, lastNameErrorLabel, "❌ Nom requis !");
        } else {
            showSuccess(lastNameField, lastNameErrorLabel, "✅ Nom valide !");
        }
        updateSubmitButtonState();
    }

    private void validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$")) {
            showError(emailField, emailErrorLabel, "❌ Email invalide !");
        } else {
            showSuccess(emailField, emailErrorLabel, "✅ Email valide !");
        }
        updateSubmitButtonState();
    }

    private void updateSubmitButtonState() {
        boolean isValid = firstNameErrorLabel.getText().contains("✅") &&
                lastNameErrorLabel.getText().contains("✅") &&
                emailErrorLabel.getText().contains("✅");

        submitButton.setDisable(!isValid);
    }

    @FXML
    private void handleEditSubmit() {
        if (submitButton.isDisabled() || currentUser == null) {
            return;
        }

        // 🔹 Mise à jour des valeurs de l'utilisateur
        currentUser.setFirstname(firstNameField.getText().trim());
        currentUser.setLastname(lastNameField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());

        // 🔹 Vérifier et attribuer le rôle sélectionné
        String selectedRole = roleComboBox.getValue();
        if (selectedRole != null && roleMap.containsKey(selectedRole)) {
            currentUser.setRole(roleMap.get(selectedRole));
        }

        try {
            // 🔄 Mettre à jour l'utilisateur dans la base de données
            utilisateurService.update(currentUser);

            // ✅ Afficher un message de succès
            showSuccessMessage("Utilisateur modifié avec succès !");

            // 🔄 Mettre à jour la liste principale après modification
            ListUsersController.getInstance().refreshUserList();

            // ✅ Fermer la fenêtre après modification
            ((Stage) submitButton.getScene().getWindow()).close();

        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
        }
    }

    private void showError(TextField field, Label label, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        label.setText(message);
        label.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        label.setVisible(true);
    }

    private void showSuccess(TextField field, Label label, String message) {
        field.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        label.setText(message);
        label.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        label.setVisible(true);
    }

    private void showSuccessMessage(String message) {
        successMessageLabel.setText(message);
        successMessageBox.setVisible(true);

        // 🔹 Cacher le message après 3 secondes
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> successMessageBox.setVisible(false));
        delay.play();
    }

    @FXML
    private void handleClose() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleCloseSuccessMessage() {
        successMessageBox.setVisible(false);
    }
}
