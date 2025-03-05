package tn.esprit.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.DeadLineNotification;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Utils.NotificationManager;
import tn.esprit.Utils.SessionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
    private Button btnTrajets;
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
    private HBox navbarHBox;

    @FXML
    private ImageView notificationIcon;
    @FXML
    private Label userNameLabel;
    // For the circular avatar placeholder
    @FXML
    private StackPane avatarStackPane; // Changed from userAvatarPane


    @FXML
    private Circle avatarCircle;      // Changed from userAvatar
    @FXML
    private Label avatarLabel;        // Changed from userInitials

    private Utilisateur currentUser;
    private final UtilisateurService utilisateurService = new UtilisateurService();

    public void initialize() {


        // Set active button styling for dashboard by default
        setActiveButton(btnProfil);

        // Load default dashboard view
        loadView("/fxml/employeeProfile.fxml");

        // Create user box programmatically
        createUserBox();

    }


    private void createUserBox() {
        System.out.println("🔧 Creating user box...");

        // Check if a user is logged in
        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
        if (utilisateur == null) {
            System.out.println("⚠ No user logged in!");
            return;
        }

        try {
            // Update user profile image
            updateNavbarProfileImage(utilisateur);

            // Update user name label
            userNameLabel.setText(utilisateur.getFirstname() + " " + utilisateur.getLastname());

            // Configure notification icon (optional interaction)
            configureNotificationIcon();

        } catch (Exception e) {
            System.err.println("Error creating user box: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configureNotificationIcon() {
        // Optional: Add interaction to notification icon
        notificationIcon.setOnMouseClicked(event -> {
            // Handle notification click
            System.out.println("Notification icon clicked");
            // Implement notification logic here
        });
    }
    private void setupPlaceholderAvatar(String initial) {
        if (avatarCircle != null && avatarLabel != null) {
            avatarCircle.setFill(Color.LIGHTBLUE);
            avatarCircle.setStroke(Color.WHITE);
            avatarCircle.setStrokeWidth(2);
            avatarLabel.setText(initial);
        } else {
            System.out.println("Warning: Avatar components are null. Make sure fx:id is set correctly in FXML.");
            System.out.println("Expected fx:id: avatarStackPane, avatarCircle, avatarLabel");
        }
    }

    public void saveProfileImage(File selectedFile) {
        try {
            // Get the current logged-in user
            Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
            if (utilisateur == null) {
                System.err.println("No user logged in!");
                return;
            }

            // Generate a unique filename
            String timestamp = String.valueOf(System.currentTimeMillis());
            String fileExtension = getFileExtension(selectedFile);
            String newFileName = utilisateur.getFirstname().toLowerCase() + "_" +
                    utilisateur.getLastname().toLowerCase() + "_" +
                    timestamp + "." + fileExtension;

            // Define destination directory (adjust path as needed)
            File uploadDir = new File("C:/xampp/htdocs/evolvify/");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Create destination file
            File destFile = new File(uploadDir, newFileName);

            // Copy the file
            try (FileInputStream fis = new FileInputStream(selectedFile);
                 FileOutputStream fos = new FileOutputStream(destFile)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }

            // Construct the URL path
            String newImagePath = "http://localhost/evolvify/" + newFileName;

            // Update user's profile photo
            utilisateur.setProfilePhoto(newImagePath);

            // Update the navbar profile image
            updateNavbarProfileImage(utilisateur);

            // Optional: Update user in database
            utilisateurService.update(utilisateur);

            System.out.println("Profile image saved successfully: " + newImagePath);

        } catch (Exception e) {
            System.err.println("Error saving profile image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to get file extension
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "png"; // default extension
        }
        return name.substring(lastIndexOf + 1);
    }

    @FXML
    private void handleProfileImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            saveProfileImage(selectedFile);
        }
    }

    private void updateNavbarProfileImage(Utilisateur utilisateur) {
        // Vérifier si un utilisateur est connecté
        if (utilisateur == null) {
            System.out.println("⚠ Aucun utilisateur connecté !");
            return;
        }

        // Récupérer le chemin de l'image de profil
        String newImagePath = utilisateur.getProfilePhoto(); // Assuming you have a method to get profile image path

        try {
            // Find the ImageView in the navbar
            Node profileImageNode = navbarHBox.lookup("#navbarProfileImage");

            if (profileImageNode instanceof ImageView) {
                ImageView profileImage = (ImageView) profileImageNode;

                Image newProfileImg;
                // Charger la nouvelle image avec les mêmes paramètres que dans createUserBox()
                if (newImagePath != null && !newImagePath.isEmpty()) {
                    if (newImagePath.startsWith("http://") || newImagePath.startsWith("https://")) {
                        newProfileImg = new Image(newImagePath, 40, 40, true, true);
                    } else {
                        File file = new File(newImagePath);
                        if (file.exists()) {
                            newProfileImg = new Image(file.toURI().toString(), 40, 40, true, true);
                        } else {
                            System.out.println("⚠ Fichier image introuvable : " + newImagePath);
                            newProfileImg = new Image(getClass().getResource("/images/profile.png").toExternalForm(), 40, 40, true, true);
                        }
                    }
                } else {
                    newProfileImg = new Image(getClass().getResource("/images/profile.png").toExternalForm(), 40, 40, true, true);
                }

                // Mettre à jour l'image
                Platform.runLater(() -> {
                    profileImage.setImage(newProfileImg);

                    // Recréer le clip circulaire si nécessaire
                    Circle clip = new Circle(20, 20, 20);
                    profileImage.setClip(clip);

                    // Forcer la mise à jour du layout
                    navbarHBox.requestLayout();
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour de l'image de profil : " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Rest of your methods remain the same
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
        loadView("/fxml/FrontAbonnement.fxml");
    }
    @FXML
    private void handleTrajets() throws IOException {
        setActiveButton(btnTrajets);
        loadView("/fxml/FrontTransport.fxml");
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

    @FXML
    private void handleLogout() {
        System.out.println("🔒 Déconnexion de l'utilisateur...");
        SessionManager.getInstance().logout();

        // Optionnel : Rediriger vers la page de connexion
        try {
            Parent loginView = FXMLLoader.load(getClass().getResource("/fxml/loginUser.fxml"));
            Scene loginScene = new Scene(loginView);
            Stage stage = (Stage) navbarHBox.getScene().getWindow();
            stage.setScene(loginScene);
            stage.show();
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

}