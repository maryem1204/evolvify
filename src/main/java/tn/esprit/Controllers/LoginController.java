package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Utils.SessionManager;

//import java.awt.*;
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

        // Input validation
        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            Utilisateur utilisateur = utilisateurService.getUserByEmail(email);
            if (utilisateur != null) {
                try {
                    // Try standard BCrypt verification first
                    if (BCrypt.checkpw(password, utilisateur.getPassword())) {
                        handleSuccessfulLogin(utilisateur, event);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Mot de passe incorrect.");
                    }
                } catch (IllegalArgumentException e) {
                    if (e.getMessage().contains("Invalid salt revision")) {
                        // Convert $2y$ to $2a$ and try again
                        String convertedHash = utilisateur.getPassword().replace("$2y$", "$2a$");
                        if (BCrypt.checkpw(password, convertedHash)) {
                            handleSuccessfulLogin(utilisateur, event);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Mot de passe incorrect.");
                        }
                    } else {
                        // If it's some other error, rethrow it
                        throw e;
                    }
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun compte trouvé avec cet email.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Problème de connexion à la base de données.");
        }
    }

    // Add this helper method to avoid code duplication
    private void handleSuccessfulLogin(Utilisateur utilisateur, ActionEvent event) throws IOException {
        // Clear password from memory for security
        if (passwordField.isVisible()) {
            passwordField.clear();
        } else {
            textPasswordField.clear();
        }

        // Store user in session
        SessionManager.getInstance().setUtilisateurConnecte(utilisateur);
        redirectUser(utilisateur.getRole(), event);
    }

    private void redirectUser(Role role, ActionEvent event) throws IOException {
        String fxmlFile;
        String pageTitle;

        // Determine FXML file and page title based on user role
        switch (role) {
            case RESPONSABLE_RH:
                fxmlFile = "/fxml/dash.fxml";
                pageTitle = "Tableau de bord RH";
                break;
            case EMPLOYEE:
                fxmlFile = "/fxml/dashEmployee.fxml";
                pageTitle = "Tableau de bord Employé";
                break;
            default:
                showAlert(Alert.AlertType.ERROR, "Erreur", "Rôle non autorisé.");
                return;
        }

        // Load the appropriate dashboard
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(pageTitle);
        stage.setScene(new Scene(root));
        stage.show();
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null); // Remove header text
        alert.setContentText(message);

        // Apply CSS
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Styles/custom-alert.css").toExternalForm());

        // Make window draggable
        alert.initStyle(StageStyle.TRANSPARENT);

        // The OK button will already be styled by the CSS

        alert.showAndWait();
    }
    private void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Créer une nouvelle scène et l'affecter à la fenêtre
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Oublier le mot de passe");

            // Récupérer les dimensions de l'écran
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();

            // Ajuster la fenêtre en plein écran
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true); // Activer le mode plein écran

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page : " + fxmlFile);
        }
    }

    private boolean checkPassword(String plainTextPassword, String storedHash) {
        try {
            // Try the normal check
            return BCrypt.checkpw(plainTextPassword, storedHash);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Invalid salt revision")) {
                // Convert $2y$ to $2a$ and try again
                String convertedHash = storedHash.replace("$2y$", "$2a$");
                return BCrypt.checkpw(plainTextPassword, convertedHash);
            }
            throw e;
        }
    }


    @FXML
    private void handleForgotPassword(ActionEvent event) {
        switchScene(event, "/fxml/forgetPwd.fxml");
    }

}