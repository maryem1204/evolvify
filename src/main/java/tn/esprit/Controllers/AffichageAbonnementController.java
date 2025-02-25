package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Entities.Abonnement;
import tn.esprit.Services.AbonnementCRUD;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AffichageAbonnementController {

    @FXML
    private TableView<Abonnement> tableViewAbonnement;

    @FXML
    private TableColumn<Abonnement, Integer> colIdAb;

    @FXML
    private TableColumn<Abonnement, String> colTypeAb;

    @FXML
    private TableColumn<Abonnement, String> colDateDebut;

    @FXML
    private TableColumn<Abonnement, String> colDateExp;

    @FXML
    private TableColumn<Abonnement, Double> colPrix;

    @FXML
    private TableColumn<Abonnement, Integer> colIdEmploye;

    @FXML
    private TableColumn<Abonnement, String> colStatus;

    @FXML
    private TableColumn<Abonnement, Void> colAction;

    @FXML
    private TextField recherche;

    @FXML
    private ComboBox<String> triPrixComboBox;
    @FXML
    private Label lblTotalAbonnements;

    @FXML
    private Label lblMoyennePrix;

    @FXML
    private PieChart pieChartAbonnements;



    @FXML
    private ObservableList<Abonnement> abonnements = FXCollections.observableArrayList();

    @FXML
    private static final Logger logger = Logger.getLogger(AffichageAbonnementController.class.getName());

    @FXML
    private ObservableList<Abonnement> filteredAbonnementList = FXCollections.observableArrayList();


    @FXML
    void loadAbonnements() {
        AbonnementCRUD abonnementCRUD = new AbonnementCRUD();
        try {
            List<Abonnement> abonnementsList = abonnementCRUD.showAll();
            abonnements.setAll(abonnementsList);
            tableViewAbonnement.setItems(abonnements);

            // 🔥 Mettre à jour les statistiques
            updateStatistics();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des abonnements", e);
            afficherAlerte("Erreur", "Impossible de charger les abonnements.");
        }
    }

    private void updateStatistics() {
        if (abonnements.isEmpty()) return;

        // 1️⃣ Nombre total d'abonnements
        lblTotalAbonnements.setText(String.valueOf(abonnements.size()));

        // 2️⃣ Moyenne des prix des abonnements
        double moyennePrix = abonnements.stream()
                .mapToDouble(Abonnement::getPrix)
                .average()
                .orElse(0.0);
        lblMoyennePrix.setText(String.format("%.2f DT", moyennePrix));

        // 3️⃣ Répartition des abonnements par type (PieChart)
        Map<String, Long> abonnementParType = abonnements.stream()
                .collect(Collectors.groupingBy(Abonnement::getType_Ab, Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        abonnementParType.forEach((type, count) -> pieChartData.add(new PieChart.Data(type, count)));

        pieChartAbonnements.setData(pieChartData);
    }

    @FXML
    private void handleTriPrix() {
        String choix = triPrixComboBox.getValue();
        if (choix != null) {
            List<Abonnement> sortedList;
            if (choix.equals("Prix Ascendant")) {
                sortedList = abonnements.stream()
                        .sorted(Comparator.comparingDouble(Abonnement::getPrix))
                        .collect(Collectors.toList());
            } else { // Prix Descendant
                sortedList = abonnements.stream()
                        .sorted(Comparator.comparingDouble(Abonnement::getPrix).reversed())
                        .collect(Collectors.toList());
            }
            tableViewAbonnement.setItems(FXCollections.observableArrayList(sortedList));
        }
    }

    @FXML
    private void addActionsColumn() {
        colAction.setCellFactory(param -> new TableCell<Abonnement, Void>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox hbox = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                btnEdit.setOnAction(event -> {
                    Abonnement abonnement = getTableView().getItems().get(getIndex());
                    showEditPopup(abonnement);
                });

                btnDelete.setOnAction(event -> {
                    Abonnement abonnement = getTableView().getItems().get(getIndex());
                    confirmDelete(abonnement);
                });
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

            loadAbonnements(); // Recharger après modification
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
                loadAbonnements(); // Recharger après suppression
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erreur lors de la suppression de l'abonnement", e);
                afficherAlerte("Erreur", "Impossible de supprimer l'abonnement.");
            }
        }
    }

    @FXML
    void handleAjouterAbonnement() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_abonnement.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        String cssPath = "/styles/add_transport.css";
        if (getClass().getResource(cssPath) != null) {
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        } else {
            logger.log(Level.WARNING, "Fichier CSS non trouvé : " + cssPath);
        }

        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.setTitle("Ajouter un abonnement");
        newStage.showAndWait();

        loadAbonnements();
    }

    @FXML
    public void initialize() {
        // Initialisation des colonnes
        colIdAb.setCellValueFactory(new PropertyValueFactory<>("id_Ab"));
        colTypeAb.setCellValueFactory(new PropertyValueFactory<>("type_Ab"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        colDateExp.setCellValueFactory(new PropertyValueFactory<>("date_exp"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colIdEmploye.setCellValueFactory(new PropertyValueFactory<>("id_employe"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Ajouter la colonne d'actions
        addActionsColumn();
        recherche.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        triPrixComboBox.setOnAction(event -> handleTriPrix());


        // Charger les abonnements
        loadAbonnements();
    }

    @FXML
    private void handleSearch() {
        String keyword = recherche.getText();
        filterAbonnement(keyword);
    }

    private void filterAbonnement(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            tableViewAbonnement.setItems(abonnements);
            return;
        }

        String searchKeyword = keyword.toLowerCase();

        // Filtrer la liste des abonnements
        List<Abonnement> filteredList = abonnements.stream()
                .filter(abonnement -> abonnement.getType_Ab().toLowerCase().contains(searchKeyword)
                        || String.valueOf(abonnement.getId_Ab()).contains(searchKeyword)
                        || String.valueOf(abonnement.getId_employe()).contains(searchKeyword)
                        || String.valueOf(abonnement.getPrix()).contains(searchKeyword)
                        || (abonnement.getStatus() != null && abonnement.getStatus().name().toLowerCase().contains(searchKeyword)))
                .collect(Collectors.toList());

        filteredAbonnementList.setAll(filteredList);
        tableViewAbonnement.setItems(filteredAbonnementList);
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
