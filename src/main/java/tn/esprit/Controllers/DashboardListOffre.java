package tn.esprit.Controllers;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
public class DashboardListOffre {
    @FXML
    private TableColumn<?, ?> colDesc;

    @FXML
    private TableColumn<?, ?> colTitre;

    @FXML
    private TableColumn<?, ?> coldatePub;

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
    private TableColumn<?, ?> status;

    @FXML
    private AnchorPane tableblanche;

    @FXML
    private TableView<?> tabledeliste;

    @FXML
    private AnchorPane tableofthings;

}
