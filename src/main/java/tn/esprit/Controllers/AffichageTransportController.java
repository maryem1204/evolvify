package tn.esprit.Controllers;

import javafx.beans.property.SimpleObjectProperty;
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
import tn.esprit.Entities.StatusTransport;
import tn.esprit.Services.MoyenTransportCRUD;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AffichageTransportController {

    @FXML
    private TableView<MoyenTransport> tableMoyenTransport;
    @FXML
    private TableColumn<MoyenTransport, Integer> colId;
    @FXML
    private TableColumn<MoyenTransport, String> colType;
    @FXML
    private TableColumn<MoyenTransport, Integer> colCapacite;
    @FXML
    private TableColumn<MoyenTransport, Integer> colImmatriculation;
    @FXML
    private TableColumn<MoyenTransport, StatusTransport> colStatus;
    @FXML
    private TableColumn<MoyenTransport, Void> colAction;
    @FXML
    private TextField recherche;

    private final ObservableList<MoyenTransport> moyensTransport = FXCollections.observableArrayList();
    private final ObservableList<MoyenTransport> filteredMoyenTransportList = FXCollections.observableArrayList();
    private static final Logger logger = Logger.getLogger(AffichageTransportController.class.getName());

    @FXML
    void loadMoyens() {
        MoyenTransportCRUD moyenTransportCRUD = new MoyenTransportCRUD();
        try {
            List<MoyenTransport> moyensList = moyenTransportCRUD.showAll();

            moyensTransport.setAll(moyensList);
            tableMoyenTransport.setItems(moyensTransport);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des moyens de transport", e);
            afficherAlerte("Erreur", "Impossible de charger les moyens de transport.");
        }
    }

    @FXML
    private void addActionsColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox hbox = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                btnEdit.setOnAction(event -> {
                    MoyenTransport moyenTransport = getTableView().getItems().get(getIndex());
                    showEditPopup(moyenTransport);
                });

                btnDelete.setOnAction(event -> {
                    MoyenTransport moyenTransport = getTableView().getItems().get(getIndex());
                    confirmDelete(moyenTransport);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void showEditPopup(MoyenTransport moyenTransport) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifier_transport.fxml"));
            Parent root = loader.load();

            ModifierTransportController controller = loader.getController();
            controller.setMoyenTransport(moyenTransport);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier le moyen de transport");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadMoyens();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ouverture de la fenêtre de modification", e);
        }
    }

    private void confirmDelete(MoyenTransport moyenTransport) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer ce moyen de transport ?", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation de suppression");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                new MoyenTransportCRUD().delete(moyenTransport);
                loadMoyens();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erreur lors de la suppression", e);
                afficherAlerte("Erreur", "Impossible de supprimer le moyen de transport.");
            }
        }
    }

    @FXML
    void handleAjouterTransport() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_transport.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.setTitle("Ajouter un moyen de transport");
        newStage.showAndWait();

        loadMoyens();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idMoyen"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeMoyen"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacité"));
        colImmatriculation.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));

        // Correction de la liaison avec StatusTransport
        colStatus.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getStatus()));

        // Affichage correct du statut dans la table
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(StatusTransport status, boolean empty) {
                super.updateItem(status, empty);
                setText(empty || status == null ? "" : status.toString());
            }
        });

        addActionsColumn();
        recherche.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        loadMoyens();
    }

    @FXML
    private void handleSearch() {
        filterTransport(recherche.getText());
    }

    private void filterTransport(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            tableMoyenTransport.setItems(moyensTransport);
            return;
        }

        String searchKeyword = keyword.toLowerCase();
        List<MoyenTransport> filteredList = moyensTransport.stream()
                .filter(transport -> transport.getTypeMoyen().toLowerCase().contains(searchKeyword)
                        || String.valueOf(transport.getImmatriculation()).contains(searchKeyword)  // Correction ici
                        || String.valueOf(transport.getCapacité()).contains(searchKeyword)
                        || transport.getStatus().name().toLowerCase().contains(searchKeyword))
                .collect(Collectors.toList());

        filteredMoyenTransportList.setAll(filteredList);
        tableMoyenTransport.setItems(filteredMoyenTransportList);
    }


    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(titre);
        alert.showAndWait();
    }
}
