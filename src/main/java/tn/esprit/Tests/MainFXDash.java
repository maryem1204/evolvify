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

            // Obtenir les dimensions de l'écran
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Créer la scène avec les dimensions de l'écran
            Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

            // Ajouter des styles CSS pour rendre le contenu responsive si nécessaire
            //scene.getStylesheets().add(getClass().getResource("/css/responsive.css").toExternalForm());

            // Configurer la fenêtre pour utiliser tout l'écran disponible
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());

            // Permettre le redimensionnement
            primaryStage.setResizable(true);

            // Utiliser le mode maximisé pour garder les contrôles de la fenêtre visibles
            primaryStage.setMaximized(true);

            // Commentez ou supprimez ces lignes pour le mode plein écran
            // primaryStage.setFullScreen(true);
            // primaryStage.setFullScreenExitHint("Appuyez sur Échap pour quitter le plein écran");

            primaryStage.setScene(scene);
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
