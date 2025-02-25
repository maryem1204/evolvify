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

public class MainTestTrajet extends Application {

    @Override
    public void start(Stage stage) {

        try {
            // Charger la vue initiale (Affichage_trajet.fxml)
            Parent root = loadFXML("/fxml/Affichage_trajet.fxml");

            // Créer une scène avec la vue chargée
            Scene scene = new Scene(root);

            // Ajouter le fichier CSS
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/tn/esprit/Styles/color.css")).toExternalForm()
            );

            // Configurer la scène et le titre
            stage.setTitle("Gestion des Trajets");

            // Ajouter le menu et les actions
            MenuBar menuBar = createMenuBar(stage);

            // Afficher le menu
            BorderPane borderPane = new BorderPane();
            borderPane.setTop(menuBar);
            borderPane.setCenter(root);

            // Ajouter la scène avec le menu
            Scene menuScene = new Scene(borderPane);
            stage.setScene(menuScene);

            // Afficher la scène
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de l'application : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour charger un fichier FXML et renvoyer la racine du parent
    private Parent loadFXML(String fxmlFile) throws IOException {
        return FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
    }

    // Crée et retourne la barre de menu avec les options de navigation
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        // Menu principal
        Menu menu = new Menu("Navigation");

        // Option pour afficher la liste des trajets
        MenuItem afficherTrajets = new MenuItem("Afficher les trajets");
        afficherTrajets.setOnAction(e -> loadView("/fxml/Affichage_trajet.fxml", stage));

        // Option pour ajouter un trajet
        MenuItem ajouterTrajet = new MenuItem("Ajouter un trajet");
        ajouterTrajet.setOnAction(e -> loadView("/fxml/add_trajet.fxml", stage));

        menu.getItems().addAll(afficherTrajets, ajouterTrajet);
        menuBar.getMenus().add(menu);

        return menuBar;
    }

    // Méthode pour charger une vue spécifique dans la zone centrale du BorderPane
    private void loadView(String fxmlFile, Stage stage) {
        try {
            Parent view = loadFXML(fxmlFile);
            ((BorderPane) stage.getScene().getRoot()).setCenter(view);
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la vue : " + fxmlFile);
            e.printStackTrace();
        }
    }


}
