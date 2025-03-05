package tn.esprit.Controllers;

import javafx.embed.swing.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Utils.SimpleCaptcha;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class ForgotPasswordController {
    @FXML
    private TextField phoneField, codeField, captchaTextField;
    @FXML
    private Button sendCodeButton, verifyCodeButton, refreshCaptchaButton, verifyCaptchaButton;
    @FXML
    private Label errorLabel, codeLabel;
    @FXML
    private VBox codeSection;
    @FXML
    private ImageView captchaImageView;
    @FXML
    private Hyperlink backToLogin;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private int generatedCode;
    private int userId;
    private boolean captchaVerified = false;
    private SimpleCaptcha captcha = new SimpleCaptcha();

    @FXML
    public void initialize() {
        codeSection.setVisible(false);  // Masquer la section du code au démarrage
        initializeCustomCaptcha();
        sendCodeButton.setDisable(true); // Désactiver le bouton jusqu'à ce que le CAPTCHA soit validé
        VBox.setVgrow(backToLogin, Priority.ALWAYS);

    }

    private void initializeCustomCaptcha() {
        // Convert BufferedImage to JavaFX Image
        BufferedImage bufferedImage = captcha.getImage();
        WritableImage image = SwingFXUtils.toFXImage(bufferedImage, null);

        // Display in ImageView
        captchaImageView.setImage(image);

        // Setup verification
        verifyCaptchaButton.setOnAction(event -> {
            String userAnswer = captchaTextField.getText();
            if (captcha.verify(userAnswer)) {
                // Captcha verified
                captchaVerified = true;
                sendCodeButton.setDisable(false);
                errorLabel.setText("CAPTCHA validé");
                errorLabel.setStyle("-fx-text-fill: green;");
            } else {
                // Captcha failed
                errorLabel.setText("CAPTCHA incorrect, veuillez réessayer");
                errorLabel.setStyle("-fx-text-fill: red;");
                refreshCaptcha();
            }
        });

        // Add refresh button functionality
        refreshCaptchaButton.setOnAction(event -> refreshCaptcha());
    }

    private void refreshCaptcha() {
        captcha.refresh();
        WritableImage newImage = SwingFXUtils.toFXImage(captcha.getImage(), null);
        captchaImageView.setImage(newImage);
        captchaTextField.clear();
        captchaVerified = false;
        sendCodeButton.setDisable(true);
    }

    @FXML
    private void sendVerificationCode() {
        String phoneNumber = phoneField.getText().trim();
        errorLabel.setText("");

        if (!captchaVerified) {
            showError("Veuillez valider le CAPTCHA d'abord !");
            return;
        }

        if (phoneNumber.isEmpty()) {
            showError("Veuillez entrer votre numéro de téléphone !");
            return;
        }

        Utilisateur user = utilisateurService.findByPhone(phoneNumber);
        if (user == null) {
            showError("Aucun compte trouvé avec ce numéro de téléphone !");
            return;
        }

        userId = user.getId_employe();
        generatedCode = new Random().nextInt(90000) + 10000; // Code à 5 chiffres pour SMS

        // Envoyer le SMS avec le code de vérification
        boolean smsSent = utilisateurService.sendSMSConfirmationCode(phoneNumber, generatedCode);

        if (smsSent) {
            // Afficher la section du code
            codeSection.setVisible(true);
            showSuccess("Un code de vérification a été envoyé à votre numéro de téléphone");
        } else {
            showError("Erreur lors de l'envoi du SMS. Veuillez réessayer plus tard.");
        }
    }

    @FXML
    private void verifyCode() {
        try {
            int enteredCode = Integer.parseInt(codeField.getText().trim());
            if (enteredCode == generatedCode) {
                openResetPasswordWindow();
            } else {
                showError("Code incorrect, vérifiez votre SMS !");
            }
        } catch (NumberFormatException e) {
            showError("Veuillez entrer un code valide !");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void openResetPasswordWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/resetPwd.fxml"));
            Parent root = loader.load();

            // Apply a small adjustment to center the form
            // Try these values and adjust as needed
            root.setTranslateX(-50); // Start with 0 and adjust if needed
            root.setTranslateY(-70); // Start with 0 and adjust if needed

            ResetPasswordController controller = loader.getController();
            controller.setUserId(userId);

            // Create a centered container
            StackPane container = new StackPane();
            container.setStyle("-fx-background-color: #111827;"); // Dark background
            container.getChildren().add(root);

            // These two lines ensure perfect centering
            StackPane.setAlignment(root, Pos.CENTER);
            container.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo1.png")));
            stage.setScene(new Scene(container));
            stage.setTitle("Changer mot de passe");

            // Configure full screen
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true);

            stage.show();

            // Close current window
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

            // Apply translation to center the login screen
            if (fxmlFile.equals("/fxml/loginUser.fxml")) {
                root.setTranslateY(-220); // Move up by 30 pixels
                root.setTranslateX(-380);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur: Impossible de charger la page : " + fxmlFile);
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        switchScene(event, "/fxml/loginUser.fxml");
    }
}