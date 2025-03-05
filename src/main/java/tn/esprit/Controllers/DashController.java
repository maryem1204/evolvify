package tn.esprit.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.util.ArrayList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DashController {

    @FXML
    private Button btnDashboard, btnUser, btnRecrutements, btnProjets, btnTransports, btnTaches, btnEquipe;
    @FXML
    private Button btnConges, btnAbsences, btnGestionConges, btnCandidats, btnEntretiens, btnTrajet, btnAb, btnTransport;
    @FXML
    private ImageView userIcon, arrowIconConges, arrowIconRecrutements, arrowIconProjets, arrowIconTransports, logoImage, logoutIcon;
    @FXML
    private Label username;
    @FXML
    private VBox sidebar, subMenuConges, subMenuRecrutements, subMenuProjets, subMenuTransport;
    // Boutons du sous-menu Transports
    @FXML
    private Button menuGererTransport, menuGererAbonnement, menuGererTrajet;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private Pane subMenuContainer;
    @FXML
    private ScrollPane scrollSidebar;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private boolean isSubMenuCongesVisible = false;
    private boolean isSubMenuRecrutementsVisible = false;
    private boolean isSubMenuProjetsVisible = false;
    private boolean isSubMenuTransportVisible = false;

    private List<Button> sidebarButtons;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            loadView("/fxml/dashboardAdminRH.fxml");
            setActiveButton(btnDashboard);
        });

        // Null check before creating the list
        List<Button> tempButtons = new ArrayList<>();

        // Add buttons only if they are not null
        addButtonIfNotNull(tempButtons, btnDashboard);
        addButtonIfNotNull(tempButtons, btnUser);
        addButtonIfNotNull(tempButtons, btnRecrutements);
        addButtonIfNotNull(tempButtons, btnProjets);
        addButtonIfNotNull(tempButtons, btnTransports);
        addButtonIfNotNull(tempButtons, btnConges);
        addButtonIfNotNull(tempButtons, btnAbsences);
        addButtonIfNotNull(tempButtons, btnCandidats);
        addButtonIfNotNull(tempButtons, btnEntretiens);
        addButtonIfNotNull(tempButtons, btnTrajet);
        addButtonIfNotNull(tempButtons, btnAb);
        addButtonIfNotNull(tempButtons, btnTransport);
        addButtonIfNotNull(tempButtons, menuGererTransport);
        addButtonIfNotNull(tempButtons, menuGererAbonnement);
        addButtonIfNotNull(tempButtons, menuGererTrajet);
        addButtonIfNotNull(tempButtons, btnTaches);
        addButtonIfNotNull(tempButtons, btnEquipe);

        sidebarButtons = tempButtons;

        sidebarButtons.forEach(button -> button.setOnAction(event -> {
            setActiveButton(button);
            loadView(getFxmlPath(button));
        }));

        // Submenu toggle actions
        btnGestionConges.setOnAction(event -> toggleSubMenuConges());
        btnRecrutements.setOnAction(event -> toggleSubMenuRecrutements());
        btnProjets.setOnAction(event -> toggleSubMenuProjets());
        btnTransports.setOnAction(event -> toggleSubMenuTransports());

        // Other specific button actions
        menuGererTransport.setOnAction(event -> {
            setActiveButton(menuGererTransport);
            loadView("/fxml/Affichage_transport.fxml");
        });
        menuGererAbonnement.setOnAction(event -> {
            setActiveButton(menuGererAbonnement);
            loadView("/fxml/Affichage_abonnement.fxml");
        });
        menuGererTrajet.setOnAction(event -> {
            setActiveButton(menuGererTrajet);
            loadView("/fxml/Affichage_trajet.fxml");
        });

        btnConges.setOnAction(event -> {
            setActiveButton(btnConges);
            loadView("/fxml/dashboardCongeRh.fxml");
        });

        btnAbsences.setOnAction(event -> {
            setActiveButton(btnAbsences);
            loadView("/fxml/AttendanceView.fxml");
        });
        btnTaches.setOnAction(event -> {
            setActiveButton(btnProjets);
            loadView("/fxml/ListProjet.fxml");
        });
        btnEquipe.setOnAction(event -> {
            setActiveButton(btnProjets);
            loadView("/fxml/ListTacheRH.fxml");
        });

        // Rest of your initialization remains the same
        hideAllSubMenus();
        loadUserProfileImage();
        setupUsernameLabel();
        username.setOnMouseClicked(event -> {
            handleProfil();
        });
        logoutIcon.setOnMouseClicked(event -> {
            handleLogout();
        });
    }
    private void addButtonIfNotNull(List<Button> list, Button button) {
        if (button != null) {
            list.add(button);
        }
    }

    private void loadUserProfileImage() {
        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();

        if (utilisateur == null) {
            username.setText("Utilisateur non connecté");
            setDefaultProfileImage();
            return;
        }

        // Set username
        username.setText(utilisateur.getFirstname() + " " + utilisateur.getLastname());
        username.setMaxWidth(Double.MAX_VALUE);
        username.setWrapText(true);

        String imagePath = utilisateur.getProfilePhoto();

        if (imagePath == null || imagePath.isEmpty()) {
            setDefaultProfileImage();
            return;
        }

        Platform.runLater(() -> {
            try {
                Image profileImage;

                if (imagePath.startsWith("http")) {
                    // Load image from URL
                    profileImage = new Image(imagePath, true);
                } else {
                    // Load image from local file
                    File imageFile = new File(imagePath);
                    if (!imageFile.exists()) {
                        setDefaultProfileImage();
                        return;
                    }
                    profileImage = new Image(imageFile.toURI().toString());
                }

                // If loading the image failed, use default
                if (profileImage.isError()) {
                    setDefaultProfileImage();
                } else {
                    userIcon.setImage(profileImage);
                    userIcon.setClip(new Circle(userIcon.getFitWidth() / 2, userIcon.getFitHeight() / 2, userIcon.getFitWidth() / 2));
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
                setDefaultProfileImage();
            }
        });
    }


    private void setDefaultProfileImage() {
        Platform.runLater(() -> {
            try {
                Image defaultImage = new Image(getClass().getResource("/images/profile.png").toExternalForm());
                userIcon.setImage(defaultImage);
                Circle clip = new Circle(25, 25, 25);
                userIcon.setClip(clip);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setActiveButton(Button activeButton) {
        if (sidebarButtons != null) {
            sidebarButtons.forEach(btn -> {
                btn.getStyleClass().remove("active-button");
            });

            if (activeButton != null) {
                activeButton.getStyleClass().add("active-button");
            }
        }
    }

    private void loadView(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFxmlPath(Button button) {
        return switch (button.getId()) {
            case "btnDashboard" -> "/fxml/dashboardAdminRH.fxml";
            case "btnUser" -> "/fxml/listUsers.fxml";
            case "btnConges" -> "/fxml/conges.fxml";
            case "btnAbsences" -> "/fxml/absences.fxml";
            case "btnTaches" -> "/fxml/ListProjet.fxml";
            case "btnEquipe" -> "/fxml/ListTacheRH.fxml";
            case "btnCandidats" -> "/fxml/conges.fxml";
            case "btnEntretiens" -> "/fxml/absences.fxml";
            case "btnTransport", "btnTrajet", "btnAb" -> "/fxml/ListTacheRH.fxml";
            default -> null;
        };
    }

    private void toggleSubMenu(VBox subMenu, ImageView arrowIcon, boolean isVisible) {
        boolean newState = !isVisible;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(subMenu.prefHeightProperty(), newState ? 80 : 0),
                        new KeyValue(subMenu.opacityProperty(), newState ? 1 : 0)));
        timeline.play();
        subMenu.setManaged(newState);
        subMenu.setVisible(newState);
        arrowIcon.setRotate(newState ? 90 : 0);
    }

    @FXML
    private void toggleSubMenuConges() {
        toggleSubMenu(subMenuConges, arrowIconConges, isSubMenuCongesVisible);
        isSubMenuCongesVisible = !isSubMenuCongesVisible;
    }

    /**
     * Basculer le sous-menu Recrutements
     */
    @FXML
    private void toggleSubMenuRecrutements() {
        toggleSubMenu(subMenuRecrutements, arrowIconRecrutements, isSubMenuRecrutementsVisible);
        isSubMenuRecrutementsVisible = !isSubMenuRecrutementsVisible;
    }

    /**
     * Basculer le sous-menu Projets
     */
    @FXML
    private void toggleSubMenuProjets() {
        toggleSubMenu(subMenuProjets, arrowIconProjets, isSubMenuProjetsVisible);
        isSubMenuProjetsVisible = !isSubMenuProjetsVisible;
    }

    @FXML
    private void toggleSubMenuTransports() {
        toggleSubMenu(subMenuTransport, arrowIconTransports, isSubMenuTransportVisible);
        isSubMenuTransportVisible = !isSubMenuTransportVisible;
    }
    private void hideAllSubMenus() {
        subMenuConges.setVisible(false);
        subMenuConges.setManaged(false);
        subMenuRecrutements.setVisible(false);
        subMenuRecrutements.setManaged(false);
        subMenuProjets.setVisible(false);
        subMenuProjets.setManaged(false);
        subMenuTransport.setVisible(false);
    }

    private void setupUsernameLabel() {
        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
        username.setText(utilisateur != null ? utilisateur.getFirstname() + " " + utilisateur.getLastname() : "Utilisateur non connecté");
    }

    @FXML
    private void handleDashboard() {
        setActiveButton(btnDashboard);
        loadView("/fxml/dashboardAdminRH.fxml");
    }

    @FXML
    private void handleGestionUtilisateur() {
        setActiveButton(btnUser);
        loadView("/fxml/listUsers.fxml");
    }
    @FXML
    private void handleGestionProjet() {
        setActiveButton(btnDashboard);
        loadView("/fxml/ListProjet.fxml");
    }
    @FXML
    private void handleTache() {
        setActiveButton(btnDashboard);
        loadView("/fxml/ListTacheRH.fxml");
    }

    @FXML
    public void handleConge(ActionEvent actionEvent) {
        setActiveButton(btnConges);
        loadView("/fxml/dashboardCongeRh.fxml");
    }
    @FXML
    public void handleAbsence(ActionEvent actionEvent) {
        setActiveButton(btnAbsences);
        loadView("/fxml/AttendanceView.fxml");
    }

    @FXML
    public void handleAbonnement(ActionEvent actionEvent) {
        setActiveButton(menuGererAbonnement);
        loadView("/fxml/Affichage_abonnement.fxml");
    }
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment vous déconnecter ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            SessionManager.getInstance().setUtilisateurConnecte(null);
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/loginUser.fxml"));
                Stage stage = (Stage) logoutIcon.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Connexion");
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleProfil() {
        loadView("/fxml/employeeProfile.fxml");
    }
}
