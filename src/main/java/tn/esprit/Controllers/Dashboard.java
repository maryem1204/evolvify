package tn.esprit.Controllers;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.Entities.Offre;
import tn.esprit.Services.OffreService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Dashboard {

    @FXML
    private TableColumn<Offre, String> colDesc;

    @FXML
    private TableColumn<Offre, String> colTitre;

    @FXML
    private TableColumn<Offre, String> coldateExp;

    @FXML
    private TableColumn<Offre, String> coldatePub;


    @FXML
    private Button gestcong;

    @FXML
    private Button gestpro;

    @FXML
    private Button gestran;

    @FXML
    private Button gestrec;

    @FXML
    private AnchorPane gests;

    @FXML
    private Button gestutiliser;

    @FXML
    private AnchorPane logo;

    @FXML
    private Button logout;

    @FXML
    private Label logoutlabel;

    @FXML
    private TableColumn<Offre, String> status;

    @FXML
    private AnchorPane tableblanche;

    @FXML
    private TableView<Offre> tabledaffichage;

    @FXML
    private AnchorPane tableofthings;


    @FXML
    private ObservableList<Offre> offres = FXCollections.observableArrayList();
    @FXML
    private static final Logger logger = Logger.getLogger(Dashboard.class.getName());


    private final OffreService offreService = new OffreService();
    @FXML
    private void loadOffres() {
        try {
            List<Offre> offresList = offreService.showAll();  // Cette méthode lance une SQLException
            ObservableList<Offre> offresObservableList = FXCollections.observableArrayList(offresList);
            tabledaffichage.setItems(offresObservableList);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des offres", e);
            afficherAlerte("Erreur", "Impossible de charger les offres.");
        }
    }
    @FXML
    public void initialize() {
        // Lier les colonnes aux propriétés de l'entité Offre
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        coldatePub.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDatePublication().toString()));
        coldateExp.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateExpiration().toString()));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Ajouter la colonne d'actions
        addActionsColumn();

        // Charger les offres
        loadOffres();
    }


    @FXML
    void loadOffres(ActionEvent event) {
        loadOffres();
    }
    @FXML
    void handleAjouterOffre(ActionEvent event) throws IOException {
        // Charger le formulaire d'ajout d'offre (AddOffre.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddOffre.fxml"));
        Parent root = loader.load();

        // Créer une nouvelle scène
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/tn/esprit/Styles/formulCond.css").toExternalForm());

        // Créer une nouvelle fenêtre (Stage)
        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.setTitle("Ajouter une Offre");
        newStage.show();
    }


    @FXML
    void loadOffre() {
        OffreService offreService = new OffreService();
        try {
            List<Offre> offreList = offreService.showAll();
            offres.setAll(offreList);
            tabledaffichage.setItems(offres);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des offres", e);
            afficherAlerte("Erreur", "Impossible de charger les offres");
        }
    }



    @FXML
    private void addActionsColumn() {
        TableColumn<Offre, Void> Action = new TableColumn<>("Actions");
        Action.setCellFactory(param -> new TableCell<Offre, Void>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDelete = new Button("Supprimer");
            private final HBox hbox = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnDelete.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                btnEdit.setOnAction(event -> {
                    Offre offre = getTableView().getItems().get(getIndex());
                    showEditPopup(offre);
                });

                btnDelete.setOnAction(event -> {
                    Offre offre = getTableView().getItems().get(getIndex());
                    confirmDelete(offre);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    System.out.println("Ajout des boutons dans la cellule");
                    setGraphic(hbox);
                }
            }

        });
        // Ajouter la colonne Action à la TableView
        tabledaffichage.getColumns().add(Action);
    }

    private void showEditPopup(Offre offre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierOffre.fxml"));
            Parent root = loader.load();
            ModifierOffre controller = loader.getController();
            controller.setOffre(offre);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'offre");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadOffre(); // Recharger les données après modification
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ouverture de la fenêtre de modification", e);
        }
    }

    private void confirmDelete(Offre offre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet offre ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
               OffreService os= new OffreService();
                os.delete(offre);
                loadOffre(); // Recharger les données après suppression
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erreur lors de la suppression de l'offre", e.getMessage());

                afficherAlerte("Erreur", "Impossible de supprimer l'offre.");
            }
        }
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

