package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField textPasswordField;
    @FXML
    private Button toggleButton;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink forgotPasswordLink;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private boolean isPasswordVisible = false;

    @FXML
    private void initialize() {
        textPasswordField.setManaged(false);
        textPasswordField.setVisible(false);
        updateEyeIcon();
        toggleButton.setOnAction(e -> togglePasswordVisibility());

        // Associer le bouton login
        loginButton.setOnAction(this::handleLogin);
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            textPasswordField.setText(passwordField.getText());
            textPasswordField.setVisible(true);
            textPasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(textPasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            textPasswordField.setVisible(false);
            textPasswordField.setManaged(false);
        }
        updateEyeIcon();
    }

    private void updateEyeIcon() {
        String iconPath = isPasswordVisible ? "/images/eye_open.png" : "/images/eye_closed.png";
        ImageView eyeIcon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        eyeIcon.setFitWidth(20);
        eyeIcon.setFitHeight(20);
        toggleButton.setGraphic(eyeIcon);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText() : textPasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            Utilisateur utilisateur = utilisateurService.getUserByEmail(email);
            if (utilisateur != null) {
                // 🔥 Stocker l'utilisateur en session
                SessionManager.getInstance().setUtilisateurConnecte(utilisateur);

                redirectUser(utilisateur.getRole(), event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun compte trouvé avec cet email.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Problème de connexion à la base de données.");
        }
    }


    private void redirectUser(Role role, ActionEvent event) throws IOException {
        String fxmlFile;
        if (role == Role.RESPONSABLE_RH) {
            fxmlFile = "/fxml/dash.fxml";
        } else {
            fxmlFile = "/fxml/dashEmployee.fxml";
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forgetPwd.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de récupération de mot de passe.");
        }
    }
}
