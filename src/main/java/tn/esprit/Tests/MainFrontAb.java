package tn.esprit.Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class MainFrontAb extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // Charger la vue initiale
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/front_abonnement.fxml"));
        Parent root = loader.load();

        // Créer une scène avec la vue chargée
        Scene scene = new Scene(root);

        // Ajouter le fichier CSS
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/tn/esprit/Styles/front.css")).toExternalForm()
        );

        // Configurer la scène et le titre
        stage.setScene(scene);
        stage.setTitle("Gestion des Abonnements");

        // Ajouter le menu et les actions
        MenuBar menuBar = new MenuBar();

        // Menu
        Menu menu = new Menu("Navigation");

        // Option pour afficher la liste des abonnements
        MenuItem afficherAbonnement = new MenuItem("Afficher les abonnements");
        afficherAbonnement.setOnAction(e -> loadView("//front_abonnement.fxml", stage));

        // Option pour ajouter un abonnement
        MenuItem ajouterAbonnement = new MenuItem("Ajouter un abonnement");
        ajouterAbonnement.setOnAction(e -> loadView("/add_abonnement.fxml", stage));

        menu.getItems().addAll(afficherAbonnement, ajouterAbonnement);
        menuBar.getMenus().add(menu);

        // Afficher le menu
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);
        borderPane.setCenter(root);

        // Ajouter la scène avec le menu
        Scene menuScene = new Scene(borderPane);
        stage.setScene(menuScene);

        // Afficher la scène
        stage.show();
    }

    // Méthode modifiée pour accepter les deux paramètres : un chemin de fichier et un stage
    void loadView(String fxmlFile, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();
            ((BorderPane) stage.getScene().getRoot()).setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

