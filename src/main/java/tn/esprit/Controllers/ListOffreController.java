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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Entities.Offre;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.OffreService;
import tn.esprit.Utils.MyDataBase;

import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ListOffreController {

    @FXML
    private TableColumn<Offre, String> colDesc;

    @FXML
    private TableColumn<Offre, String> colTitre;

    @FXML
    private TableColumn<Offre, String> coldateExp;

    @FXML
    private TableColumn<Offre, String> coldatePub;

    @FXML
    private TableColumn<Offre, String> status;

    @FXML
    private Button btnTrier;

    @FXML
    private Button gestcong;

    @FXML
    private Button gestpro;

    @FXML
    private Button gestran;

    @FXML
    private Button gestrec;

    @FXML
    private TextField recherche;

    @FXML
    private AnchorPane gests;

    @FXML
    private Button gestutiliser;

    @FXML
    private ToggleButton facondetrie;

    @FXML
    private ComboBox<String> comboTrie;

    @FXML
    private AnchorPane logo;

    @FXML
    private Button logout;

    @FXML
    private Label logoutlabel;


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
    private Connection cnx = MyDataBase.getInstance().getCnx();
    private ObservableList<Offre> filteredOffreList = FXCollections.observableArrayList();
    private final OffreService offreService = new OffreService();
    ObservableList<Offre> offresList = FXCollections.observableArrayList();
    private Offre offre;  // L'offre qui sera modifiée
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
        recherche.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        comboTrie.setOnAction(event -> handleTriOffres());
        // Charger les offres
        loadOffre();
        offres.setAll(offresList);


    }


    /*@FXML
    void loadOffres(ActionEvent event) {
        loadOffre();
    }*/
    @FXML
    void handleAjouterOffre(ActionEvent event) throws IOException {
        // Charger le formulaire d'ajout d'offre (AddOffre.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddOffre.fxml"));
        Parent root = loader.load();

        // Créer une nouvelle scène
        Scene scene = new Scene(root);
        //scene.getStylesheets().add(getClass().getResource("/tn/esprit/Styles/formulCond.css").toExternalForm());

        // Créer une nouvelle fenêtre (Stage)
        Stage newStage = new Stage();
        newStage.setScene(scene);
        newStage.setTitle("Ajouter une Offre");
        newStage.show();

    }


    @FXML
    private void loadOffre() {
        offresList.clear();
       // System.out.println("hi");
        String req = "SELECT `id_offre`, `titre`, `description`, `date_publication`, `date_expiration`, `status` FROM `offre`";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            //System.out.println("bonjour");
            while (rs.next()) {
                int idOffre = rs.getInt("id_offre"); // Important : on récupère l'ID même si on ne l'affiche pas
                String titre = rs.getString("titre");
                String description = rs.getString("description");
                Date datePublication = rs.getDate("date_publication");
                Date dateExpiration = rs.getDate("date_expiration");
                Offre.Status status = Offre.Status.valueOf(rs.getString("status"));

                Offre offre = new Offre(idOffre, titre, description, datePublication, dateExpiration, status);
                offresList.add(offre);
            }
            offres.setAll(offresList);
            tabledaffichage.setItems(offresList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void addActionsColumn() {
        TableColumn<Offre, Void> actionColumn = new TableColumn<>("Actions");

        actionColumn.setCellFactory(param -> new TableCell<Offre, Void>() {
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/editIcon.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/deleteIcon.png")));
            private final HBox hbox = new HBox(10, editIcon, deleteIcon);

            {
                // Définition des tailles des icônes
                editIcon.setFitWidth(25);
                editIcon.setFitHeight(25);
                deleteIcon.setFitWidth(25);
                deleteIcon.setFitHeight(25);

                // Appliquer un style "main" au survol
                editIcon.setStyle("-fx-cursor: hand;");
                deleteIcon.setStyle("-fx-cursor: hand;");

                // Gestion de l'édition
                editIcon.setOnMouseClicked(event -> {
                    Offre offre = getTableRow().getItem();
                    if (offre != null) {
                        System.out.println("Modification de l'offre: " + offre);
                        showEditPopup(offre);
                        reloadTableData();
                    } else {
                        System.err.println("Aucune offre sélectionnée pour l'édition.");
                    }
                });

                // Gestion de la suppression
                deleteIcon.setOnMouseClicked(event -> {
                    Offre offre = getTableRow().getItem();
                    if (offre != null) {
                        System.out.println("Suppression de l'offre: " + offre);
                        confirmDelete(offre);
                    } else {
                        System.err.println("Aucune offre sélectionnée pour la suppression.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        // Ajout de la colonne dans la TableView
        tabledaffichage.getColumns().add(actionColumn);
    }


    private void showEditPopup(Offre offre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierOffre.fxml"));
            Parent root = loader.load();
            ModifierOffre controller = loader.getController();
            controller.setOffre(offre); // Passer l'offre sélectionnée

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

        if (offre == null || offre.getIdOffre() <= 0) {
            afficherAlerte("Erreur", "Aucune offre valide sélectionnée pour suppression.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette offre ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                OffreService os = new OffreService();
                int deletedRows = os.delete(offre);
                if (deletedRows > 0) {
                    loadOffre(); // Met à jour le TableView après suppression
                    afficherAlerte("Succès", "Offre supprimée avec succès !");
                } else {
                    afficherAlerte("Erreur", "L'offre n'a pas pu être supprimée. Vérifiez son existence.");
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Erreur lors de la suppression de l'offre", e);
                afficherAlerte("Erreur", "Impossible de supprimer l'offre.");
            }
        }
    }


    // Méthode pour afficher une alerte
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void reloadTableData() {
        // Créer une nouvelle ObservableList
        ObservableList<Offre> offreList = FXCollections.observableArrayList();

        // Créer une instance de OffreService
        OffreService offreService = new OffreService();

        try {
            System.out.println("reload depart");
            // Récupérer les offres actualisées depuis la base de données
            List<Offre> offres = offreService.showAll(); // Récupérer les offres

            if (offres != null && !offres.isEmpty()) {
                // Ajouter les offres dans l'ObservableList
                offreList.setAll(offres);
            } else {
                // Si aucune offre n'est trouvée, afficher une alerte
                showAlert(Alert.AlertType.WARNING, "Aucune offre", "Aucune offre disponible.");
            }
        } catch (SQLException e) {
            // Gérer l'exception en cas d'erreur lors de la récupération des données
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des offres.");
            e.printStackTrace();
            return;
        }

        // Mettre à jour la TableView avec la nouvelle ObservableList
        tabledaffichage.setItems(offreList);

        // Forcer un rafraîchissement de la TableView
        tabledaffichage.refresh();
    }
    @FXML
    private void handleSearch() {
        // Récupère le mot-clé saisi dans le champ de recherche
        String keyword = recherche.getText();

        // Appelle la méthode de filtrage avec le mot-clé
        filterOffres(keyword);

    }
    @FXML
    private void filterOffres(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            tabledaffichage.setItems(offresList);
            return;
        }

        String searchKeyword = keyword.toLowerCase();
        System.out.println(" keyword== " +  keyword);
        // Filtrer la liste des offres
        List<Offre> filteredList = offres.stream()
                .filter(offre -> offre.getTitre().toLowerCase().contains(searchKeyword)
                        || offre.getDescription().toLowerCase().contains(searchKeyword)
                        || String.valueOf(offre.getDatePublication()).contains(searchKeyword)
                        || String.valueOf(offre.getDateExpiration()).contains(searchKeyword)
                        || (offre.getStatus() != null && offre.getStatus().name().toLowerCase().contains(searchKeyword)))
                .collect(Collectors.toList());

        filteredOffreList.setAll(filteredList);
        tabledaffichage.setItems(filteredOffreList);

    }
    @FXML
    private void handleTriOffres() {
        facondetrie.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Vérifie l'état du ToggleButton
            boolean ascendant = newValue; // Si sélectionné, tri ascendant, sinon tri descendant

            // Récupère le critère de tri choisi par l'utilisateur dans le ComboBox
            String critere = comboTrie.getValue();

            if (critere != null) {
                trierOffres(critere, ascendant); // Appelle la méthode de tri avec le critère et l'ordre choisi
            }
        });

    }

    private void trierOffres(String critere, boolean ascendant) {
        if (critere.equals("Titre")) {
            Collections.sort(offresList, (o1, o2) -> {
                String titre1 = o1.getTitre().trim(); // Enlever les espaces avant et après
                String titre2 = o2.getTitre().trim(); // Enlever les espaces avant et après

                if (ascendant) {
                    return titre1.compareTo(titre2);
                } else {
                    return titre2.compareTo(titre1);
                }
            });
        } else if (critere.equals("Date_Publication")) {
            Collections.sort(offresList, (o1, o2) -> {
                if (ascendant) {
                    return o1.getDatePublication().compareTo(o2.getDatePublication());
                } else {
                    return o2.getDatePublication().compareTo(o1.getDatePublication());
                }
            });
        } else if (critere.equals("Date_Expiration")) {
            Collections.sort(offresList, (o1, o2) -> {
                if (ascendant) {
                    return o1.getDateExpiration().compareTo(o2.getDateExpiration());
                } else {
                    return o2.getDateExpiration().compareTo(o1.getDateExpiration());
                }
            });
        } else if (critere.equals("Status")) {
            Collections.sort(offresList, (o1, o2) -> {
                if (ascendant) {
                    return o1.getStatus().compareTo(o2.getStatus());
                } else {
                    return o2.getStatus().compareTo(o1.getStatus());
                }
            });
        }
    }




}

