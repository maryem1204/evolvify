package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

import java.io.IOException;

public class ForgotPasswordController {
    @FXML
    private Hyperlink backToLogin;
    @FXML
    private void handleLogin(ActionEvent event) {
        System.out.println("Hyperlink cliqué !");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/loginUser.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement du fichier FXML.");
        }
    }
}
