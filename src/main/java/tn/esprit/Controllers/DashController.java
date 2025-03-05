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

        sidebarButtons = List.of(btnDashboard, btnUser, btnRecrutements, btnProjets, btnTransports, btnConges,
                btnAbsences, btnCandidats, btnEntretiens, btnTrajet, btnAb, btnTransport);

        sidebarButtons.forEach(button -> button.setOnAction(event -> {
            setActiveButton(button);
            loadView(getFxmlPath(button));
        }));
        sidebarButtons = new ArrayList<>(List.of(btnDashboard, btnUser, btnRecrutements, btnProjets, btnTransports, btnConges, btnAbsences));
        sidebarButtons.add(menuGererTransport);
        sidebarButtons.add(menuGererAbonnement);
        sidebarButtons.add(menuGererTrajet);
        for (Button button : sidebarButtons) {
            button.setOnAction(event -> {
                setActiveButton(button);
                loadView(getFxmlPath(button));
            });
        }

        btnGestionConges.setOnAction(event -> toggleSubMenu(subMenuConges, arrowIconConges, isSubMenuCongesVisible));
        btnRecrutements.setOnAction(event -> toggleSubMenu(subMenuRecrutements, arrowIconRecrutements, isSubMenuRecrutementsVisible));
        btnProjets.setOnAction(event -> toggleSubMenu(subMenuProjets, arrowIconProjets, isSubMenuProjetsVisible));
        btnTransports.setOnAction(event -> toggleSubMenu(subMenuTransport, arrowIconTransports, isSubMenuTransportVisible));

        username.setOnMouseClicked(event -> handleProfil());
        userIcon.setOnMouseClicked(event -> handleProfil());
        logoutIcon.setOnMouseClicked(event -> handleLogout());
        // Ajout des événements de sous-menus
        btnGestionConges.setOnAction(event -> toggleSubMenuConges());
        btnRecrutements.setOnAction(event -> toggleSubMenuRecrutements());
        btnProjets.setOnAction(event -> toggleSubMenuProjets());
        btnTransports.setOnAction(event -> toggleSubMenuTransports());
        // Actions spécifiques pour les boutons qui se trouvent dans les sous-menus
        menuGererTransport.setOnAction(event -> {
            setActiveButton(menuGererTransport);
            loadView("/fxml/Affichage_transport.fxml");
        });
        menuGererAbonnement.setOnAction(event -> {
            setActiveButton(menuGererAbonnement);
            loadView("/fxml/Affichage_abonnement.fxml");//FrontAbonnement.fxml matensehomch
        });
        menuGererTrajet.setOnAction(event -> {
            setActiveButton(menuGererTrajet);
            loadView("/fxml/Affichage_trajet.fxml");//FrontTransport.fxml
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

        //username.setText("Meriem Sassi");
        hideAllSubMenus();
        loadUserProfileImage();
        setupUsernameLabel();
    }

    private void loadUserProfileImage() {
        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
        if (utilisateur == null) return;

        String imagePath = utilisateur.getProfilePhoto();
        Image profileImage = (imagePath != null && !imagePath.isEmpty()) ? new Image(new File(imagePath).toURI().toString())
                : new Image(getClass().getResource("/images/profile.png").toExternalForm());

        // Ensure the username label resizes to fit content
        username.setMaxWidth(Double.MAX_VALUE);
        username.setWrapText(true);


        // Vérifier si un utilisateur est bien en session
        if (utilisateur != null) {
            // Afficher son prénom et nom dans le Label
            username.setText(utilisateur.getFirstname() + " " + utilisateur.getLastname());
        } else {
            // Si aucun utilisateur, afficher un message par défaut
            username.setText("Utilisateur non connecté");
        }

        Platform.runLater(() -> {
            userIcon.setImage(profileImage);
            Circle clip = new Circle(25, 25, 25);
            userIcon.setClip(clip);
        });
    }


    /**
     * Définit le bouton actif (change la couleur de fond)
     */
    private void setActiveButton(Button selectedButton) {
        sidebarButtons.forEach(button -> button.getStyleClass().remove("selected"));
        selectedButton.getStyleClass().add("selected");
    }

    /**
     * Charge dynamiquement une vue dans le `contentArea`
     */
    private void loadView(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("⚠ Erreur de chargement : " + fxmlFile);
        }
    }

    /**
     * Récupère le bon FXML en fonction du bouton
     */
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

    /**
     * Animation pour basculer un sous-menu
     */
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

    /**
     * Basculer le sous-menu Gestion Congés
     */
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

    /**
     * Cacher tous les sous-menus au démarrage
     */
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
    private void showCandidats() {
        loadView("/fxml/Listcondidate.fxml");
    }

    @FXML
    private void showOffres() {
        loadView("/fxml/Listoffre.fxml");
    }

    @FXML
    private void showListOffresCandidates() {
        loadView("/fxml/listoffreCandidates.fxml");
    }


    @FXML
    private void handleProfil() {
        loadView("/fxml/employeeProfile.fxml");
    }
}
