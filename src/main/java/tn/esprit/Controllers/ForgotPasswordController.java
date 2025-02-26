package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class ForgotPasswordController {
    @FXML
    private Hyperlink backToLogin;
    @FXML
    private TextField emailField;
    @FXML
    private Button submitButton;
    @FXML
    private Label errorLabel;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        submitButton.setOnAction(event -> {
            try {
                handleForgotPassword();
            } catch (SQLException e) {
                showError("Erreur de base de données, veuillez réessayer !");
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleForgotPassword() throws SQLException {
        String email = emailField.getText().trim();
        errorLabel.setText(""); // Réinitialise le message d'erreur

        if (email.isEmpty()) {
            showError("Veuillez entrer votre email !");
            return;
        }

        Utilisateur user = utilisateurService.findByEmail(email);
        if (user == null) {
            showError("Aucun compte trouvé avec cet email !");
            return;
        }

        // Générer un token sécurisé
        String token = UUID.randomUUID().toString();
        utilisateurService.savePasswordResetToken(user.getId_employe(), token);

        utilisateurService.sendPasswordResetEmail(email, token);
    }

    /**
     * Affiche une alerte avec un message.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche un message d'erreur dans le label.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    /**
     * ✅ Ouvre la fenêtre de réinitialisation avec le token.
     */
    private void openResetPasswordWindow(int userId, String token) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/resetPwd.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et passer les informations
            ResetPasswordController controller = loader.getController();
            controller.setResetToken(userId, token);

            Stage stage = new Stage();
            stage.setTitle("Réinitialisation du mot de passe");
            stage.setScene(new Scene(root));
            stage.show();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Veuillez entrer votre nouveau mot de passe.");
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture de la fenêtre !");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/loginUser.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page de connexion !");
            e.printStackTrace();
        }
    }
}
