package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Button;

public class FrontController {

    @FXML
    private Button Accocier_tr;

    @FXML
    private Button Creer_ab;

    @FXML
    private void openAbonnementWindow() {
        openNewWindow("/add_abonnement.fxml", "Créer un abonnement");
    }

    @FXML
    private void openTrajetWindow() {
        openNewWindow("/add_trajet.fxml", "Associer un trajet");
    }

    private void openNewWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
