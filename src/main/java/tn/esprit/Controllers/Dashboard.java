package tn.esprit.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class Dashboard {

    @FXML
    private Button gestcong, gestpro, gestrec, gestutiliser, logout;
    @FXML
    private BorderPane mainContent;
    @FXML
    private MenuButton gestran;
    @FXML
    private MenuItem menuGererTransport, menuGererAbonnement, menuGererTrajet;

    @FXML
    public void initialize() {
        // Vérification que les éléments ne sont pas null avant d'ajouter les actions
        if (menuGererTransport != null)
            menuGererTransport.setOnAction(e -> chargerPage("/fxml/Affichage_transport.fxml"));

        if (menuGererAbonnement != null)
            menuGererAbonnement.setOnAction(e -> chargerPage("/fxml/Affichage_abonnement.fxml"));

        if (menuGererTrajet != null)
            menuGererTrajet.setOnAction(e -> chargerPage("/fxml/Affichage_trajet.fxml"));
    }

    private void chargerPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent newView = loader.load(); // Utilisation de Parent au lieu d'AnchorPane

            // Vérifier que mainContent n'est pas null
            if (mainContent != null) {
                Platform.runLater(() -> mainContent.setCenter(newView));
            } else {
                System.err.println("❌ Erreur : mainContent (BorderPane) est null ! Vérifiez votre FXML.");
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la page : " + fxmlFile);
            e.printStackTrace();
        }
    }
}
