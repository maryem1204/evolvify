package tn.esprit.Controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AjouterUserController {
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

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final Map<String, Role> roleMap = new HashMap<>();

    @FXML
    private void initialize() {
        roleMap.put("Employé", Role.EMPLOYEE);
        roleMap.put("Responsable RH", Role.RESPONSABLE_RH);
        roleMap.put("Chef de projet", Role.CHEF_PROJET);
        roleComboBox.getItems().addAll(roleMap.keySet());
        roleComboBox.setValue("Employé");

        successMessageBox.setVisible(false);
        firstNameErrorLabel.setVisible(false);
        lastNameErrorLabel.setVisible(false);
        emailErrorLabel.setVisible(false);

        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> validateFirstName());
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> validateLastName());
        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmail());

        updateSubmitButtonState();
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
    private void handleSubmit() {
        if (submitButton.isDisabled()) {
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        Role role = roleMap.get(roleComboBox.getValue());

        Utilisateur newUser = new Utilisateur(firstName, lastName, email, "", null, null, null, role, 0, 0, null, "");

        try {
            int result = utilisateurService.add(newUser);
            if (result > 0) {
                showSuccessMessage(" Employé ajouté avec succès !");
                resetForm();
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
        }
    }

    private void showError(TextField field, Label label, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-border-insets: 0; -fx-padding: 2px;");
        field.setFocusTraversable(false);
        label.setText(message);
        label.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-border-color: transparent; -fx-background-color: transparent; -fx-padding: 3px;");
        label.setVisible(true);
    }

    private void showSuccess(TextField field, Label label, String message) {
        field.setStyle("-fx-border-color: green; -fx-border-width: 2px; -fx-border-insets: 0; -fx-padding: 2px;");
        field.setFocusTraversable(false);
        label.setText(message);
        label.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-border-color: transparent; -fx-background-color: transparent; -fx-padding: 3px;");
        label.setVisible(true);
    }

    private void showSuccessMessage(String message) {
        successMessageLabel.setText(message);
        successMessageBox.setVisible(true);
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> successMessageBox.setVisible(false));
        delay.play();
    }

    @FXML
    private void closeSuccessMessage() {
        successMessageBox.setVisible(false);
    }

    private void resetForm() {
        firstNameField.clear();
        firstNameField.setStyle("");

        lastNameField.clear();
        lastNameField.setStyle("");

        emailField.clear();
        emailField.setStyle("");

        roleComboBox.setValue("Employé");

        firstNameErrorLabel.setText("");
        firstNameErrorLabel.setVisible(false);

        lastNameErrorLabel.setText("");
        lastNameErrorLabel.setVisible(false);

        emailErrorLabel.setText("");
        emailErrorLabel.setVisible(false);

        submitButton.setDisable(true);
    }

    @FXML
    private void handleClose() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }

    @FXML
    private Button closeSuccessButton;

    @FXML
    private void handleCloseSuccessMessage() {
        successMessageBox.setVisible(false);
    }

}
