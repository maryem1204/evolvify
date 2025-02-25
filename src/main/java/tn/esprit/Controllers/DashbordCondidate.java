package tn.esprit.Controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;

public class  DashbordCondidate  {

    @FXML
    private TableColumn<?, ?> DatedeNaissance;

    @FXML
    private TableColumn<?, ?> DatedePostulation;

    @FXML
    private TableColumn<?, ?> Photo;

    @FXML
    private ComboBox<?> comboTrie;

    @FXML
    private TableColumn<?, ?> cv;

    @FXML
    private TableColumn<?, ?> email;

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
    private TableColumn<?, ?> nom;

    @FXML
    private TableColumn<?, ?> numdetele;

    @FXML
    private TableColumn<?, ?> prenom;

    @FXML
    private TextField recherche;

    @FXML
    private AnchorPane tableblanche;

    @FXML
    private TableView<?> tabledaffichage;

    @FXML
    private AnchorPane tableofthings;

    @FXML
    void handleSearch(ActionEvent event) {

    }


}

