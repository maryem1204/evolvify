package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Tests.MainFXLogin;

import java.io.IOException;
import java.util.Random;

public class ForgotPasswordController {
    @FXML
    private TextField emailField, codeField;
    @FXML
    private Button submitButton, verifyCodeButton;
    @FXML
    private Label errorLabel, codeLabel;
    @FXML
    private VBox codeSection;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private int generatedCode;
    private int userId;

    @FXML
    public void initialize() {
        codeSection.setVisible(false);  // Masquer la section du code au démarrage
    }

    @FXML
    private void sendVerificationCode() {
        String email = emailField.getText().trim();
        errorLabel.setText("");

        if (email.isEmpty()) {
            showError("Veuillez entrer votre email !");
            return;
        }

        Utilisateur user = utilisateurService.findByEmail(email);
        if (user == null) {
            showError("Aucun compte trouvé avec cet email !");
            return;
        }

        userId = user.getId_employe();
        generatedCode = new Random().nextInt(90000000) + 10000000;
        utilisateurService.sendConfirmationCode(email, generatedCode);

        // Afficher la section du code
        codeSection.setVisible(true);
    }

    @FXML
    private void verifyCode() {
        try {
            int enteredCode = Integer.parseInt(codeField.getText().trim());
            if (enteredCode == generatedCode) {
                openResetPasswordWindow();
            } else {
                showError("Code incorrect, vérifiez votre email !");
            }
        } catch (NumberFormatException e) {
            showError("Veuillez entrer un code valide !");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    @FXML
    private void openResetPasswordWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/resetPwd.fxml"));
            Parent root = loader.load();

            ResetPasswordController controller = loader.getController();
            controller.setUserId(userId);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) verifyCodeButton.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture de la page de réinitialisation !");
            e.printStackTrace();
        }
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
            System.out.println("ErreurImpossible de charger la page : " + fxmlFile);
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        switchScene(event, "/fxml/loginUser.fxml");
    }
}
