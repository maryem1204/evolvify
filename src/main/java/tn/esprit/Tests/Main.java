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
import java.util.Objects;

public class Main extends Application {

    private BorderPane rootLayout;

    @Override
    public void start(Stage stage) {
        try {
            // Charger le Dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            rootLayout = new BorderPane();
            rootLayout.setCenter(loader.load());

            // Ajouter la barre de menu
            rootLayout.setTop(createMenuBar());

            Scene scene = new Scene(rootLayout);

            // Ajouter le CSS
            String cssPath = "/tn/esprit/Styles/color.css";
            if (getClass().getResource(cssPath) != null) {
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
            } else {
                System.err.println("❌ Fichier CSS introuvable : " + cssPath);
            }

            // Configurer et afficher la scène
            stage.setScene(scene);
            stage.setTitle("Gestion des Transports et Abonnements");
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement du Dashboard !");
            e.printStackTrace();
        }
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu menuNavigation = new Menu("Navigation");

        // Création des options du menu
        MenuItem menuTransport = new MenuItem("Gestion des Transports");
        menuTransport.setOnAction(e -> loadView("/Affichage_transport.fxml"));
        MenuItem ajouterTransport = new MenuItem("Ajouter un transport");
        ajouterTransport.setOnAction(e -> loadView("/add_transport.fxml"));

        MenuItem menuAbonnement = new MenuItem("Gestion des Abonnements");
        menuAbonnement.setOnAction(e -> loadView("/Affichage_abonnement.fxml"));
        MenuItem ajouterAbonnement = new MenuItem("Ajouter un abonnement");
        ajouterAbonnement.setOnAction(e -> loadView("/add_abonnement.fxml"));

        MenuItem menuTrajet = new MenuItem("Gestion des Trajets");
        menuTrajet.setOnAction(e -> loadView("/Affichage_trajet.fxml"));
        // Option pour ajouter un trajet
        MenuItem ajouterTrajet = new MenuItem("Ajouter un trajet");
        ajouterTrajet.setOnAction(e -> loadView("/add_trajet.fxml"));

        menuNavigation.getItems().addAll(menuTransport, menuAbonnement, menuTrajet);
        menuBar.getMenus().add(menuNavigation);

        return menuBar;
    }

    private void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent view = loader.load();
            rootLayout.setCenter(view);
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la vue : " + fxmlFileName);
            e.printStackTrace();
        }
    }


}
