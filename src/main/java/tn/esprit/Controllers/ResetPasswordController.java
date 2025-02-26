package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.Services.UtilisateurService;

import java.sql.SQLException;

public class ResetPasswordController {

    @FXML
    private PasswordField password;
    @FXML
    private PasswordField ConfirmedPassword;
    @FXML
    private Button submitButton;
    @FXML
    private Label errorLabel;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private int userId;  // ID de l'utilisateur concerné par la réinitialisation
    private String token; // Token de validation

    /**
     * ✅ Permet de récupérer l'ID utilisateur et le token.
     */
    public void setResetToken(int userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    @FXML
    public void initialize() {
        submitButton.setOnAction(event -> handlePasswordReset());
    }

    /**
     * ✅ Vérifie et met à jour le mot de passe.
     */
    private void handlePasswordReset() {
        String newPassword = password.getText().trim();
        String confirmPassword = ConfirmedPassword.getText().trim();
        errorLabel.setText(""); // Réinitialiser le message d'erreur

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs !");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas !");
            return;
        }

        if (newPassword.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères !");
            return;
        }

        try {
            // Vérifier si le token est valide
            if (!utilisateurService.isValidResetToken(userId, token)) {
                showError("Lien de réinitialisation invalide ou expiré !");
                return;
            }

            // Mettre à jour le mot de passe
            utilisateurService.updatePassword(userId, newPassword);

            // Afficher un message de succès
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre mot de passe a été mis à jour avec succès !");

            // Fermer la fenêtre après succès
            Stage stage = (Stage) submitButton.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour du mot de passe !");
            e.printStackTrace();
        }
    }

    /**
     * ✅ Affiche une erreur dans l'interface.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    /**
     * ✅ Affiche une alerte.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
