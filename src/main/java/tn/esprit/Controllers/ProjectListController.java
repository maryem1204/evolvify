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
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.ProjetService;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectListController {
    @FXML private TableView<Projet> projectTable;
    @FXML private TableColumn<Projet, String> colName;
    @FXML private TableColumn<Projet, String> colAbbreviation;
    @FXML private TableColumn<Projet, String> colStatus;
    @FXML private TableColumn<Projet, Date> colEndDate;
    @FXML private TableColumn<Projet, Date> colStarterDate;
    @FXML private TableColumn<Projet, String> colManagerId;  // Afficher le nom au lieu de l'ID
    @FXML private TableColumn<Projet, String> colUploadedFiles;
    @FXML private TableColumn<Projet, Void> colActions;
    @FXML private Button createProjectButton;
    @FXML
    private TextField recherche;

    private ObservableList<Projet> projets = FXCollections.observableArrayList();
    private ObservableList<Projet> filteredProjet = FXCollections.observableArrayList();

    private ProjetService projectService = new ProjetService();
    private Map<Integer, String> userMap = new HashMap<>(); // ✅ Stocke les ID -> Noms

    private static ProjectListController instance;

    public ProjectListController() {
        instance = this;
    }

    public static ProjectListController getInstance() {
        return instance;
    }

    public void initialize() throws SQLException {
        // Charger les employés AVANT de charger les projets
        loadUsers();

        // 🔹 Mapper les colonnes aux attributs correspondants
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAbbreviation.setCellValueFactory(new PropertyValueFactory<>("abbreviation"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("end_date"));
        colStarterDate.setCellValueFactory(new PropertyValueFactory<>("starter_at"));

        // 🔹 Personnaliser l'affichage du nom de l'employé avec firstName et lastName
        colManagerId.setCellValueFactory(cellData -> {
            int userId = cellData.getValue().getId_employe();
            String fullName = userMap.getOrDefault(userId, "Inconnu"); // On récupère le nom complet
            return new javafx.beans.property.SimpleStringProperty(fullName);
        });


        // Charger les projets après avoir chargé les utilisateurs
        loadProjects();

        // Ajouter les colonnes d'actions
        addActionsColumn();

        // Mettre à jour la table lorsque l'utilisateur tape dans la barre de recherche
        recherche.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
    }


    @FXML
    private void handleSearch() {
        String keyword = recherche.getText();
        filterProjets(keyword);
    }

    private void filterProjets(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            projectTable.setItems(projets);
            return;
        }

        String searchKeyword = keyword.toLowerCase();

        List<Projet> filteredList = projets.stream()
                .filter(projet -> {
                    String name = projet.getName() != null ? projet.getName().toLowerCase() : "";
                    String abbreviation = projet.getAbbreviation() != null ? projet.getAbbreviation().toLowerCase() : "";
                    String status = projet.getStatus() != null ? projet.getStatus().toString().toLowerCase() : "";

                    return name.contains(searchKeyword) || abbreviation.contains(searchKeyword) || status.contains(searchKeyword);
                })
                .collect(Collectors.toList());

        filteredProjet.setAll(filteredList);
        projectTable.setItems(filteredProjet);
    }



    private void loadProjects() throws SQLException {
        List<Projet> projetsList = projectService.showAll();
        projets.setAll(projetsList); // Mise à jour de la liste observable
        projectTable.setItems(projets); // Mise à jour de la table
    }


    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Projet, Void>() {
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/editIcon.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/deleteIcon.png")));
            private final HBox hbox = new HBox(10, editIcon, deleteIcon);

            {
                // Taille des icônes
                editIcon.setFitHeight(20);
                editIcon.setFitWidth(20);
                deleteIcon.setFitHeight(20);
                deleteIcon.setFitWidth(20);

                // Ajout du curseur main pour montrer qu'ils sont cliquables
                editIcon.setStyle("-fx-cursor: hand;");
                deleteIcon.setStyle("-fx-cursor: hand;");

                // Actions sur clic des icônes
                editIcon.setOnMouseClicked(event -> {
                    Projet projet = getTableView().getItems().get(getIndex());
                    showEditPopup(projet);
                });

                deleteIcon.setOnMouseClicked(event -> {
                    Projet projet = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(projet);
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


    private void showDeleteConfirmation(Projet projet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DeleteProjet.fxml"));
            Parent root = loader.load();
            DeleteProjetController controller = loader.getController();
            controller.setProjet(projet);
            controller.setProjectListController(this); // ✅ Passer l'instance actuelle

            Stage deleteStage = new Stage();
            deleteStage.initStyle(StageStyle.UNDECORATED);
            deleteStage.setTitle("Suppression de Projet");
            deleteStage.setScene(new Scene(root));
            deleteStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void showEditPopup(Projet projet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierProjet.fxml"));
            Parent root = loader.load();
            ModifierProjetController controller = loader.getController();
            controller.setUserData(projet);
            controller.setListUsersController(this);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshProjetList();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void openAjoutProjetPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjoutProjet.fxml"));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);// Empêche l'interaction avec la fenêtre principale
            popupStage.setTitle("Ajouter un projet");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
            refreshProjetList();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshProjetList() {
        try {
            List<Projet> projetList = projectService.showAll();
            projets.setAll(projetList); // ✅ Mise à jour directe de la liste observable
            projectTable.setItems(projets);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadUsers() throws SQLException {
        UtilisateurService utilisateurService = new UtilisateurService();
        List<Utilisateur> users = utilisateurService.showAll(); // Récupérer tous les utilisateurs

        userMap.clear(); // Vider la map avant de la remplir
        for (Utilisateur user : users) {
            String fullName = user.getFirstname() + " " + user.getLastname(); // Concaténer prénom + nom
            userMap.put(user.getId_employe(), fullName);
        }

        System.out.println("User Map Loaded: " + userMap); // Vérifier le contenu dans la console
    }





}
