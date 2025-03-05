package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class EmployeeProfileController implements Initializable {

    @FXML
    private Label nameLabel, emailLabel, roleLabel, genderLabel, phoneLabel, birthDateLabel;
    @FXML
    private Label employeeIdLabel, joiningDateLabel, totalLeavesLabel, remainingLeavesLabel;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Circle profilePhotoCircle;
    @FXML
    private Button editProfileBtn, requestLeaveBtn;

    private Utilisateur currentUser;
    private final UtilisateurService userService = new UtilisateurService();

    private DashController dashController;
    public void setDashController(DashController dashController) {
        this.dashController = dashController;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10.0);
        dropShadow.setOffsetX(0.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        profilePhotoCircle.setEffect(dropShadow);

        loadUserData();
        setupCircularProfileImage();
        setupButtonActions();

    }

    private void loadUserData() {
        currentUser = SessionManager.getUtilisateurConnecte();

        if (currentUser != null) {
            nameLabel.setText(currentUser.getFirstname() + " " + currentUser.getLastname());
            emailLabel.setText(currentUser.getEmail());
            roleLabel.setText(currentUser.getRole().toString());
            genderLabel.setText(currentUser.getGender().toString());
            phoneLabel.setText(currentUser.getNum_tel());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            birthDateLabel.setText(dateFormat.format(currentUser.getBirthdayDate()));
            joiningDateLabel.setText(dateFormat.format(currentUser.getJoiningDate()));

            totalLeavesLabel.setText(String.valueOf(currentUser.getTtRestants()));
            remainingLeavesLabel.setText(String.valueOf(currentUser.getCongeRestant()));

            employeeIdLabel.setText("EMP" + String.format("%05d", currentUser.getId_employe()));

            loadProfileImage();
        }
    }

    private void loadProfileImage() {
        if (currentUser.getProfilePhotoPath() != null && !currentUser.getProfilePhoto().isEmpty()) {
            Image image = new Image(currentUser.getProfilePhoto());
            profileImageView.setImage(image);


        } else {
            setDefaultAvatar();
        }
    }

    private void setDefaultAvatar() {
        if (currentUser.getProfilePhoto() != null && currentUser.getProfilePhoto().isEmpty()) {
            String defaultImagePath = currentUser.getGender().toString().equalsIgnoreCase("HOMME") ?
                    "/images/male_avatar.png" : "/images/female_avatar.png";
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream(defaultImagePath));
                profileImageView.setImage(defaultImage);
            } catch (Exception e) {
                System.err.println("Failed to load default avatar: " + e.getMessage());
            }
        } else {
            try {
                Image defaultImage = new Image(currentUser.getProfilePhoto());
                profileImageView.setImage(defaultImage);
            } catch (Exception e) {
                System.err.println("Failed to load default avatar: " + e.getMessage());
            }
        }


    }

    private void setupCircularProfileImage() {
        profileImageView.setClip(new Circle(70, 70, 70));
        profileImageView.setPreserveRatio(false);
        profileImageView.setSmooth(true);
        profileImageView.setCache(true);
    }

    private void setupButtonActions() {
        editProfileBtn.setOnAction(e -> handleEditProfile(e));
        requestLeaveBtn.setOnAction(e -> handleRequestLeave());
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        try {
            int userId = getCurrentUserId();
            Utilisateur currentUser = userService.getUserProfile(userId);

            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not retrieve user profile.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditProfile.fxml"));
            Parent root = loader.load();
            EditProfileController controller = loader.getController();
            controller.setUser(currentUser);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Profile");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(root));

            dialogStage.showAndWait();

            // ✅ Rafraîchir les données après fermeture de la popup
            refreshUserProfile();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Edit Profile dialog: " + e.getMessage());
        }
    }

    private void refreshUserProfile() {
        // Reload user from database to get fresh data
        try {
            currentUser = userService.getUserById(currentUser.getId_employe());
            // Update UI components with the fresh data
            if (currentUser.getProfilePhoto() != null && !currentUser.getProfilePhoto().isEmpty()) {
                String urlWithCacheBuster = currentUser.getProfilePhoto() +
                        (currentUser.getProfilePhoto().contains("?") ? "&t=" : "?t=") +
                        System.currentTimeMillis();
                profileImageView.setImage(new Image(urlWithCacheBuster));
            }
            // Update other UI components as needed
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private int getCurrentUserId() {
        return SessionManager.getUtilisateurConnecte().getId_employe();
    }

    private void handleDownloadCV() {
        if (currentUser.getUploadedCv() == null || currentUser.getUploadedCv().length == 0) {
            System.out.println("No CV available for download");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(currentUser.getLastname() + "_" + currentUser.getFirstname() + "_CV.pdf");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            System.out.println("Saving CV to: " + file.getAbsolutePath());
        }
    }

    private void handleRequestLeave() {
        try {
            System.out.println("Navigate to Request Leave");
        } catch (Exception e) {
            System.err.println("Error navigating to request leave: " + e.getMessage());
        }
    }
}
