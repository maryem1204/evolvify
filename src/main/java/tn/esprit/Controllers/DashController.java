package tn.esprit.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.io.IOException;
import java.util.List;

public class DashController {

    @FXML
    private Button btnDashboard, btnUser, btnRecrutements, btnProjets, btnTransports;
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

    private boolean isSubMenuCongesVisible = false;
    private boolean isSubMenuRecrutementsVisible = false;
    private boolean isSubMenuProjetsVisible = false;
    private boolean isSubMenuTransportVisible = false;

    private List<Button> sidebarButtons;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            loadView("/fxml/dashboardAdminRH.fxml");
            setActiveButton(btnDashboard); // Sélection par défaut
        });

        // Chargement de l'icône utilisateur
        userIcon.setImage(new Image(getClass().getResource("/images/profileicon.png").toExternalForm(), true));

        // Liste de tous les boutons du sidebar
        sidebarButtons = List.of(btnDashboard, btnUser, btnRecrutements, btnProjets, btnTransports, btnConges, btnAbsences);

        // Gestion des actions des boutons principaux
        for (Button button : sidebarButtons) {
            button.setOnAction(event -> {
                setActiveButton(button);
                loadView(getFxmlPath(button));
            });
        }

        // Gestion des sous-menus
        btnGestionConges.setOnAction(event -> toggleSubMenuConges());
        btnRecrutements.setOnAction(event -> toggleSubMenuRecrutements());
        btnProjets.setOnAction(event -> toggleSubMenuProjets());
        btnTransports.setOnAction(event -> toggleSubMenuTransports());

        btnConges.setOnAction(event -> {
            setActiveButton(btnConges);
            loadView("/fxml/conges.fxml");
        });

        btnAbsences.setOnAction(event -> {
            setActiveButton(btnAbsences);
            loadView("/fxml/absences.fxml");
        });

        // Afficher le nom de l'utilisateur connecté
        username.setText("Meriem Sassi");

        // Cacher les sous-menus au démarrage
        hideAllSubMenus();
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
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("⚠ Erreur de chargement : " + fxmlFile);
        }
    }

    /**
     * Récupère le bon FXML en fonction du bouton
     */
    private String getFxmlPath(Button button) {
        if (button == btnDashboard) return "/fxml/dashboardAdminRH.fxml";
        if (button == btnUser) return "/fxml/listUsers.fxml";
        if (button == btnRecrutements) return "/fxml/recrutements.fxml";
        if (button == btnProjets) return "/fxml/projets.fxml";
        if (button == btnTransports) return "/fxml/transports.fxml";
        if (button == btnConges) return "/fxml/conges.fxml";
        if (button == btnAbsences) return "/fxml/absences.fxml";
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
    public void handleGestionProjet(ActionEvent actionEvent) {
        loadView("/fxml/ListProjet.fxml");
    }
    @FXML
    public void handleTache(ActionEvent actionEvent) {
        loadView("/fxml/ListTacheRH.fxml");
    }
}
