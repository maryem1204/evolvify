package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Utils.SessionManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class EmployeeProfileController implements Initializable {

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label genderLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label birthDateLabel;

    @FXML
    private Label employeeIdLabel;

    @FXML
    private Label joiningDateLabel;

    @FXML
    private Label totalLeavesLabel;

    @FXML
    private Label remainingLeavesLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Circle profilePhotoCircle;

    @FXML
    private Button editProfileBtn;

    @FXML
    private Button downloadCvBtn;

    @FXML
    private Button requestLeaveBtn;

    @FXML
    private Button updateCvBtn;

    private Utilisateur currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Add drop shadow effect to profile photo
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10.0);
        dropShadow.setOffsetX(0.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        profilePhotoCircle.setEffect(dropShadow);

        // Initialize with current user data
        loadUserData();

        // Set up circular clip for profile image
        setupCircularProfileImage();

        // Configure button event handlers
        setupButtonActions();
    }

    private void loadUserData() {
        // Get current user from session (you'll need to implement SessionManager)
        currentUser = SessionManager.getUtilisateurConnecte();

        if (currentUser != null) {
            // Set user data to UI elements
            nameLabel.setText(currentUser.getFirstname() + " " + currentUser.getLastname());
            emailLabel.setText(currentUser.getEmail());
            roleLabel.setText(currentUser.getRole().toString());
            genderLabel.setText(currentUser.getGender().toString());
            phoneLabel.setText(currentUser.getNum_tel());

            // Format dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
            birthDateLabel.setText(dateFormat.format(currentUser.getBirthdayDate()));
            joiningDateLabel.setText(dateFormat.format(currentUser.getJoiningDate()));

            // Set leave information
            totalLeavesLabel.setText(String.valueOf(currentUser.getTtRestants()));
            remainingLeavesLabel.setText(String.valueOf(currentUser.getCongeRestant()));

            // Set employee ID
            employeeIdLabel.setText("EMP" + String.format("%05d", currentUser.getId_employe()));

            // Set profile image if available
            loadProfileImage();
        }
    }

    private void loadProfileImage() {
        if (currentUser.getProfilePhotoPath() != null && currentUser.getProfilePhoto().length > 0) {
            Image image = new Image(new ByteArrayInputStream(currentUser.getProfilePhoto()));
            profileImageView.setImage(image);
        } else if (currentUser.getProfilePhotoPath() != null && !currentUser.getProfilePhotoPath().isEmpty()) {
            try {
                Image image = new Image(currentUser.getProfilePhotoPath());
                profileImageView.setImage(image);
            } catch (Exception e) {
                // If image loading fails, set a default avatar based on gender
                setDefaultAvatar();
            }
        } else {
            // If no profile photo available, set default avatar
            setDefaultAvatar();
        }
    }

    private void setDefaultAvatar() {
        String defaultImagePath = currentUser.getGender().toString().equalsIgnoreCase("MALE") ?
                "/resources/images/male_avatar.png" : "/resources/images/female_avatar.png";

        try {
            Image defaultImage = new Image(getClass().getResourceAsStream(defaultImagePath));
            profileImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Failed to load default avatar: " + e.getMessage());
        }
    }

    private void setupCircularProfileImage() {
        // Create a circular clip for the profile image
        profileImageView.setClip(new Circle(70, 70, 70));

        // Ensure the image fits properly
        profileImageView.setPreserveRatio(false);
        profileImageView.setSmooth(true);
        profileImageView.setCache(true);
    }

    private void setupButtonActions() {
        // Edit Profile button action
        editProfileBtn.setOnAction(e -> handleEditProfile());

        // Download CV button action
        downloadCvBtn.setOnAction(e -> handleDownloadCV());

        // Request Leave button action
        requestLeaveBtn.setOnAction(e -> handleRequestLeave());

        // Update CV button action
        updateCvBtn.setOnAction(e -> handleUpdateCV());
    }

    private void handleEditProfile() {
        // Navigate to edit profile view
        try {
            // Implement navigation to edit profile page
            // MainController.getInstance().loadView("EditProfile.fxml");
            System.out.println("Navigate to Edit Profile");
        } catch (Exception e) {
            System.err.println("Error navigating to edit profile: " + e.getMessage());
        }
    }

    private void handleDownloadCV() {
        if (currentUser.getUploadedCv() == null || currentUser.getUploadedCv().length == 0) {
            // Show alert that no CV is available
            System.out.println("No CV available for download");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName(currentUser.getLastname() + "_" +
                currentUser.getFirstname() + "_CV.pdf");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            // Save CV to selected file
            // Implement file saving logic here
            System.out.println("Saving CV to: " + file.getAbsolutePath());
        }
    }

    private void handleRequestLeave() {
        // Navigate to request leave view
        try {
            // Implement navigation to leave request page
            // MainController.getInstance().loadView("RequestLeave.fxml");
            System.out.println("Navigate to Request Leave");
        } catch (Exception e) {
            System.err.println("Error navigating to request leave: " + e.getMessage());
        }
    }

    private void handleUpdateCV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            // Upload CV file
            // Implement CV upload logic here
            System.out.println("Selected CV: " + file.getAbsolutePath());
        }
    }
}