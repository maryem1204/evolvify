package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.application.Platform;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Button gestcong, gestpro, gestran, gestrec, gestutiliser, logout;

    @FXML
    private AnchorPane tableblanche;

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Label logoutlabel;



    // ✅ Méthode pour charger dynamiquement une vue dans `mainContainer`
    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // ✅ Mise à jour correcte de l'interface
            if (mainContainer != null) {
                mainContainer.setCenter(root);
            } else {
                System.out.println("⚠ Erreur : mainContainer est null !");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur de chargement du fichier FXML : " + fxmlFile);
        }
    }

    // ✅ Charger la vue par défaut au démarrage
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            loadView("/fxml/dashboardAdminRH.fxml"); // Charge "Gestion Utilisateur" par défaut
        });
    }

    // ✅ Gestion du clic sur "Gestion Utilisateur"
    @FXML
    private void handleGestionUtilisateur(ActionEvent event) {
        loadView("/fxml/listUsers.fxml");
    }

    // ✅ Gestion du clic sur "Dashboard"
    @FXML
    public void handleDashboard(ActionEvent actionEvent) {
        loadView("/fxml/dashboardAdminRH.fxml");
    }
    @FXML
    public void handleGestionProjet(ActionEvent actionEvent) {
        loadView("/fxml/ListProjet.fxml");
    }
}
