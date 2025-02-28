package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.CandidateService;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListCandidateController {

    @FXML
    private TableColumn<Utilisateur, String> prenom;
    @FXML
    private TableColumn<Utilisateur, String> nom;
    @FXML
    private TableColumn<Utilisateur, String> email;
    @FXML
    private TableColumn<Utilisateur, byte[]> Photo;
    @FXML
    private TableColumn<Utilisateur, String> DatedeNaissance;
    @FXML
    private TableColumn<Utilisateur, String> DatedePostulation;
    @FXML
    private TableColumn<Utilisateur, byte[]> cv;
    @FXML
    private TableColumn<Utilisateur, String> numdetele;
    @FXML
    private TableColumn<Utilisateur, String> gender;

    @FXML
    private ToggleButton facondetrie;

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
    private TextField recherche;

    @FXML
    private AnchorPane tableblanche;

    @FXML
    private TableView<Utilisateur> tabledaffichage;

    @FXML
    private AnchorPane tableofthings;

    private static final Logger logger = Logger.getLogger(Dashboard.class.getName());



    private final CandidateService CandidateService  = new CandidateService();
    @FXML
    public void initialize() {
        // Initialize columns with appropriate getter methods
        prenom.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        nom.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));

        DatedeNaissance.setCellValueFactory(new PropertyValueFactory<>("birthdayDate"));
        DatedePostulation.setCellValueFactory(new PropertyValueFactory<>("joiningDate"));
        //cv.setCellValueFactory(new PropertyValueFactory<>("uploaded_cv"));
        numdetele.setCellValueFactory(new PropertyValueFactory<>("num_tel"));
        gender.setCellValueFactory(new PropertyValueFactory<>("gender"));


        // Any additional initialization code, such as setting the table's data, can be added here.
        loadOffres();

    }
    @FXML
    private void loadOffres() {
        try {

            List<Utilisateur> listCondidate = CandidateService.showAll();  // Cette méthode lance une SQLException
            System.out.println("Nombre de candidats récupérés : " + listCondidate.size());
            ObservableList<Utilisateur> listCondidateObse = FXCollections.observableArrayList(listCondidate);
            // Set the items in the TableView
            tabledaffichage.setItems(listCondidateObse);
            System.out.println("bonjour");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des offres", e);
            afficherAlerte("Erreur", "Impossible de charger les offres.");
            System.out.println("au revoir");
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

