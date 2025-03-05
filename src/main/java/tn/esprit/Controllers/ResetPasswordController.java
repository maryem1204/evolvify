package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;

public class ResetPasswordController {

    @FXML
    private PasswordField newPasswordField, confirmPasswordField;
    @FXML
    private TextField textNewPasswordField, textConfirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button resetButton, toggleNewPasswordButton, toggleConfirmPasswordButton;
    @FXML
    private HBox strengthMeterContainer;
    @FXML
    private Region strengthSection1;
    @FXML
    private Region strengthSection2;
    @FXML
    private Region strengthSection3;
    @FXML
    private Region strengthSection4;
    @FXML
    private Label strengthLabel;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private int userId;  // ID de l'utilisateur à mettre à jour

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @FXML
    public void initialize() {
        // Set up password strength monitoring
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });

        textNewPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });

        // Ensure password fields stay in sync
        newPasswordField.textProperty().bindBidirectional(textNewPasswordField.textProperty());
        confirmPasswordField.textProperty().bindBidirectional(textConfirmPasswordField.textProperty());

        // Initial setup for password visibility toggle buttons
        updateEyeIcon(toggleNewPasswordButton, isNewPasswordVisible);
        updateEyeIcon(toggleConfirmPasswordButton, isConfirmPasswordVisible);

        // Set initial visibility for text fields
        textNewPasswordField.setVisible(false);
        textNewPasswordField.setManaged(false);
        textConfirmPasswordField.setVisible(false);
        textConfirmPasswordField.setManaged(false);

        // Initialize strength meter with default style (finer lines)
        strengthSection1.setStyle("-fx-background-color: #ddd; -fx-pref-height: 3; -fx-pref-width: 50;");
        strengthSection2.setStyle("-fx-background-color: #ddd; -fx-pref-height: 3; -fx-pref-width: 50;");
        strengthSection3.setStyle("-fx-background-color: #ddd; -fx-pref-height: 3; -fx-pref-width: 50;");
        strengthSection4.setStyle("-fx-background-color: #ddd; -fx-pref-height: 3; -fx-pref-width: 50;");

        // Make sure label is positioned below the strength meter
        strengthLabel.setStyle("-fx-padding: 5 0 0 0;");
    }

    @FXML
    private void resetPassword() {
        String newPassword = isNewPasswordVisible ? textNewPasswordField.getText().trim() : newPasswordField.getText().trim();
        String confirmPassword = isConfirmPasswordVisible ? textConfirmPasswordField.getText().trim() : confirmPasswordField.getText().trim();

        // Clear previous error messages
        errorLabel.setText("");

        // Perform validations
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs !");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas !");
            return;
        }

        // Check if password is strong enough (at least medium strength)
        if (calculatePasswordStrength(newPassword) < 3) {
            showError("Le mot de passe est trop faible. Utilisez des lettres majuscules, minuscules, des chiffres et des caractères spéciaux.");
            return;
        }

        // All validations passed, update the password
        try {
            utilisateurService.updatePassword(userId, newPassword);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Mot de passe réinitialisé avec succès !");
            openLoginScene();  // Rediriger vers login après succès
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour du mot de passe !");
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleNewPasswordVisibility() {
        isNewPasswordVisible = !isNewPasswordVisible;
        if (isNewPasswordVisible) {
            textNewPasswordField.setText(newPasswordField.getText());
            textNewPasswordField.setVisible(true);
            textNewPasswordField.setManaged(true);
            newPasswordField.setVisible(false);
            newPasswordField.setManaged(false);
        } else {
            newPasswordField.setText(textNewPasswordField.getText());
            newPasswordField.setVisible(true);
            newPasswordField.setManaged(true);
            textNewPasswordField.setVisible(false);
            textNewPasswordField.setManaged(false);
        }
        updateEyeIcon(toggleNewPasswordButton, isNewPasswordVisible);
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        if (isConfirmPasswordVisible) {
            textConfirmPasswordField.setText(confirmPasswordField.getText());
            textConfirmPasswordField.setVisible(true);
            textConfirmPasswordField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
        } else {
            confirmPasswordField.setText(textConfirmPasswordField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            textConfirmPasswordField.setVisible(false);
            textConfirmPasswordField.setManaged(false);
        }
        updateEyeIcon(toggleConfirmPasswordButton, isConfirmPasswordVisible);
    }

    private void updateEyeIcon(Button button, boolean isVisible) {
        String iconPath = isVisible ? "/images/eye_open.png" : "/images/eye_closed.png";
        ImageView eyeIcon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        eyeIcon.setFitWidth(20);
        eyeIcon.setFitHeight(20);
        button.setGraphic(eyeIcon);
    }

    private void updatePasswordStrength(String password) {
        int strength = calculatePasswordStrength(password);

        // Reset all to default with finer height
        strengthSection1.setStyle("-fx-background-color: #ddd; -fx-pref-height: 3; -fx-pref-width: 50;");
        strengthSection2.setStyle("-fx-background-color: #ddd; -fx-pref-height: 3; -fx-pref-width: 50;");
        strengthSection3.setStyle("-fx-background-color: #ddd; -fx-pref-height: 3; -fx-pref-width: 50;");
        strengthSection4.setStyle("-fx-background-color: #ddd; -fx-pref-height: 3; -fx-pref-width: 50;");

        if (password.isEmpty()) {
            strengthLabel.setText("");
            return;
        }

        if (strength >= 1) {
            strengthSection1.setStyle("-fx-background-color: #FF4136; -fx-pref-height: 3; -fx-pref-width: 50;");
            strengthLabel.setText("Très faible!");
            strengthLabel.setStyle("-fx-text-fill: #FF4136; -fx-padding: 5 0 0 0;");
        }

        if (strength >= 2) {
            strengthSection2.setStyle("-fx-background-color: #FF851B; -fx-pref-height: 3; -fx-pref-width: 50;");
            strengthLabel.setText("Faible!");
            strengthLabel.setStyle("-fx-text-fill: #FF851B; -fx-padding: 5 0 0 0;");
        }

        if (strength >= 3) {
            strengthSection3.setStyle("-fx-background-color: #2ECC40; -fx-pref-height: 3; -fx-pref-width: 50;");
            strengthLabel.setText("Moyen!");
            strengthLabel.setStyle("-fx-text-fill: #2ECC40; -fx-padding: 5 0 0 0;");
        }

        if (strength >= 4) {
            strengthSection4.setStyle("-fx-background-color: #0074D9; -fx-pref-height: 3; -fx-pref-width: 50;");
            strengthLabel.setText("Très fort!");
            strengthLabel.setStyle("-fx-text-fill: #0074D9; -fx-padding: 5 0 0 0;");
        }
    }

    private int calculatePasswordStrength(String password) {
        // Return value is 0-4, where 0 is empty and 4 is very strong
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int strength = 0;

        // Length check
        if (password.length() >= 8) {
            strength += 1;
        }

        // Contains lowercase
        if (password.matches(".*[a-z].*")) {
            strength += 1;
        }

        // Contains uppercase and numbers
        if (password.matches(".*[A-Z].*") && password.matches(".*[0-9].*")) {
            strength += 1;
        }

        // Contains special characters
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            strength += 1;
        }

        return Math.min(4, strength);
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

    private void openLoginScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/loginUser.fxml"));
            Parent root = loader.load();

            // Récupérer la fenêtre actuelle
            Stage stage = (Stage) resetButton.getScene().getWindow();

            // Ajouter l'icône de l'application
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo1.png")));
            stage.setTitle("Login");

            // Configuration de l'écran en plein écran
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true); // Activer le mode plein écran

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