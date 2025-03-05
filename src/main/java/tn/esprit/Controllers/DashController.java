package tn.esprit.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Utils.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class DashController {

    @FXML
    private Button btnDashboard, btnUser, btnRecrutements, btnProjets, btnTransports,btnTaches,btnEquipe;
    @FXML
    private Button btnConges, btnAbsences, btnGestionConges;
    @FXML
    private ImageView userIcon, arrowIconConges, arrowIconRecrutements, arrowIconProjets,arrowIconTransports;
    @FXML
    private Label username;
    @FXML
    private VBox sidebar, subMenuConges, subMenuRecrutements, subMenuProjets,subMenuTransport;
    @FXML
    private AnchorPane contentArea;
    @FXML
    private Pane subMenuContainer;
    @FXML
    private ScrollPane scrollSidebar;

    @FXML
    private ImageView logoImage;
    @FXML
    private ImageView logoutIcon; // Liaison avec l'icône de déconnexion


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

        // Chargement des icônes
        userIcon.setImage(new Image(getClass().getResource("/images/profile.png").toExternalForm(), true));

        sidebarButtons = List.of(btnDashboard, btnUser, btnRecrutements, btnProjets, btnTransports, btnConges, btnAbsences);

        for (Button button : sidebarButtons) {
            button.setOnAction(event -> {
                setActiveButton(button);
                loadView(getFxmlPath(button));
            });
        }

        // Ajout des événements de sous-menus
        btnGestionConges.setOnAction(event -> toggleSubMenuConges());
        btnRecrutements.setOnAction(event -> toggleSubMenuRecrutements());
        btnProjets.setOnAction(event -> toggleSubMenuProjets());
        btnTransports.setOnAction(event -> toggleSubMenuTransports());

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

        scrollSidebar.setFitToHeight(true);
        scrollSidebar.setFitToWidth(true);
        sidebar.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // 🔹 ÉVÉNEMENT GLOBAL : Fermer les sous-menus si on clique en dehors
        contentArea.setOnMouseClicked(event -> closeAllSubMenus(event));

        scrollSidebar.setPrefWidth(330);
        sidebar.setPrefWidth(330);

        // Récupérer l'utilisateur connecté
        Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();

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

        // événement de clic à l'icône de déconnexion
        logoutIcon.setOnMouseClicked(event -> handleLogout());
        username.setOnMouseClicked(event -> {
            try {
                handleProfil();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        userIcon.setOnMouseClicked(event -> {
            try {
                handleProfil();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 🔹 Ferme tous les sous-menus si le clic est en dehors
     */
    private void closeAllSubMenus(javafx.scene.input.MouseEvent event) {
        if (isSubMenuCongesVisible && !subMenuConges.getBoundsInParent().contains(event.getX(), event.getY())) {
            toggleSubMenuConges();
        }
        if (isSubMenuRecrutementsVisible && !subMenuRecrutements.getBoundsInParent().contains(event.getX(), event.getY())) {
            toggleSubMenuRecrutements();
        }
        if (isSubMenuProjetsVisible && !subMenuProjets.getBoundsInParent().contains(event.getX(), event.getY())) {
            toggleSubMenuProjets();
        }
        if (isSubMenuTransportVisible && !subMenuTransport.getBoundsInParent().contains(event.getX(), event.getY())) {
            toggleSubMenuTransports();
        }
    }


    /**
     * Définit le bouton actif (change la couleur de fond)
     */
    private void setActiveButton(Button selectedButton) {
        for (Button button : sidebarButtons) {
            button.getStyleClass().remove("selected");
        }
        selectedButton.getStyleClass().add("selected");
    }

    /**
     * Charge dynamiquement une vue dans le `contentArea`
     */
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

    /**
     * Récupère le bon FXML en fonction du bouton
     */
    private String getFxmlPath(Button button) {
        if (button == btnDashboard) return "/fxml/dashboardAdminRH.fxml";
        if (button == btnUser) return "/fxml/listUsers.fxml";

        return null;
    }

    /**
     * Animation pour basculer un sous-menu
     */
    private void toggleSubMenu(VBox subMenu, ImageView arrowIcon, boolean isVisible) {
        boolean newState = !isVisible; // Inverser l'état actuel
        double targetHeight = newState ? 80 : 0; // Ajuste selon le nombre de boutons

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(subMenu.prefHeightProperty(), targetHeight),
                        new KeyValue(subMenu.opacityProperty(), newState ? 1 : 0)
                )
        );

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
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Confirmation");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        // Attendre la réponse de l'utilisateur
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Fermer la session
            SessionManager.getInstance().setUtilisateurConnecte(null);

            try {
                // Charger la scène de connexion
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/loginUser.fxml"));
                Parent root = loader.load();

                // Récupérer la scène actuelle et la remplacer par la scène de connexion
                Stage stage = (Stage) logoutIcon.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    private void handleProfil() throws IOException {
        loadView("/fxml/employeeProfile.fxml");
    }

    public void handleConge(ActionEvent actionEvent) {
        setActiveButton(btnConges);
        loadView("fxml/dashboardCongeRh.fxml");
    }

    public void handleAbsence(ActionEvent actionEvent) {
        setActiveButton(btnAbsences);
        loadView("fxml/AttendanceView.fxml");
    }
}