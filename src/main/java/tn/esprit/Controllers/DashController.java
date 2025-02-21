package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

public class DashController {

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnConges;
    @FXML
    private Button btnRecrutements;
    @FXML
    private Button btnProjets;
    @FXML
    private Button btnTransports;
    @FXML
    private ImageView logo;
    @FXML
    private Label username;

    @FXML
    private ImageView userIcon;
    @FXML
    private HBox userBox;

    private List<Button> sidebarButtons;

    @FXML
    private VBox sidebar; // Assure-toi que c'est bien l'élément FXML

    public VBox getSidebar() {
        return sidebar;
    }


    @FXML
    public void initialize() {
        Image img = new Image(getClass().getResource("/images/profileicon.png").toExternalForm(), true);
        userIcon.setImage(img);


        sidebarButtons = List.of(btnDashboard, btnConges, btnRecrutements, btnProjets, btnTransports);

        for (Button button : sidebarButtons) {
            button.setOnAction(event -> setActiveButton(button));
        }
        String userConnected = "Meriem Sassi";
        username.setText(userConnected);

    }


    private void setActiveButton(Button selectedButton) {
        for (Button button : sidebarButtons) {
            button.getStyleClass().remove("selected");
        }
        selectedButton.getStyleClass().add("selected");
    }
}

