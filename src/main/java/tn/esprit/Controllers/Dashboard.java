package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

public class Dashboard {

    @FXML
    private Button gestcong;

    @FXML
    private Button gestpro;

    @FXML
    private Button gestran;

    @FXML
    private Button gestrec;

    @FXML
    private AnchorPane gests;

    @FXML
    private Button gestutiliser;

    @FXML
    private AnchorPane logo;

    @FXML
    private Button logout;

    @FXML
    private AnchorPane tableblanche;

    @FXML
    private Label logoutlabel;

    // ✅ Méthode pour charger dynamiquement une vue dans `tableblanche`
    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();

            tableblanche.getChildren().clear();
            tableblanche.getChildren().add(view);

            // ✅ Ajuster l'affichage pour qu'il occupe tout l'espace disponible
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ Charger la vue par défaut au démarrage
    @FXML
    private void initialize() {
        loadView("/fxml/listUsers.fxml"); // Charge "Gestion Utilisateur" par défaut
    }

    // ✅ Gestion du clic sur "Gestion Utilisateur"
    @FXML
    private void handleGestionUtilisateur(ActionEvent event) {
        loadView("/fxml/listUsers.fxml");
    }


    @FXML
    private void handleGestionConge(ActionEvent event) {
        loadView("/fxml/dashboardCongeRh.fxml");
    }
}
