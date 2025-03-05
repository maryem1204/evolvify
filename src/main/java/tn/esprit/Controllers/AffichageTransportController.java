package tn.esprit.Controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Entities.MoyenTransport;
import tn.esprit.Entities.StatusTransport;
import tn.esprit.Services.MoyenTransportCRUD;
import tn.esprit.Services.TransportStatisticsService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
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

    // Ajout pour les statistiques
    @FXML
    private Button btnShowStatistics;

    private final ObservableList<MoyenTransport> moyensTransport = FXCollections.observableArrayList();
    private final ObservableList<MoyenTransport> filteredMoyenTransportList = FXCollections.observableArrayList();
    private static final Logger logger = Logger.getLogger(AffichageTransportController.class.getName());
    private final TransportStatisticsService statisticsService = new TransportStatisticsService();

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

    // Nouvelle méthode pour afficher les statistiques
    @FXML
    void handleShowStatistics() {
        try {
            showStatisticsWindow();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des statistiques", e);
            afficherAlerte("Erreur", "Impossible de charger les statistiques de capacité.");
        }
    }

    private void showStatisticsWindow() throws SQLException {
        // Créer une nouvelle fenêtre
        Stage statisticsStage = new Stage();
        statisticsStage.setTitle("Statistiques de Capacité");
        statisticsStage.initModality(Modality.APPLICATION_MODAL);

        // Préparer les statistiques
        int totalCapacity = statisticsService.calculateTotalCapacity();
        double averageCapacity = statisticsService.calculateAverageCapacity();
        Map<String, Integer> capacityByType = statisticsService.getCapacityByType();
        Map<StatusTransport, Integer> capacityByStatus = statisticsService.getCapacityByStatus();
        MoyenTransport maxCapacityTransport = statisticsService.getTransportWithHighestCapacity();
        MoyenTransport minCapacityTransport = statisticsService.getTransportWithLowestCapacity();

        // Créer un résumé des statistiques
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(20));

        Label titleLabel = new Label("Résumé des Statistiques de Capacité");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label totalCapacityLabel = new Label("Capacité Totale: " + totalCapacity);
        Label avgCapacityLabel = new Label(String.format("Capacité Moyenne: %.2f", averageCapacity));

        Label maxCapacityLabel = new Label("Transport avec la plus grande capacité: " +
                (maxCapacityTransport != null ?
                        maxCapacityTransport.getTypeMoyen() + " (" + maxCapacityTransport.getCapacité() + ")" :
                        "Aucun"));

        Label minCapacityLabel = new Label("Transport avec la plus petite capacité: " +
                (minCapacityTransport != null ?
                        minCapacityTransport.getTypeMoyen() + " (" + minCapacityTransport.getCapacité() + ")" :
                        "Aucun"));

        // Créer un graphique pour la capacité par type
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Capacité par Type de Transport");
        xAxis.setLabel("Type de Transport");
        yAxis.setLabel("Capacité");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Capacité Totale");

        for (Map.Entry<String, Integer> entry : capacityByType.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);
        barChart.setPrefHeight(400);

        // Créer un graphique pour la capacité par statut
        CategoryAxis xAxis2 = new CategoryAxis();
        NumberAxis yAxis2 = new NumberAxis();
        BarChart<String, Number> statusChart = new BarChart<>(xAxis2, yAxis2);
        statusChart.setTitle("Capacité par Statut de Transport");
        xAxis2.setLabel("Statut");
        yAxis2.setLabel("Capacité");

        XYChart.Series<String, Number> statusSeries = new XYChart.Series<>();
        statusSeries.setName("Capacité par Statut");

        for (Map.Entry<StatusTransport, Integer> entry : capacityByStatus.entrySet()) {
            statusSeries.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
        }

        statusChart.getData().add(statusSeries);
        statusChart.setPrefHeight(400);

        // Ajouter tous les éléments à la boîte principale
        statsBox.getChildren().addAll(
                titleLabel,
                totalCapacityLabel,
                avgCapacityLabel,
                maxCapacityLabel,
                minCapacityLabel,
                new Separator(),
                barChart,
                new Separator(),
                statusChart
        );

        Scene scene = new Scene(statsBox, 800, 900);
        statisticsStage.setScene(scene);
        statisticsStage.show();
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

        // Si le bouton existe dans le FXML, on lui assigne l'action
        if (btnShowStatistics != null) {
            btnShowStatistics.setOnAction(event -> handleShowStatistics());
        }

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
                        || String.valueOf(transport.getImmatriculation()).contains(searchKeyword)
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