package tn.esprit.Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Dashboard");
            primaryStage.setResizable(false);

            //primaryStage.setMinWidth(1366); // Largeur minimale (ex : Full HD)
            //primaryStage.setMinHeight(768); // Hauteur minimale       e'

            //primaryStage.initStyle(StageStyle.UTILITY);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface FXML !");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
