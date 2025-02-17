package tn.esprit.Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFXLogin extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/loginUser.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Login");
            primaryStage.setResizable(false);

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
