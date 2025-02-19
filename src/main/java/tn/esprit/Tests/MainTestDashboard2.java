package tn.esprit.Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.layout.StackPane;
import javafx.stage.StageStyle;

public class MainTestDashboard2 extends Application {

    @Override
    public void start(Stage stage) throws IOException {


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DachboardListOffre.fxml"));
        if (loader.getLocation() == null) {
           System.out.println("Le fichier FXML est introuvable !");
        }
        Parent root = loader.load();


        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/tn/esprit/Styles/color.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Dashboard");
        stage.show();

    }


}
