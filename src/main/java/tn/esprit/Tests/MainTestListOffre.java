package tn.esprit.Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainTestListOffre extends Application {

        @Override
        public void start(Stage primaryStage) {
            try {
                URL fxmlUrl = getClass().getResource("/ListOffre.fxml");
                if (fxmlUrl == null) {
                    System.out.println("Le fichier FXML n'a pas été trouvé !");
                    return;
                }

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                Scene scene = new Scene(root);
               // scene.getStylesheets().add(getClass().getResource("C:/Users/HP/IdeaProjects/Gestion-Ressources-Humaine/src/main/java/tn/esprit/Styles/Button.css").toExternalForm());
                primaryStage.setTitle("Les Offres Disponibles");
                primaryStage.setScene(scene);
                primaryStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }





}
