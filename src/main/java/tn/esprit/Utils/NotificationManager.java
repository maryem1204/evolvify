package tn.esprit.Utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import tn.esprit.Controllers.ResetPasswordController;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class NotificationManager {
    private static final UtilisateurService utilisateurService = new UtilisateurService();

    public static void checkFirstTimeLogin(HBox navbarHBox) {
        Utilisateur currentUser = SessionManager.getInstance().getUtilisateurConnecte();

        if (currentUser != null && isFirstTimeLogin(currentUser)) {
            Platform.runLater(() -> {
                showPasswordChangeNotification(navbarHBox, currentUser);
                markFirstLoginComplete(currentUser);
            });
        }
    }

    private static boolean isFirstTimeLogin(Utilisateur user) {
        // Implement your logic to check if it's the first login
        // For example, check a flag in the user object or database
        return user.isFirstLogin(); // Assuming you have this method in your Utilisateur class
    }

    private static void markFirstLoginComplete(Utilisateur user) {
        user.setFirstLogin(false);
        try {
            utilisateurService.updateFirstLogin(user, false); // Passer explicitement le statut false
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut de première connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void showPasswordChangeNotification(HBox navbarHBox, Utilisateur user) {
        Popup notificationPopup = new Popup();

        // Notification Container
        HBox notificationBox = new HBox(10);
        notificationBox.setStyle(
                "-fx-background-color: #F0F8FF;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #4682B4;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 10;"
        );
        notificationBox.setAlignment(Pos.CENTER_LEFT);

        // Notification Icon
        ImageView notificationIcon = new ImageView(new Image(
                NotificationManager.class.getResourceAsStream("/images/warning-icon.png")
        ));
        notificationIcon.setFitWidth(30);
        notificationIcon.setFitHeight(30);

        // Notification Message
        Label notificationLabel = new Label("For security, please change your password.");
        notificationLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        // Change Password Button
        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setStyle(
                "-fx-background-color: #4682B4;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 5;"
        );
        changePasswordBtn.setOnAction(e -> {
            // Open change password dialog or view
            openChangePasswordDialog(user);
            notificationPopup.hide();
        });

        // Dismiss Button
        Button dismissBtn = new Button("Dismiss");
        dismissBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #888;" +
                        "-fx-border-color: #888;" +
                        "-fx-border-radius: 5;"
        );
        dismissBtn.setOnAction(e -> notificationPopup.hide());

        // Add components to notification box
        notificationBox.getChildren().addAll(
                notificationIcon,
                notificationLabel,
                new Region(),
                changePasswordBtn,
                dismissBtn
        );
        HBox.setHgrow(new Region(), Priority.ALWAYS);

        // Setup popup
        notificationPopup.getContent().add(notificationBox);
        notificationPopup.setAutoHide(false);

        // Position the popup relative to the navbar
        notificationPopup.show(navbarHBox.getScene().getWindow(),
                navbarHBox.getScene().getWindow().getX() + navbarHBox.getScene().getWindow().getWidth() - 500,
                navbarHBox.getScene().getWindow().getY() + 80
        );
    }

    public static void openChangePasswordDialog(Utilisateur user) {
        try {
            // Load the reset password FXML
            FXMLLoader loader = new FXMLLoader(NotificationManager.class.getResource("/fxml/resetPwd.fxml"));
            Parent changePasswordView = loader.load();

            // If you need to pass the user to the reset password controller
            ResetPasswordController controller = loader.getController();
            controller.setUser(user);

            // Create a new stage for the change password dialog
            Stage changePasswordStage = new Stage();
            changePasswordStage.setTitle("Change Password");
            changePasswordStage.setScene(new Scene(changePasswordView));
            changePasswordStage.show();
        } catch (IOException e) {
            System.err.println("Error opening change password dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
