package tn.esprit.Controllers;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javafx.scene.control.Alert.AlertType;
import tn.esprit.Utils.SessionManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class ProjectListWithCardsController {

    private ProjetService projetService = new ProjetService();
    private List<Projet> allProjets;

    @FXML private GridPane projectListContainer;  // Container pour les cartes de projets
    @FXML private TextField recherche;
    @FXML private Button btnAjouterProjet;
    @FXML private ImageView notificationIcon;
    @FXML private Label notificationBadge;

    @FXML public void initialize() {
        loadProjects();
        loadNotificationIcon();


        if (btnAjouterProjet != null) {
            Utilisateur utilisateur = SessionManager.getUtilisateurConnecte();
            if (utilisateur != null && utilisateur.getRole() == Role.EMPLOYEE) {
                btnAjouterProjet.setDisable(true);
                btnAjouterProjet.setOpacity(0.5);
            }
        }


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
            // Get the currently logged-in user
            Utilisateur utilisateurConnecte = SessionManager.getUtilisateurConnecte();

            if (utilisateurConnecte == null) {
                // If no user is logged in, show an error or empty list
                allProjets = new ArrayList<>();
                updateProjectList(allProjets);
                return;
            }

            // Check if the user is an admin (show all projects)
            if (utilisateurConnecte.getRole() == Role.EMPLOYEE) {
                allProjets = projetService.showAll();
            } else {
                // For other roles, get projects assigned to the current user
                allProjets = projetService.getProjectsByEmployee(utilisateurConnecte.getId_employe());
            }

            updateProjectList(allProjets);
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the error appropriately, perhaps show an alert
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de chargement des projets");
            alert.setContentText("Impossible de charger les projets. Veuillez réessayer.");
            alert.showAndWait();
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
            if (col > 3) {  // Trois cartes par ligne
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

        // Appliquer stripHtmlTags au nom du projet
        Text nameText = new Text(stripHtmlTags(projet.getName()));
        nameText.getStyleClass().add("project-title");

        // Appliquer stripHtmlTags à la description du projet
        Text descText = new Text(stripHtmlTags(projet.getDescription()));
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


        // Vérifier le rôle de l'utilisateur connecté
        Utilisateur utilisateur = SessionManager.getUtilisateurConnecte();
        boolean isEmployee = false;

        // Vérifier si l'utilisateur est un employé
        if (utilisateur != null && utilisateur.getRole() == Role.EMPLOYEE) {
            isEmployee = true;
        }

        // Appliquer les styles et les actions en fonction du rôle
        if (isEmployee) {
            // Désactiver les icônes pour les employés
            editIcon.setOpacity(0.5);
            deleteIcon.setOpacity(0.5);
            editIcon.setStyle("-fx-cursor: default;");
            deleteIcon.setStyle("-fx-cursor: default;");
            // Ne pas ajouter d'événements de clic pour les employés
        } else {
            // Actions pour les administrateurs et autres rôles
            editIcon.setStyle("-fx-cursor: hand;");
            deleteIcon.setStyle("-fx-cursor: hand;");
            editIcon.setOnMouseClicked(event -> showEditPopup(projet));
            deleteIcon.setOnMouseClicked(event -> showDeleteConfirmation(projet));
        }


        // Instead, enable all buttons regardless of role
        editIcon.setStyle("-fx-cursor: hand;");
        deleteIcon.setStyle("-fx-cursor: hand;");
        editIcon.setOnMouseClicked(event -> showEditPopup(projet));
        deleteIcon.setOnMouseClicked(event -> showDeleteConfirmation(projet));

        // L'icône pour voir les détails reste toujours active
        eyeIcon.setStyle("-fx-cursor: hand;");
        eyeIcon.setOnMouseClicked(event -> {
            afficherDetailsProjet(projet);
        });

        // Ajouter toutes les icônes à la boîte de boutons
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

    @FXML
    private void handleNotificationClick() {
        DeadLineNotification.checkProjectDeadlines();
    }
    private void loadNotificationIcon() {
        String iconPath = "/images/notif.png";
        Image image = new Image(getClass().getResourceAsStream(iconPath));
        notificationIcon.setImage(image);
    }

    private void checkProjectDeadlines() {
        try {
            allProjets = projetService.showAll(); // Recharger les projets

            // Filtrer les projets ayant une deadline demain
            long notificationCount = allProjets.stream()
                    .filter(projet -> projet.getEnd_date() != null && projet.getEnd_date().isEqual(LocalDate.now().plus(1, ChronoUnit.DAYS)))
                    .count();

            //System.out.println("Projets avec deadline demain : " + notificationCount); // Vérifier la valeur
            updateNotificationBadge((int) notificationCount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void updateNotificationBadge(int count) {
        Platform.runLater(() -> {
            //System.out.println("Mise à jour du badge avec : " + count);
            if (count > 0) {
                notificationBadge.setText(String.valueOf(count));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setText("");
                notificationBadge.setVisible(false);
            }
        });
    }
    public static String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "");
    }
}