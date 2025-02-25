package tn.esprit.Controllers;
import tn.esprit.Services.DeadLineNotification;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.Entities.Projet;
import tn.esprit.Services.ProjetService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javafx.scene.control.Alert.AlertType;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ProjectListWithCardsController {

    private ProjetService projetService = new ProjetService();
    private List<Projet> allProjets;

    @FXML
    private GridPane projectListContainer;  // Container pour les cartes de projets
    @FXML private TextField recherche;

    @FXML private ImageView notificationIcon;
    @FXML private Label notificationBadge;

    @FXML
    public void initialize() {
        loadProjects();
        loadNotificationIcon();

        // Vérifier si les composants existent avant d'agir dessus
        if (notificationBadge != null) {
            notificationBadge.setText("");
            notificationBadge.setVisible(false);
        }

        if (notificationIcon != null) {
            notificationIcon.setOnMouseClicked(event -> handleNotificationClick());
        }

        if (recherche != null) {
            recherche.textProperty().addListener((observable, oldValue, newValue) -> filterProjets(newValue));
        }

        // Vérifier immédiatement les deadlines au démarrage
        Platform.runLater(this::checkProjectDeadlines);

        // Planifier la mise à jour des notifications toutes les 20 secondes
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> Platform.runLater(this::checkProjectDeadlines), 1, 20, TimeUnit.SECONDS);

        // Ajouter un shutdown propre pour éviter les fuites de threads
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
    }


    private void loadProjects() {
        try {
            allProjets = projetService.showAll();  // Charger tous les projets
            updateProjectList(allProjets);  // Afficher tous les projets
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = recherche.getText();
        filterProjets(keyword);  // Appel de la méthode
    }

    private void filterProjets(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // Si la recherche est vide, afficher tous les projets
            updateProjectList(allProjets);
            return;
        }

        String searchKeyword = keyword.toLowerCase();

        // Filtrer les projets en fonction du mot-clé
        List<Projet> filteredProjets = allProjets.stream()
                .filter(projet -> {
                    String name = projet.getName() != null ? projet.getName().toLowerCase() : "";
                    String abbreviation = projet.getAbbreviation() != null ? projet.getAbbreviation().toLowerCase() : "";
                    String status = projet.getStatus() != null ? projet.getStatus().toString().toLowerCase() : "";

                    // Vérifier si le mot-clé est présent dans le nom, l'abréviation ou le statut du projet
                    return name.contains(searchKeyword) || abbreviation.contains(searchKeyword) || status.contains(searchKeyword);
                })
                .collect(Collectors.toList());

        // Mettre à jour l'affichage avec les projets filtrés
        updateProjectList(filteredProjets);
    }

    private void updateProjectList(List<Projet> projets) {
        projectListContainer.getChildren().clear();  // Vider le conteneur des projets

        int col = 0;
        int row = 0;

        // Ajouter chaque projet sous forme de carte dans le GridPane
        for (Projet projet : projets) {
            StackPane card = createCard(projet);

            // Ajouter la carte dans le GridPane à la position (col, row)
            projectListContainer.add(card, col, row);

            col++;
            if (col > 2) {  // Trois cartes par ligne
                col = 0;
                row++;
            }
        }
    }

    private StackPane createCard(Projet projet) {
        StackPane card = new StackPane();
        card.getStyleClass().add("project-card");

        String[] gradients = {"#fbaeaf, #f3e9bd", "#a3cee1, #b6efe7", "#dda1ff, #f4d1fd"};
        String randomGradient = gradients[new Random().nextInt(gradients.length)];

        card.setStyle("-fx-background-radius: 15px; -fx-background-color: linear-gradient(to bottom, " + randomGradient + "); -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 2, 2);");

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);

        Text nameText = new Text(projet.getName());
        nameText.getStyleClass().add("project-title");

        Text descText = new Text(projet.getDescription());
        descText.getStyleClass().add("project-description");

        Text statusText = new Text(projet.getStatus().toString());
        statusText.getStyleClass().add("project-status");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        // Ajout des icônes d'édition et de suppression
        ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/editIcon.png")));
        ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/deleteIcon.png")));
        ImageView eyeIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/eye.png")));

        // Redimensionnement des icônes
        editIcon.setFitHeight(40);
        editIcon.setFitWidth(40);
        deleteIcon.setFitHeight(40);
        deleteIcon.setFitWidth(40);
        eyeIcon.setFitHeight(40);
        eyeIcon.setFitWidth(40);

        // Changer le curseur quand la souris passe dessus
        editIcon.setStyle("-fx-cursor: hand;");
        deleteIcon.setStyle("-fx-cursor: hand;");

        // Actions lors du clic sur les icônes
        editIcon.setOnMouseClicked(event -> showEditPopup(projet));
        deleteIcon.setOnMouseClicked(event -> showDeleteConfirmation(projet));
        eyeIcon.setOnMouseClicked(event -> {
            afficherDetailsProjet(projet);
        });
        // Action pour afficher les détails du projet
        buttonBox.getChildren().addAll(editIcon, deleteIcon, eyeIcon);
        content.getChildren().addAll(nameText, descText, statusText, buttonBox);
        card.getChildren().add(content);

        return card;
    }

    private void showEditPopup(Projet projet) {
        // Ouvrir la fenêtre pour éditer un projet
        showPopup("/fxml/ModifierProjet.fxml", projet, "Modifier Projet");
    }

    private void showDeleteConfirmation(Projet projet) {
        // Ouvrir la fenêtre pour confirmer la suppression d'un projet
        showPopup("/fxml/DeleteProjet.fxml", projet, "Suppression de Projet");
    }

    private void showPopup(String fxmlFile, Projet projet, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Passer le projet aux contrôleurs correspondants
            if (loader.getController() instanceof ModifierProjetController) {
                ModifierProjetController controller = (ModifierProjetController) loader.getController();
                controller.setUserData(projet);
            } else if (loader.getController() instanceof DeleteProjetController) {
                DeleteProjetController controller = (DeleteProjetController) loader.getController();
                controller.setProjet(projet);
            }

            // Créer et afficher la fenêtre modale
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadProjects();  // Rafraîchir la liste des projets après modification
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAjoutProjetPopup() {
        try {
            // Ouvrir la fenêtre pour ajouter un projet
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjoutProjet.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setTitle("Ajouter un projet");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
            loadProjects();  // Rafraîchir les projets après l'ajout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void afficherDetailsProjet(Projet projet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DetailsProjet.fxml"));
            Parent root = loader.load();

            DetailsProjetController controller = loader.getController();
            controller.setProjet(projet);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du Projet");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void checkProjectDeadlines() {
        try {
            allProjets = projetService.showAll(); // Recharger les projets

            // Filtrer les projets en retard
            long notificationCount = allProjets.stream()
                    .filter(projet -> projet.getEnd_date() != null && projet.getEnd_date().isBefore(LocalDate.now()))
                    .count();

            System.out.println("Projets en retard : " + notificationCount); // Vérifier la valeur
            updateNotificationBadge((int) notificationCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    @FXML
    private void handleNotificationClick() {
        DeadLineNotification.checkProjectDeadlines();
    }
    private void loadNotificationIcon() {
        String iconPath = "/images/notif.png";
        Image image = new Image(getClass().getResourceAsStream(iconPath));
        notificationIcon.setImage(image);
    }

    private void updateNotificationBadge(int count) {
        Platform.runLater(() -> {
            System.out.println("Mise à jour du badge avec : " + count);
            if (count > 0) {
                notificationBadge.setText(String.valueOf(count));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setText("");
                notificationBadge.setVisible(false);
            }
        });
    }

}

