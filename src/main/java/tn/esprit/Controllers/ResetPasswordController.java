package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;

public class ResetPasswordController {

    @FXML
    private PasswordField newPasswordField, confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button resetButton;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private int userId;  // ID de l'utilisateur à mettre à jour

    /**
     * Initialise le contrôleur avec l'ID de l'utilisateur récupéré de l'interface ForgotPasswordController.
     * @param userId Identifiant de l'utilisateur qui veut réinitialiser son mot de passe.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @FXML
    private void resetPassword() {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        errorLabel.setText("");

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs !");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas !");
            return;
        }

        try {
            utilisateurService.updatePassword(userId, newPassword);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Mot de passe réinitialisé avec succès !");
            openLoginScene();  // Rediriger vers login après succès
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour du mot de passe !");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page : " + fxmlFile);
        }
    }

    /**
     * Ouvre la page de connexion après la réinitialisation du mot de passe.
     * Cette version ferme l'ancienne fenêtre et ouvre une nouvelle.
     */
    private void openLoginScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/loginUser.fxml"));
            Parent root = loader.load();

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) resetButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture de la page de connexion !");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        switchScene(event, "/fxml/loginUser.fxml");
    }
}
