package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class Dashboard {

    @FXML
    private Button gestcong, gestpro, gestran, gestrec, gestutiliser, logout;

    @FXML
    private AnchorPane gests; // Conteneur où la vue sera chargée

    @FXML
    private AnchorPane contentPane; // Zone où les vues seront chargées

    @FXML
    private void initialize() {
        loadListUsersView(); // Charge la liste des utilisateurs au démarrage
    }

    private void loadListUsersView() {
        loadView("/fxml/listUsers.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Vérifie si contentPane est bien défini avant de modifier son contenu
            if (contentPane != null) {
                contentPane.getChildren().setAll(view); // Affichage dynamique
            } else {
                System.out.println("❌ Erreur : contentPane est null !");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors du chargement de " + fxmlPath);
        }
    }

    @FXML
    private void goToGestionUtilisateur(ActionEvent event) {
        loadView("/fxml/listUsers.fxml");
    }

}
