package tn.esprit.Controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Entities.Gender;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

public class EditProfileController implements Initializable {

    @FXML private ImageView profileImageView;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField roleField;
    @FXML private TextField joiningDateField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField phoneNumberField;
    @FXML private DatePicker birthdayDatePicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button choosePhotoButton;

    private UtilisateurService userService;
    private Utilisateur currentUser;
    private File selectedProfilePhoto;
    private boolean birthdayEditable;
    private Runnable onSaveCallback; // Add callback for refreshing main view

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userService = new UtilisateurService(); // Initialize your service

        // Setup gender combo box - assuming Gender is an enum with HOMME, FEMME values
        genderComboBox.getItems().addAll("HOMME", "FEMME");

        // Disable non-editable fields
        firstNameField.setEditable(false);
        lastNameField.setEditable(false);
        emailField.setEditable(false);
        roleField.setEditable(false);
        joiningDateField.setEditable(false);
    }

    public void setUser(Utilisateur user) {
        this.currentUser = user;

        // Populate fields with user data
        firstNameField.setText(user.getFirstname());
        lastNameField.setText(user.getLastname());
        emailField.setText(user.getEmail());
        roleField.setText(user.getRole() != null ? user.getRole().toString() : "");

        if (user.getJoiningDate() != null) {
            joiningDateField.setText(user.getJoiningDate().toString());
        }

        genderComboBox.setValue(user.getGender() != null ? user.getGender().name() : "HOMME");
        phoneNumberField.setText(user.getNum_tel());

        if (user.getBirthdayDate() != null) {
            // Convert java.util.Date to LocalDate for DatePicker
            LocalDate localDate = Instant.ofEpochMilli(user.getBirthdayDate().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            birthdayDatePicker.setValue(localDate);
        }

        // Check if birthday has been edited before
        try {
            birthdayEditable = !userService.hasBirthDateBeenEdited(user.getId_employe());
            birthdayDatePicker.setDisable(!birthdayEditable);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not check birthday edit status: " + e.getMessage());
            birthdayDatePicker.setDisable(true);  // Disable to be safe
        }

        // Load profile image if available
        loadProfileImage(user.getProfilePhoto());
    }

    // Method to load profile image with error handling
    private void loadProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Add a cache-busting parameter to force refresh
                String cacheBreaker = imageUrl.contains("?") ? "&t=" : "?t=";
                String urlWithCacheBuster = imageUrl + cacheBreaker + System.currentTimeMillis();
                System.out.println("Loading image from: " + urlWithCacheBuster);

                Image image = new Image(urlWithCacheBuster);
                profileImageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Method to set callback for refreshing main view
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleChoosePhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) choosePhotoButton.getScene().getWindow();
        selectedProfilePhoto = fileChooser.showOpenDialog(stage);

        if (selectedProfilePhoto != null) {
            System.out.println("Selected photo: " + selectedProfilePhoto.getAbsolutePath());
            try {
                Image image = new Image(selectedProfilePhoto.toURI().toString());
                profileImageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading selected image: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load the selected image.");
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            System.out.println("Save button clicked");
            boolean profileUpdated = false;

            // Update profile photo if a new one was selected
            if (selectedProfilePhoto != null) {
                System.out.println("Uploading profile photo from: " + selectedProfilePhoto.getAbsolutePath());
                String photoUrl = userService.uploadProfilePhoto(currentUser.getId_employe(), selectedProfilePhoto);
                System.out.println("Photo URL after upload: " + photoUrl);
                currentUser.setProfilePhoto(photoUrl);
            } else {
                System.out.println("No new photo selected, keeping existing photo: " + currentUser.getProfilePhoto());
            }

            // Update user object with editable field values
            currentUser.setGender(Gender.valueOf(genderComboBox.getValue()));
            currentUser.setNum_tel(phoneNumberField.getText());

            // Update birthday if it's editable and has been changed
            if (birthdayEditable && birthdayDatePicker.getValue() != null) {
                // Convert LocalDate to java.util.Date
                LocalDate localDate = birthdayDatePicker.getValue();
                Date birthdayDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                currentUser.setBirthdayDate(new java.sql.Date(birthdayDate.getTime()));

                // Mark birthday as edited
                userService.setBirthDateEdited(currentUser.getId_employe(), true);
            }

            // Update profile in the database
            System.out.println("Updating user profile in database with photo URL: " + currentUser.getProfilePhoto());
            int result = userService.updateUserProfile(currentUser);
            System.out.println("Database update result: " + result);
            profileUpdated = (result > 0);

            if (profileUpdated) {
                // Execute the callback to refresh the main profile view
                if (onSaveCallback != null) {
                    System.out.println("Executing refresh callback");
                    onSaveCallback.run();
                } else {
                    System.out.println("No refresh callback set");
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
                closeDialog();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile. Please try again.");
            }
        } catch (SQLException | IOException e) {
            System.err.println("Exception during save: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Error updating profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}