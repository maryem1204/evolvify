package tn.esprit.Controllers;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Entities.Offre;
import tn.esprit.Utils.MyDataBase;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;

import static com.mysql.cj.conf.PropertyKey.logger;

public class ListOffre {
    @FXML
    private VBox offresBox;

    @FXML
    public void initialize() {
        chargerOffres();
    }

    private Connection cnx = MyDataBase.getInstance().getCnx();

    private void chargerOffres() {
        try (PreparedStatement stmt = cnx.prepareStatement("SELECT id_offre,titre, description FROM offre");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idOffre = rs.getInt("id_offre");
                System.out.println("id recupere " + idOffre);
                String titre = rs.getString("titre");
                String description = rs.getString("description");

                // Création du texte pour afficher le titre de l'offre en gras et taille grande
                Text titreText = new Text(titre);
                titreText.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                // Création du texte pour la description en petite taille
                Text descriptionText = new Text(description);
                descriptionText.setStyle("-fx-font-size: 12px; -fx-fill: gray;"); // Taille petite et couleur grise pour la description

                // Création du bouton "Postuler"
                Button postulerBtn = new Button("Postuler");
                postulerBtn.getStyleClass().add("gradient-button"); // Appliquer un style CSS

                postulerBtn.setOnAction(e -> postuler( idOffre));
                postulerBtn.setOnAction(event -> {
                    afficherformulaire(idOffre);

                });

                // Créer un VBox pour contenir le titre et la description
                VBox offreDetails = new VBox(5, titreText, descriptionText); // Espacement de 5px entre le titre et la description
                offreDetails.setAlignment(Pos.CENTER_LEFT); // Alignement à gauche

                // Créer un HBox pour tout contenir (titre + description + bouton)
                HBox offreRow = new HBox(10, offreDetails, postulerBtn);  // Espacement de 10px entre les éléments
                offreRow.setAlignment(Pos.CENTER_LEFT); // Aligner à gauche
                // Alignement du bouton à droite du HBox
                HBox.setHgrow(postulerBtn, Priority.ALWAYS);  // Permet au bouton de se déployer à droite
                offreRow.setHgrow(offreDetails, Priority.SOMETIMES); // Laisser l'espace au titre et description à gauche

                // Ajouter l'HBox à la VBox des offres
                offresBox.getChildren().add(offreRow);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postuler(int idOffre) {
        System.out.println("Postulation envoyée pour : " + idOffre);
    }

    private void afficherformulaire(int idOffre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddCondidate.fxml"));
            Parent root = loader.load();
// Passer l'ID de l'offre au contrôleur AddCondidate
            AddCondidate controller = loader.getController();
            controller.setIdOffre(idOffre);  // Passer l'ID de l'offre
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'offre");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();  // Affiche la trace de l'exception dans la console
        }

    }

}