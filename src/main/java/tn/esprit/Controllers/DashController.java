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
    private Button btnConges, btnAbsences, btnGestionConges, btnCandidats, btnEntretiens, btnTrajet, btnAb, btnTransport, btnOffre;
    @FXML
    private ImageView userIcon, arrowIconConges, arrowIconRecrutements, arrowIconProjets, arrowIconTransports, logoImage, logoutIcon;
    @FXML
    private Label username;
    @FXML
    private VBox sidebar, subMenuConges, subMenuRecrutements, subMenuProjets, subMenuTransport;
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

    // Singleton instance for external access
    private static DashController instance;

    @FXML
    public void initialize() {
        // Set the singleton instance
        instance = this;

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
        addButtonIfNotNull(tempButtons, btnOffre);

        sidebarButtons = tempButtons;

        // Set up the button click actions
        setupButtonActions();

        // Rest of your initialization remains the same
        hideAllSubMenus();
        loadUserProfileImage();
        setupUsernameLabel();

        if (username != null) {
            username.setOnMouseClicked(event -> {
                handleProfil();
            });
        }

        if (logoutIcon != null) {
            logoutIcon.setOnMouseClicked(event -> {
                handleLogout();
            });
        }

        System.out.println("DashController initialized successfully");
    }

    // Method to get the singleton instance
    public static DashController getInstance() {
        return instance;
    }

    private void setupButtonActions() {
        // For buttons that should follow the standard pattern
        sidebarButtons.forEach(button -> {
            if (button != null) {
                button.setOnAction(event -> {
                    setActiveButton(button);
                    String path = getFxmlPath(button);
                    if (path != null) {
                        loadView(path);
                    }
                });
            }
        });

        // Submenu toggle actions
        if (btnGestionConges != null) {
            btnGestionConges.setOnAction(event -> toggleSubMenuConges());
        }
        if (btnRecrutements != null) {
            btnRecrutements.setOnAction(event -> toggleSubMenuRecrutements());
        }
        if (btnProjets != null) {
            btnProjets.setOnAction(event -> {
                // First make it active
                setActiveButton(btnProjets);
                // Then toggle submenu if needed
                toggleSubMenuProjets();
                // Then load the appropriate view
                loadView("/fxml/ListProjet.fxml");
            });
        }
        if (btnTransports != null) {
            btnTransports.setOnAction(event -> toggleSubMenuTransports());
        }

        // Other specific button actions with special handling
        if (menuGererTransport != null) {
            menuGererTransport.setOnAction(event -> {
                setActiveButton(menuGererTransport);
                loadView("/fxml/Affichage_transport.fxml");
            });
        }
        if (menuGererAbonnement != null) {
            menuGererAbonnement.setOnAction(event -> {
                setActiveButton(menuGererAbonnement);
                loadView("/fxml/Affichage_abonnement.fxml");
            });
        }
        if (menuGererTrajet != null) {
            menuGererTrajet.setOnAction(event -> {
                setActiveButton(menuGererTrajet);
                loadView("/fxml/Affichage_trajet.fxml");
            });
        }

        if (btnConges != null) {
            btnConges.setOnAction(event -> {
                setActiveButton(btnConges);
                loadView("/fxml/dashboardCongeRh.fxml");
            });
        }

        if (btnAbsences != null) {
            btnAbsences.setOnAction(event -> {
                setActiveButton(btnAbsences);
                loadView("/fxml/AttendanceView.fxml");
            });
        }
        if (btnTaches != null) {
            btnTaches.setOnAction(event -> {
                setActiveButton(btnTaches);
                loadView("/fxml/ListProjet.fxml");
            });
        }

        if (btnOffre != null) {
            btnOffre.setOnAction(event -> {
                setActiveButton(btnOffre);
                loadView("/fxml/ListOffre.fxml");
            });
        }
    }

    private void addButtonIfNotNull(List<Button> list, Button button) {
        if (button != null) {
            list.add(button);
        }
    }

    private void loadUserProfileImage() {
        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();

        if (utilisateur == null || userIcon == null) {
            if (username != null) {
                username.setText("Utilisateur non connecté");
            }
            setDefaultProfileImage();
            return;
        }

        // Set username
        if (username != null) {
            username.setText(utilisateur.getFirstname() + " " + utilisateur.getLastname());
            username.setMaxWidth(Double.MAX_VALUE);
            username.setWrapText(true);
        }

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
                } else if (userIcon != null) {
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
        if (userIcon == null) return;

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
                if (btn != null) {
                    btn.getStyleClass().remove("active-button");
                }
            });

            if (activeButton != null) {
                activeButton.getStyleClass().add("active-button");
            }
        }
    }

    private void loadView(String fxmlFile) {
        if (fxmlFile == null || contentArea == null) {
            System.err.println("Cannot load view: fxmlFile=" + fxmlFile + ", contentArea=" + contentArea);
            return;
        }

        try {
            System.out.println("Loading view: " + fxmlFile);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // If this is a project or task view, we might need to pass the dashboard controller
            Object controller = loader.getController();
            if (controller != null) {
                // Use reflection to check if the controller has setDashboardController method
                try {
                    java.lang.reflect.Method setDashMethod = controller.getClass().getMethod("setDashboardController", DashController.class);
                    if (setDashMethod != null) {
                        setDashMethod.invoke(controller, this);
                        System.out.println("Set dashboard controller in " + controller.getClass().getSimpleName());
                    }
                } catch (Exception e) {
                    // Method doesn't exist, which is fine
                }
            }

            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlFile);
            e.printStackTrace();
        }
    }

    public void setContent(Parent content) {
        if (contentArea != null && content != null) {
            Platform.runLater(() -> {
                try {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(content);
                    System.out.println("Content set successfully in dashboard");
                } catch (Exception e) {
                    System.err.println("Error setting content: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            System.err.println("Cannot set content: contentArea=" + contentArea + ", content=" + content);
        }
    }

    private String getFxmlPath(Button button) {
        if (button == null) return null;

        String buttonId = button.getId();
        if (buttonId == null) return null;

        return switch (buttonId) {
            case "btnDashboard" -> "/fxml/dashboardAdminRH.fxml";
            case "btnUser" -> "/fxml/listUsers.fxml";
            case "btnConges" -> "/fxml/dashboardCongeRh.fxml";
            case "btnAbsences" -> "/fxml/AttendanceView.fxml";
            case "btnTaches" -> "/fxml/ListProjet.fxml";
            case "btnEquipe" -> "/fxml/ListTacheRH.fxml";
            case "btnCandidats" -> "/fxml/ListCondidate.fxml";
            case "btnEntretiens" -> "/fxml/ListOffreCandidates.fxml";
            case "btnOffre" -> "/fxml/ListOffre.fxml";
            case "btnProjets" -> "/fxml/ListProjet.fxml";
            case "btnTransport" -> "/fxml/Affichage_transport.fxml";
            case "btnTrajet" -> "/fxml/Affichage_trajet.fxml";
            case "btnAb" -> "/fxml/Affichage_abonnement.fxml";
            default -> null;
        };
    }

    private void toggleSubMenu(VBox subMenu, ImageView arrowIcon, boolean isVisible) {
        if (subMenu == null) return;

        boolean newState = !isVisible;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(subMenu.prefHeightProperty(), newState ? 80 : 0),
                        new KeyValue(subMenu.opacityProperty(), newState ? 1 : 0)));
        timeline.play();
        subMenu.setManaged(newState);
        subMenu.setVisible(newState);

        if (arrowIcon != null) {
            arrowIcon.setRotate(newState ? 90 : 0);
        }
    }

    @FXML
    private void toggleSubMenuConges() {
        toggleSubMenu(subMenuConges, arrowIconConges, isSubMenuCongesVisible);
        isSubMenuCongesVisible = !isSubMenuCongesVisible;
    }

    @FXML
    private void toggleSubMenuRecrutements() {
        toggleSubMenu(subMenuRecrutements, arrowIconRecrutements, isSubMenuRecrutementsVisible);
        isSubMenuRecrutementsVisible = !isSubMenuRecrutementsVisible;
    }

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
        if (subMenuConges != null) {
            subMenuConges.setVisible(false);
            subMenuConges.setManaged(false);
        }
        if (subMenuRecrutements != null) {
            subMenuRecrutements.setVisible(false);
            subMenuRecrutements.setManaged(false);
        }
        if (subMenuProjets != null) {
            subMenuProjets.setVisible(false);
            subMenuProjets.setManaged(false);
        }
        if (subMenuTransport != null) {
            subMenuTransport.setVisible(false);
            subMenuTransport.setManaged(false);
        }
    }

    private void setupUsernameLabel() {
        if (username == null) return;

        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
        username.setText(utilisateur != null ?
                utilisateur.getFirstname() + " " + utilisateur.getLastname() :
                "Utilisateur non connecté");
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
        setActiveButton(btnProjets);
        loadView("/fxml/ListProjet.fxml");
    }

    @FXML
    private void handleTache() {
        setActiveButton(btnTaches);
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

    @FXML
    private void showCandidats() {
        setActiveButton(btnCandidats);
        loadView("/fxml/ListCondidate.fxml");
    }

    @FXML
    private void showOffres() {
        setActiveButton(btnOffre);
        loadView("/fxml/ListOffre.fxml");
    }

    @FXML
    private void showListOffresCandidates() {
        setActiveButton(btnEntretiens);
        loadView("/fxml/ListOffreCandidates.fxml");
    }
}