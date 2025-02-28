package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.Entities.Projet;
import tn.esprit.Entities.Tache;
import tn.esprit.Services.TacheService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KanbanController {

    @FXML private TextField searchField;
    @FXML private Button addTaskButton;
    @FXML private VBox todoTasks;
    @FXML private VBox inProgressTasks;
    @FXML private VBox doneTasks;
    @FXML private VBox canceledTasks;
    @FXML
    private ProgressBar progressBar50;

    @FXML
    private ProgressBar progressBar100;


    private final TacheService tacheService = new TacheService();

    @FXML
    public void initialize() {
        loadTaches();
        setUpSearchListener();
        addTaskButton.setOnAction(event -> openAjoutTachePopup());
    }

    private void setUpSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch(newValue));
    }

    private void handleSearch(String keyword) {
        loadTaches(keyword);
    }

    private void loadTaches() {
        loadTaches("");

    }

    private void loadTaches(String keyword) {
        try {
            List<Tache> taches = tacheService.showAll();
            if (!keyword.isEmpty()) {
                String searchKeyword = keyword.toLowerCase();
                taches = taches.stream()
                        .filter(t -> t.getDescription().toLowerCase().contains(searchKeyword) ||
                                t.getStatus().toString().toLowerCase().contains(searchKeyword))
                        .collect(Collectors.toList());
            }
            populateKanban(taches);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getPriorityColor(Tache.Priority priority) {
        switch (priority) {
            case HIGH:
                return "#DC3545"; // Rouge pour haute priorité
            case MEDIUM:
                return "#FFA500"; // Orange pour priorité moyenne
            case LOW:
                return "#007BFF"; // Bleu pour basse priorité
            default:
                return "#000000"; // Noir par défaut
        }
    }

    private void populateKanban(List<Tache> taches) {
        todoTasks.getChildren().clear();
        inProgressTasks.getChildren().clear();
        doneTasks.getChildren().clear();
        canceledTasks.getChildren().clear();



        for (Tache tache : taches) {
            VBox taskBox = createTaskBox(tache);
            VBox.setMargin(taskBox, new Insets(5, 0, 5, 0));

            switch (tache.getStatus()) {
                case TO_DO:
                    todoTasks.getChildren().add(taskBox);
                    break;
                case IN_PROGRESS:
                    inProgressTasks.getChildren().add(taskBox);
                    break;
                case DONE:
                    doneTasks.getChildren().add(taskBox);
                    break;
                case CANCELED:
                    canceledTasks.getChildren().add(taskBox);
                    break;
            }
        }
    }

    private VBox createTaskBox(Tache tache) {
        VBox taskBox = new VBox();
        taskBox.setStyle(
                "-fx-background-color: white; " +
                        "-fx-padding: 10px; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-color: " + getPriorityColor(tache.getPriority()) + "; " +
                        "-fx-margin-bottom: 10px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 2, 2);"
        );

        Label descLabel = new Label("Description: " + tache.getDescription());
        descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        Label dateLabel = new Label("Date: " + tache.getCreated_at());
        Label employeLabel = new Label("Employé ID: " + tache.getId_employe());
        Label priorityLabel = new Label("Priorité: " + tache.getPriority());
        priorityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + getPriorityColor(tache.getPriority()) + ";");

        // Icône de suppression
        ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/deleteIconn.png")));
        deleteIcon.setFitWidth(20);  // Augmenter la taille pour meilleure visibilité
        deleteIcon.setFitHeight(20);
        deleteIcon.setPreserveRatio(true);
        deleteIcon.setStyle("-fx-cursor: hand;");
        deleteIcon.setOnMouseClicked(event -> showDeleteConfirmation(tache));

        // Centrer l'icône de suppression
        HBox deleteBox = new HBox(deleteIcon);
        deleteBox.setStyle("-fx-alignment: center-right; -fx-padding: 5px;");

        // Ajouter les éléments au VBox
        taskBox.getChildren().addAll(descLabel, dateLabel, employeLabel, priorityLabel, deleteBox);
        addDragAndDropFunctionality(taskBox, tache);

        return taskBox;
    }

    private void addDragAndDropFunctionality(VBox taskBox, Tache tache) {
        // Détecter le début du drag
        taskBox.setOnDragDetected(event -> {
            javafx.scene.input.Dragboard db = taskBox.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(String.valueOf(tache.getId_tache())); // On stocke l'ID de la tâche
            db.setContent(content);
            event.consume();
        });

        // Accepter le drag sur les colonnes
        for (VBox column : new VBox[]{todoTasks, inProgressTasks, doneTasks, canceledTasks}) {
            column.setOnDragOver(event -> {
                if (event.getGestureSource() != column && event.getDragboard().hasString()) {
                    event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                }
                event.consume();
            });

            // Gérer le drop et mettre à jour le statut
            column.setOnDragDropped(event -> {
                javafx.scene.input.Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    int taskId = Integer.parseInt(db.getString());

                    try {
                        // Trouver la tâche et mettre à jour son statut
                        Tache movedTask = tacheService.getTacheById(taskId);
                        if (movedTask != null) {
                            Tache.Status newStatus = getStatusFromVBox(column);

                            // Afficher l'alerte avant de changer le statut
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Confirmation de changement");
                            alert.setHeaderText("Voulez-vous vraiment changer le statut ?");
                            alert.setContentText("Cliquez sur OK pour confirmer.");

                            Optional<ButtonType> result = alert.showAndWait();

                            if (result.isPresent() && result.get() == ButtonType.OK) {
                                movedTask.setStatus(newStatus);
                                tacheService.update(movedTask); // Met à jour la BDD

                                // Recharger l'affichage
                                loadTaches();
                                success = true;
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                event.setDropCompleted(success); // Confirmer si l'opération a été réussie
                event.consume();
            });

        }
    }
    private Tache.Status getStatusFromVBox(VBox column) {
        if (column == todoTasks) return Tache.Status.TO_DO;
        if (column == inProgressTasks) return Tache.Status.IN_PROGRESS;
        if (column == doneTasks) return Tache.Status.DONE;
        if (column == canceledTasks) return Tache.Status.CANCELED;
        return Tache.Status.TO_DO; // Par défaut
    }

    @FXML
    private void openAjoutTachePopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjoutTache.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setTitle("Ajouter une tâche");
            popupStage.setScene(new Scene(root));

            popupStage.showAndWait();

            // Recharger les tâches après fermeture du popup
            loadTaches();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDeleteConfirmation(Tache tache) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DeleteTache.fxml"));
            Parent root = loader.load();
            DeleteTacheController controller = loader.getController();
            controller.setTache(tache);
            controller.setKanbanController(this);

            Stage deleteStage = new Stage();
            deleteStage.initStyle(StageStyle.UNDECORATED);
            deleteStage.setTitle("Suppression de Tâche");
            deleteStage.setScene(new Scene(root));
            deleteStage.initModality(Modality.APPLICATION_MODAL);
            deleteStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void refreshKanban() {
        loadTaches(); // Recharger toutes les tâches
    }


}
