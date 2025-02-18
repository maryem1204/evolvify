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
import javafx.scene.layout.HBox;  // ✅ Ajout de l'importation manquante
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.input.MouseEvent;


public class ListUsersController {

    @FXML
    private TableView<Utilisateur> employeeTable;
    @FXML
    private TableColumn<Utilisateur, ImageView> colProfile;
    @FXML
    private TableColumn<Utilisateur, String> colFirstName;
    @FXML
    private TableColumn<Utilisateur, String> colLastName;
    @FXML
    private TableColumn<Utilisateur, String> colEmail;
    @FXML
    private TableColumn<Utilisateur, String> colBirthday;
    @FXML
    private TableColumn<Utilisateur, String> colJoiningDate;
    @FXML
    private TableColumn<Utilisateur, String> colRole;
    @FXML
    private TableColumn<Utilisateur, String> colNumTel;
    @FXML
    private TableColumn<Utilisateur, Integer> colCongeRestant;
    @FXML
    private TableColumn<Utilisateur, Integer> colTtRestants;
    @FXML
    private TableColumn<Utilisateur, Void> colActions;
    @FXML
    private TextField searchField;

    private ObservableList<Utilisateur> users = FXCollections.observableArrayList();
    private ObservableList<Utilisateur> filteredUsers = FXCollections.observableArrayList();

    private static ListUsersController instance;

    public ListUsersController() {
        instance = this;
    }

    public static ListUsersController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        try {
            UtilisateurService userService = new UtilisateurService();
            List<Utilisateur> userList = userService.showAll();

            for (Utilisateur user : userList) {
                byte[] profilePhoto = userService.getProfilePhoto(user.getId_employe());
                user.setProfilePhoto(profilePhoto);
                users.add(user);
            }

            employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            colProfile.setCellValueFactory(new PropertyValueFactory<>("profilePhotoImageView"));
            colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstname"));
            colLastName.setCellValueFactory(new PropertyValueFactory<>("lastname"));
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            colBirthday.setCellValueFactory(new PropertyValueFactory<>("birthdayDate"));
            colJoiningDate.setCellValueFactory(new PropertyValueFactory<>("joiningDate"));
            colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
            colNumTel.setCellValueFactory(new PropertyValueFactory<>("num_tel"));
            colCongeRestant.setCellValueFactory(new PropertyValueFactory<>("congeRestant"));
            colTtRestants.setCellValueFactory(new PropertyValueFactory<>("ttRestants"));

            employeeTable.setItems(users);

            // 🔹 Ajustement dynamique des colonnes
            colProfile.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.10));
            colFirstName.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.12));
            colLastName.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.12));
            colEmail.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.18));
            colBirthday.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.10));
            colJoiningDate.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.10));
            colRole.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.08));
            colNumTel.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.08));
            colCongeRestant.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.06));
            colTtRestants.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.06));
            colActions.prefWidthProperty().bind(employeeTable.widthProperty().multiply(0.10));

            addActionsColumn();

            searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Correction de la méthode handleSearch() et ajout de filterUsers()
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        filterUsers(keyword);
    }

    private void filterUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            employeeTable.setItems(users);
            return;
        }

        String searchKeyword = keyword.toLowerCase();

        List<Utilisateur> filteredList = users.stream()
                .filter(utilisateur -> (utilisateur.getFirstname() + " " + utilisateur.getLastname()).toLowerCase().contains(searchKeyword)
                        || utilisateur.getFirstname().toLowerCase().contains(searchKeyword)
                        || utilisateur.getLastname().toLowerCase().contains(searchKeyword)
                        || utilisateur.getEmail().contains(searchKeyword)
                        || utilisateur.getRole().toString().toLowerCase().contains(searchKeyword))
                .collect(Collectors.toList());

        filteredUsers.setAll(filteredList);
        employeeTable.setItems(filteredUsers);
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Utilisateur, Void>() {
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/editIcon.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/deleteIconn.png")));
            private final HBox hbox = new HBox(10, editIcon, deleteIcon);

            {
                // Taille des icônes
                editIcon.setFitWidth(20);
                editIcon.setFitHeight(20);
                deleteIcon.setFitWidth(20);
                deleteIcon.setFitHeight(20);

                // Style au survol
                editIcon.setOnMouseEntered(e -> editIcon.setOpacity(0.7));
                editIcon.setOnMouseExited(e -> editIcon.setOpacity(1.0));
                deleteIcon.setOnMouseEntered(e -> deleteIcon.setOpacity(0.7));
                deleteIcon.setOnMouseExited(e -> deleteIcon.setOpacity(1.0));

                // Actions des icônes
                editIcon.setOnMouseClicked((MouseEvent event) -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    showEditPopup(user);
                });

                deleteIcon.setOnMouseClicked((MouseEvent event) -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    confirmDelete(user);
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

    private void showEditPopup(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editUser.fxml"));
            Parent root = loader.load();

            // 🔹 Récupérer le contrôleur du formulaire d'édition
            EditUserController controller = loader.getController();
            controller.setUserData(user);
            controller.setListUsersController(this); // ✅ Passer la référence de ListUsersController

            // 🔹 Afficher la fenêtre de modification sans barre de titre
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED); // ❌ Supprime toute la barre de titre
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void confirmDelete(Utilisateur user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cet utilisateur ?");
        alert.setContentText(user.getFirstname() + " " + user.getLastname());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                new UtilisateurService().delete(user);  // ✅ Correction de l'appel de delete()
                refreshUserList();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void showAddEmployeePopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouterUser.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshUserList(); // Mettre à jour la liste après l'ajout
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void refreshUserList() {
        try {
            UtilisateurService userService = new UtilisateurService();
            List<Utilisateur> userList = userService.showAll();

            users.clear();
            for (Utilisateur user : userList) {
                byte[] profilePhoto = userService.getProfilePhoto(user.getId_employe());
                user.setProfilePhoto(profilePhoto);
                users.add(user);
            }

            employeeTable.setItems(users);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
