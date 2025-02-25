package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.Entities.Projet;
import tn.esprit.Entities.Tache;
import tn.esprit.Services.TacheService;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class tacheListController {

    @FXML
    private TableView<Tache> tacheTable;
    @FXML
    private TableColumn<Tache, String> colDescription;
    @FXML
    private TableColumn<Tache, Tache.Status> colStatus;
    @FXML
    private TableColumn<Tache, Date> colCreatedDate;
    @FXML
    private TableColumn<Tache, Tache.Priority> colPriority;
    @FXML
    private TableColumn<Tache, String> colLocation;
    @FXML
    private TableColumn<Tache, Void> colActions;
    @FXML
    private TextField recherche;
    private Projet projetActuel;


    private ObservableList<Tache> taches = FXCollections.observableArrayList();
    private ObservableList<Tache> filteredTache = FXCollections.observableArrayList();
    private final TacheService tacheService = new TacheService();
    private Map<Integer, String> userMap = new HashMap<>();
    private static tacheListController instance;

    public tacheListController() {
        instance = this;
    }

    public static tacheListController getInstance() {
        return instance;
    }

    public void initialize() throws SQLException {
        tacheTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Mapping columns
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        setupStatusColumn();
        colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("created_at"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        setupPriorityColumn();

        loadTaches();
        addActionsColumn();
        setUpSearchListener();
    }

    public void setProjet(Projet projet) throws SQLException {
        this.projetActuel = projet;
        loadTaches(); // Charger les tâches associées à ce projet
    }

    private void setupStatusColumn() {
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Tache, Tache.Status>() {
            private final Label statusLabel = new Label();

            @Override
            protected void updateItem(Tache.Status status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    statusLabel.setText(status.toString());
                    statusLabel.getStyleClass().clear();
                    statusLabel.getStyleClass().add("status-label");

                    switch (status) {
                        case TO_DO:
                            statusLabel.getStyleClass().add("status-en-cours");
                            break;
                        case IN_PROGRESS:
                            statusLabel.getStyleClass().add("status-terminee");
                            break;
                        case DONE:
                            statusLabel.getStyleClass().add("status-valide");
                            break;
                        case CANCELED:
                            statusLabel.getStyleClass().add("status-en-attente");
                            break;
                    }

                    setGraphic(statusLabel);
                }
            }
        });
    }

    private void setupPriorityColumn() {
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));

        colPriority.setCellFactory(column -> new TableCell<Tache, Tache.Priority>() {
            @Override
            protected void updateItem(Tache.Priority priority, boolean empty) {
                super.updateItem(priority, empty);

                if (empty || priority == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(""); // Réinitialisation du style
                } else {
                    setText(priority.name());

                    // Suppression des anciennes classes de style
                    getStyleClass().removeAll("priority-haute", "priority-moyenne", "priority-basse");

                    // Appliquer le bon style
                    switch (priority) {
                        case HIGH -> setStyle("-fx-background-color: #f68d8d; -fx-text-fill: #020202;");
                        case MEDIUM -> setStyle("-fx-background-color: #f1ecb9; -fx-text-fill: #020202;");
                        case LOW -> setStyle("-fx-background-color: #afefaf; -fx-text-fill: #000000;");
                    }
                }
            }
        });

}


    private void setUpSearchListener() {
        recherche.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
    }

    @FXML
    private void handleSearch() {
        String keyword = recherche.getText();
        filterTaches(keyword);
    }

    private void filterTaches(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            tacheTable.setItems(taches);
            return;
        }

        String searchKeyword = keyword.toLowerCase();
        List<Tache> filteredList = taches.stream()
                .filter(tache -> {
                    String description = tache.getDescription() != null ? tache.getDescription().toLowerCase() : "";
                    String status = tache.getStatus() != null ? tache.getStatus().toString().toLowerCase() : "";
                    return description.contains(searchKeyword) || status.contains(searchKeyword);
                })
                .collect(Collectors.toList());

        filteredTache.setAll(filteredList);
        tacheTable.setItems(filteredTache);
    }

    private void loadTaches() throws SQLException {
        List<Tache> tachesList = tacheService.showAll();
        taches.setAll(tachesList);
        tacheTable.setItems(taches);
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Tache, Void>() {
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/editIcon.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/deleteIconn.png")));
            private final HBox hbox = new HBox(10, editIcon, deleteIcon);

            {
                // Set icon size and cursor
                editIcon.setFitHeight(20);
                editIcon.setFitWidth(20);
                deleteIcon.setFitHeight(20);
                deleteIcon.setFitWidth(20);
                editIcon.setStyle("-fx-cursor: hand;");
                deleteIcon.setStyle("-fx-cursor: hand;");

                // Edit and delete actions
                editIcon.setOnMouseClicked(event -> {
                    Tache tache = getTableView().getItems().get(getIndex());
                    showEditPopup(tache);
                });

                deleteIcon.setOnMouseClicked(event -> {
                    Tache tache = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(tache);
                });
            }


            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }
    @FXML private void openAjoutTachePopup()
    { try { FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjoutTache.fxml"));
        Parent root = loader.load(); Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.UNDECORATED);
        popupStage.setTitle("Ajouter une tache");
        popupStage.setScene(new Scene(root));
        popupStage.showAndWait();
        refreshTacheList(); }
    catch (IOException e) { e.printStackTrace(); } }



    private void showDeleteConfirmation(Tache tache) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DeleteTache.fxml"));
            Parent root = loader.load();
            DeleteTacheController controller = loader.getController();
            controller.setTache(tache);
            controller.setTacheListController(this);

            Stage deleteStage = new Stage();
            deleteStage.initStyle(StageStyle.UNDECORATED);
            deleteStage.setTitle("Suppression de Tâche");
            deleteStage.setScene(new Scene(root));
            deleteStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showEditPopup(Tache tache) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierTache.fxml"));
            Parent root = loader.load();
            ModifierTacheController controller = loader.getController();
            controller.setTache(tache);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);  // Applique le même style que pour la suppression
            stage.setTitle("Modification de Tâche");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshTacheList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void refreshTacheList() {
        try {
            List<Tache> tacheList = tacheService.showAll();
            taches.setAll(tacheList);
            tacheTable.setItems(taches);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

