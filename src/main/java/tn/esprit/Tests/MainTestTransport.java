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

public class MainTestTransport extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Charger la vue initiale (Affichage_transport.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Affichage_transport.fxml"));
        Parent root = loader.load();

        // Créer une scène avec la vue chargée
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createMenuBar(stage, borderPane)); // Ajout du menu
        borderPane.setCenter(root);

        Scene scene = new Scene(borderPane);

        // Ajouter le fichier CSS
        String cssPath = "/tn/esprit/Styles/color.css";
        if (getClass().getResource(cssPath) != null) {
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(cssPath)).toExternalForm());
        } else {
            System.err.println("CSS file not found: " + cssPath);
        }

        // Configurer la scène et le titre
        stage.setScene(scene);
        stage.setTitle("Gestion des Transports");
        stage.show();
    }

    private MenuBar createMenuBar(Stage stage, BorderPane borderPane) {
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Navigation");

        // Option pour afficher la liste des moyens de transport
        MenuItem afficherTransport = new MenuItem("Afficher les transports");
        afficherTransport.setOnAction(e -> loadView("/fxml/Affichage_transport.fxml", borderPane));

        // Option pour ajouter un moyen de transport
        MenuItem ajouterTransport = new MenuItem("Ajouter un transport");
        ajouterTransport.setOnAction(e -> loadView("/fxml/add_transport.fxml", borderPane));

        menu.getItems().addAll(afficherTransport, ajouterTransport);
        menuBar.getMenus().add(menu);

        return menuBar;
    }

    private void loadView(String fxmlFile, BorderPane borderPane) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent view = loader.load();
            borderPane.setCenter(view);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la vue : " + fxmlFile);
            e.printStackTrace();
        }
    }


}
