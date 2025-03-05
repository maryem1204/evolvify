package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

public class FirstLoginDialogController {
    private Utilisateur user;
    private EmployeeDashController parentController;

    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private void initialize() {
        // Optional: Add password validation
    }

    public void setUser(Utilisateur user) {
        this.user = user;
    }

    public void setParentController(EmployeeDashController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleUpdatePassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (validatePasswords(newPassword, confirmPassword)) {
            try {
                // Hash the password
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

                // Update user's password
                UtilisateurService utilisateurService = new UtilisateurService(); // Create an instance of UtilisateurService
                utilisateurService.updatePassword(user.getId_employe(), hashedPassword);

                // Complete first login process
                parentController.completeFirstLogin();

                // Close the dialog
                ((Stage) newPasswordField.getScene().getWindow()).close();
            } catch (Exception e) {
                // Show error dialog
                showErrorAlert("Password Update Failed", e.getMessage());
            }
        }
    }

    private boolean validatePasswords(String newPassword, String confirmPassword) {
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showErrorAlert("Validation Error", "Please fill in all fields");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            showErrorAlert("Validation Error", "Passwords do not match");
            return false;
        }

        // Add additional password strength checks
        return true;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
