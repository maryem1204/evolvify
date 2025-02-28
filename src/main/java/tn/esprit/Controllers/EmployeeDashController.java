package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class EmployeeDashController {
    @FXML
    private Label username;
    @FXML
    private Button logoutBtn;

    @FXML
    private Button btnProjets;
    @FXML
    private Button btnConge;
    @FXML
    private Button btnAbsence;
    @FXML
    private Button btnAbonnements;
    @FXML
    private Button btnOffres;
    @FXML
    private Button btnProfil;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private ScrollPane scrollSidebar;
    @FXML
    private VBox sidebar;
    @FXML
    private HBox userBox;

    // For the circular avatar placeholder
    @FXML
    private StackPane userAvatarPane;
    @FXML
    private Circle userAvatar;
    @FXML
    private Label userInitials;

    private Utilisateur currentUser;
    private final UtilisateurService utilisateurService = new UtilisateurService();

    public void initialize() {
        // Set active button styling for dashboard by default
        setActiveButton(btnProfil);

        // Load default dashboard view
        loadView("/fxml/employeeProfile.fxml");

        // Set up logout functionality
        logoutBtn.setOnAction(event -> handleLogout());

        // Verify CSS is loaded
        System.out.println("CSS file paths attempts:");
        System.out.println("1: " + getClass().getResource("/Styles/styledash.css"));
        System.out.println("2: " + getClass().getResource("../Styles/styledash.css"));
        System.out.println("3: " + getClass().getResource("/tn/esprit/Styles/styledash.css"));
        System.out.println("4: " + getClass().getResource("../../Styles/styledash.css"));

        // Initialize the username with a default value
        username.setText("Utilisateur");

        // Set up the avatar placeholder
        setupPlaceholderAvatar("U");
    }

    private void setupPlaceholderAvatar(String initial) {
        if (userAvatar != null && userInitials != null) {
            userAvatar.setFill(Color.LIGHTBLUE);
            userAvatar.setStroke(Color.WHITE);
            userAvatar.setStrokeWidth(2);
            userInitials.setText(initial);
        } else {
            System.out.println("Warning: Avatar components are null");
        }
    }

    public void setUserData(Utilisateur user) {
        this.currentUser = user;

        if (user != null) {
            username.setText(user.getFirstname() + " " + user.getLastname());

            // Set the initial for the avatar
            String initial = "";
            if (user.getFirstname() != null && !user.getFirstname().isEmpty()) {
                initial = user.getFirstname().substring(0, 1).toUpperCase();
            }
            setupPlaceholderAvatar(initial);

            // Try to load actual profile image if it exists
            tryLoadProfileImage(user);
        }
    }

    private void tryLoadProfileImage(Utilisateur user) {
        if (user.getProfilePhotoPath() == null || user.getProfilePhotoPath().isEmpty()) {
            return; // Keep using the placeholder
        }

        try {
            // Try loading from file path first
            File file = new File(user.getProfilePhotoPath());
            if (file.exists()) {
                // If we have an actual photo, we could replace the circle placeholder
                // with the actual photo, but for now we'll just keep the placeholder
                System.out.println("Found profile image at: " + file.getAbsolutePath());
                // Optionally: userAvatar.setFill(new ImagePattern(new Image(file.toURI().toString())));
            } else {
                System.out.println("Profile image file not found: " + user.getProfilePhotoPath());
            }
        } catch (Exception e) {
            System.out.println("Error loading profile image: " + e.getMessage());
        }
    }

    @FXML
    private void handleProjets() throws IOException {
        setActiveButton(btnProjets);
        //loadView("EmployeeProjects.fxml");
    }

    @FXML
    private void handleConge() throws IOException {
        setActiveButton(btnConge);
        //loadView("EmployeeLeaves.fxml");
    }

    @FXML
    private void handleAbsence() throws IOException {
        setActiveButton(btnAbsence);
        //loadView("EmployeeAbsences.fxml");
    }

    @FXML
    private void handleAbonnements() throws IOException {
        setActiveButton(btnAbonnements);
        //loadView("EmployeeSubscriptions.fxml");
    }

    @FXML
    private void handleOffres() throws IOException {
        setActiveButton(btnOffres);
        //loadView("EmployeeOffers.fxml");
    }

    @FXML
    private void handleProfil() throws IOException {
        setActiveButton(btnProfil);
        loadView("/fxml/employeeProfile.fxml");
    }


    @FXML
    private void handleProjet() throws IOException {
        setActiveButton(btnProjets);
        loadView("/fxml/ProjectListWithCards.fxml");
    }

    private void handleLogout() {
        try {
            // Clear any user session data

            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            // Replace current scene with login scene
            contentArea.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);

            // Ensure the view fills the content area
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("⚠ Error loading: " + fxmlFile);
        }
    }

    private void setActiveButton(Button button) {
        // Clear active state from all buttons
        for (Node node : sidebar.getChildren()) {
            if (node instanceof Button) {
                node.getStyleClass().remove("active-sidebar-button");
            }
        }

        // Set active state to the selected button
        button.getStyleClass().add("active-sidebar-button");
    }
    @FXML
    public void handleConge(ActionEvent actionEvent) {
        loadView("/fxml/CongeEmploye.fxml");
    }
    @FXML
    public void handleAbsence(ActionEvent actionEvent) {
        loadView("/fxml/EmployeAbsence.fxml");
    }
}