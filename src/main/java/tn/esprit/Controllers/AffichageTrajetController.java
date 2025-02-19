package tn.esprit.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Entities.MoyenTransport;
import tn.esprit.Entities.Trajet;
import tn.esprit.Services.TrajetCRUD;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AffichageTrajetController {

    @FXML
    private TableView<Trajet> tableTrajet;

    @FXML
    private TableColumn<Trajet, Integer> colIdT;

    @FXML
    private TableColumn<Trajet, String> colPointDep;

    @FXML
    private TableColumn<Trajet, String> colPointArr;

    @FXML
    private TableColumn<Trajet, Double> colDistance;

    @FXML
    private TableColumn<Trajet, Time> colDureeEstimee;

    @FXML
    private TableColumn<Trajet, String> colIdMoyen;

    @FXML
    private TableColumn<Trajet, Integer> colIdEmploye;

    @FXML
    private TableColumn<Trajet, String> colStatus;

    @FXML
    private TextField recherche;

    @FXML
    private TableColumn<Trajet, Void> colAction;

    private ObservableList<Trajet> trajets = FXCollections.observableArrayList();

    private static final Logger logger = Logger.getLogger(AffichageTrajetController.class.getName());

    private ObservableList<Trajet> filteredTrajetList = FXCollections.observableArrayList();

    @FXML
    void loadTrajets() {
        TrajetCRUD trajetCRUD = new TrajetCRUD();
        try {
            List<Trajet> trajetList = trajetCRUD.showAll();
            if (trajetList.isEmpty()) {
                afficherAlerte("Aucun trajet", "Aucun trajet n'a été trouvé dans la base de données.");
            }
            trajets.setAll(trajetList);
            tableTrajet.setItems(trajets);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des trajets", e);
            afficherAlerte("Erreur", "Impossible de charger les trajets.");
        }
    }

    @FXML
    private void handleAjouterTrajet() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_trajet.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Ajouter Trajet");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadTrajets();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ouverture de la fenêtre d'ajout", e);
        }
    }

    @FXML
    private void addActionsColumn() {
        colAction.setCellFactory(param -> new TableCell<Trajet, Void>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox hbox = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                btnEdit.setOnAction(event -> {
                    Trajet trajet = getTableView().getItems().get(getIndex());
                    showEditPopup(trajet);
                });

                btnDelete.setOnAction(event -> {
                    Trajet trajet = getTableView().getItems().get(getIndex());
                    confirmDelete(trajet);
                });
            }

            private void showEditPopup(Trajet trajet) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_trajet.fxml"));
                    Parent root = loader.load();
                    ModifierTrajetController controller = loader.getController();
                    controller.setTrajet(trajet);

                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setTitle("Modifier Trajet");
                    stage.setScene(new Scene(root));
                    stage.showAndWait();
                    loadTrajets();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Erreur lors de l'ouverture de la fenêtre de modification", e);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

    private void confirmDelete(Trajet trajet) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce trajet ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                TrajetCRUD trajetCRUD = new TrajetCRUD();
                int deletedRows = trajetCRUD.delete(trajet);
                if (deletedRows > 0) {
                    afficherAlerte("Suppression réussie", "Le trajet a été supprimé avec succès.");
                    loadTrajets();
                } else {
                    afficherAlerte("Erreur", "Aucun trajet n'a été supprimé.");
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erreur lors de la suppression du trajet", e);
                afficherAlerte("Erreur", "Impossible de supprimer le trajet.");
            }
        }
    }

    @FXML
    public void initialize() {
        colIdT.setCellValueFactory(new PropertyValueFactory<>("idT"));
        colPointDep.setCellValueFactory(new PropertyValueFactory<>("pointDep"));
        colPointArr.setCellValueFactory(new PropertyValueFactory<>("pointArr"));
        colDistance.setCellValueFactory(new PropertyValueFactory<>("distance"));
        colDureeEstimee.setCellValueFactory(new PropertyValueFactory<>("dureeEstime"));
        colIdMoyen.setCellValueFactory(new PropertyValueFactory<>("idMoyen"));




        colIdEmploye.setCellValueFactory(new PropertyValueFactory<>("idEmploye"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        addActionsColumn();
        recherche.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        loadTrajets();
    }

    @FXML
    private void handleSearch() {
        String keyword = recherche.getText();
        filterTrajets(keyword);
    }

    @FXML
    private void filterTrajets(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            tableTrajet.setItems(trajets);
            return;
        }

        String searchKeyword = keyword.toLowerCase();

        List<Trajet> filteredList = trajets.stream()
                .filter(trajet -> trajet.getPointDep().toLowerCase().contains(searchKeyword)
                        || trajet.getPointArr().toLowerCase().contains(searchKeyword)
                        || String.valueOf(trajet.getDistance()).contains(searchKeyword)
                        || trajet.getStatus().toLowerCase().contains(searchKeyword))
                .collect(Collectors.toList());

        filteredTrajetList.setAll(filteredList);
        tableTrajet.setItems(filteredTrajetList);
    }

    @FXML
    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
