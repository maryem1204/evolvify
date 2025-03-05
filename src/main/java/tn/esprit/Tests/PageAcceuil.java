package tn.esprit.Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PageAcceuil extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // Charger le fichier FXML et le contrôleur
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PageAcceuil.fxml"));
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root);

            // Désactiver le redimensionnement de la fenêtre
            stage.setResizable(false);

            // Configurer la scène et la fenêtre
            stage.setScene(scene);
            stage.setTitle("Page Accueil");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur : Impossible de charger PageAcceuil.fxml");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
