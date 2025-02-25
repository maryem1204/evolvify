package tn.esprit.Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

public class MainFXDash extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo1.png")));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dash.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Dashboard");

            // Obtenir les dimensions de l'écran SANS cacher la barre des tâches
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Ajustements pour occuper toute la zone visible
            double correctionX = 5;  // Élargir un peu vers la droite
            double correctionY = 5;  // Agrandir vers le bas

            primaryStage.setX(screenBounds.getMinX() - 6); // Déplace légèrement à gauche
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth() + 5);
            primaryStage.setHeight(screenBounds.getHeight() + correctionY);

            // Empêcher le redimensionnement
            primaryStage.setResizable(false);
            primaryStage.setFullScreen(false);

            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface FXML !");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
