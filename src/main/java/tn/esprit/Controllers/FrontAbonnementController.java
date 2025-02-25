package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Entities.Abonnement;
import tn.esprit.Services.AbonnementCRUD;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FrontAbonnementController {

    @FXML
    private VBox vboxAbonnements; // Correction : le bon ID du VBox dans le FXML

    @FXML
    private TextField recherche;

    private ObservableList<Abonnement> abonnements = FXCollections.observableArrayList();

    private static final Logger logger = Logger.getLogger(FrontAbonnementController.class.getName());

    @FXML
    void loadAbonnements() {
        AbonnementCRUD abonnementCRUD = new AbonnementCRUD();
        try {
            List<Abonnement> abonnementsList = abonnementCRUD.showAll();
            abonnements.setAll(abonnementsList);
            displayAbonnements();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des abonnements", e);
            afficherAlerte("Erreur", "Impossible de charger les abonnements.");
        }
    }

    private void displayAbonnements() {
        vboxAbonnements.getChildren().clear();
        for (Abonnement abonnement : abonnements) {
            HBox card = createAbonnementCard(abonnement);
            vboxAbonnements.getChildren().add(card);
        }
    }

    private HBox createAbonnementCard(Abonnement abonnement) {
        HBox card = new HBox(20);
        card.setStyle("-fx-border-color: #ccc; -fx-padding: 10px; -fx-background-color: #f9f9f9;");

        Text type = new Text("Type: " + abonnement.getType_Ab());
        Text prix = new Text("Prix: " + abonnement.getPrix() + " €");
        Text status = new Text("Statut: " + abonnement.getStatus());

        Button btnEdit = new Button("Modifier");
        Button btnDelete = new Button("Supprimer");

        btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        btnEdit.setOnAction(event -> showEditPopup(abonnement));
        btnDelete.setOnAction(event -> confirmDelete(abonnement));

        card.getChildren().addAll(type, prix, status, btnEdit, btnDelete);
        return card;
    }

    private void showEditPopup(Abonnement abonnement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_abonnement.fxml"));
            Parent root = loader.load();
            ModifierAbonnementController controller = loader.getController();
            controller.setAbonnement(abonnement);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'abonnement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadAbonnements();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ouverture de la fenêtre de modification", e);
        }
    }

    private void confirmDelete(Abonnement abonnement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet abonnement ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                AbonnementCRUD abonnementCRUD = new AbonnementCRUD();
                abonnementCRUD.delete(abonnement);
                loadAbonnements();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erreur lors de la suppression de l'abonnement", e);
                afficherAlerte("Erreur", "Impossible de supprimer l'abonnement.");
            }
        }
    }

    @FXML
    public void initialize() {
        recherche.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        loadAbonnements();
    }

    @FXML
    private void handleSearch() {
        String keyword = recherche.getText().toLowerCase();
        if (keyword.isEmpty()) {
            displayAbonnements();
            return;
        }
        vboxAbonnements.getChildren().clear();
        abonnements.stream()
                .filter(a -> a.getType_Ab().toLowerCase().contains(keyword) ||
                        String.valueOf(a.getPrix()).contains(keyword) ||
                        a.getStatus().toString().toLowerCase().contains(keyword))
                .map(this::createAbonnementCard)
                .forEach(vboxAbonnements.getChildren()::add);
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleAjouterAbonnement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_abonnement.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            String cssPath = "/styles/add_transport.css";

            // Vérifier si le fichier CSS existe avant de l'ajouter
            if (getClass().getResource(cssPath) != null) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            } else {
                logger.log(Level.WARNING, "Fichier CSS non trouvé : " + cssPath);
            }

            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.setTitle("Ajouter un abonnement");
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.showAndWait();

            // Rafraîchir la liste des abonnements après l'ajout
            loadAbonnements();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ouverture de la fenêtre d'ajout", e);
            afficherAlerte("Erreur", "Impossible d'ouvrir la fenêtre d'ajout.");
        }
    }
}
