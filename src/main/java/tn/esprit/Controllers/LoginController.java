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
import java.io.IOException;

public class LoginController {
    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField textPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private Button toggleButton;

    private boolean isPasswordVisible = false;

    @FXML
    private void initialize() {
        if (passwordField == null || textPasswordField == null || toggleButton == null) {
            System.out.println("Erreur : un élément FXML est null. Vérifiez votre fichier loginUser.fxml.");
            return;
        }

        // Masquer le champ texte pour le mot de passe
        textPasswordField.setManaged(false);
        textPasswordField.setVisible(false);

        // Ajouter l'icône "œil" au bouton
        updateEyeIcon();

        // Action du bouton pour afficher/cacher le mot de passe
        toggleButton.setOnAction(e -> togglePasswordVisibility());
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
    private void handleForgotPassword(ActionEvent event) {
        System.out.println("Hyperlink cliqué !");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forgetPwd.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement du fichier FXML.");
        }
    }
}
