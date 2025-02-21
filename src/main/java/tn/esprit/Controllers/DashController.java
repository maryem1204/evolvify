package tn.esprit.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class DashController {

    @FXML
    private Button btnDashboard,btnUser;
    @FXML
    private Button btnConges;
    @FXML
    private Button btnRecrutements;
    @FXML
    private Button btnProjets;
    @FXML
    private Button btnTransports;

    @FXML
    private ImageView userIcon;
    @FXML
    private Label username;

    @FXML
    private VBox sidebar;
    @FXML
    private AnchorPane contentArea;

    private List<Button> sidebarButtons;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            loadView("/fxml/dashboardAdminRH.fxml"); // Charger la vue par défaut au démarrage
        });

        // Charger l'image de l'icône utilisateur
        Image img = new Image(getClass().getResource("/images/profileicon.png").toExternalForm(), true);
        userIcon.setImage(img);

        // Gestion des boutons actifs
        sidebarButtons = List.of(btnDashboard, btnConges, btnRecrutements, btnProjets, btnTransports);
        for (Button button : sidebarButtons) {
            button.setOnAction(event -> {
                setActiveButton(button);
                loadView(getFxmlPath(button));
            });
        }

        // Afficher le nom de l'utilisateur connecté
        username.setText("Meriem Sassi");
    }

    private void setActiveButton(Button selectedButton) {
        for (Button button : sidebarButtons) {
            button.getStyleClass().remove("selected");
        }
        selectedButton.getStyleClass().add("selected");
    }

    // ✅ Méthode pour charger dynamiquement une vue dans contentArea
    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // ✅ Remplace le contenu de `contentArea`
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("⚠ Erreur de chargement du fichier FXML : " + fxmlFile);
        }
    }

    // ✅ Fonction qui retourne le bon fichier FXML en fonction du bouton
    private String getFxmlPath(Button button) {
        if (button == btnDashboard) {
            return "/fxml/dashboardAdminRH.fxml";
        } else if (button == btnUser) {
            return "/fxml/listUsers.fxml";
        }else if (button == btnConges) {
            return "/fxml/conges.fxml";
        } else if (button == btnRecrutements) {
            return "/fxml/recrutements.fxml";
        } else if (button == btnProjets) {
            return "/fxml/projets.fxml";
        } else if (button == btnTransports) {
            return "/fxml/transports.fxml";
        }
        return null;
    }

    // ✅ Méthodes pour le chargement manuel (si besoin)
    @FXML
    private void handleDashboard() {
        loadView("/fxml/dashboardAdminRH.fxml");
    }

    @FXML
    private void handleGestionUtilisateur() {
        loadView("/fxml/listUsers.fxml");
    }
}
