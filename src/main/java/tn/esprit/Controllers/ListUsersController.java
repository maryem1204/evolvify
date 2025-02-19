package tn.esprit.Controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    @FXML
    private Pagination pagination; // Ajout de la pagination

    private ObservableList<Utilisateur> users = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 10;

    private static ListUsersController instance;

    public ListUsersController() {
        instance = this;
    }

    public static ListUsersController getInstance() {
        return instance;
    }
    @FXML
    public void initialize() {
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox.setMargin(searchField, new Insets(0, 0, 20, 0)); // 20px de marge en bas

        loadUsers();
        configureTable();
        pagination.setPageCount((int) Math.ceil((double) users.size() / ROWS_PER_PAGE));
        pagination.setCurrentPageIndex(0);
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updateTable(newIndex.intValue()));
        updateTable(0);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
    }

    private void loadUsers() {
        try {
            UtilisateurService userService = new UtilisateurService();
            users.clear();
            List<Utilisateur> userList = userService.showAll();
            for (Utilisateur user : userList) {
                byte[] profilePhoto = userService.getProfilePhoto(user.getId_employe());
                user.setProfilePhoto(profilePhoto);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void configureTable() {
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

        addActionsColumn();
    }

    private void updateTable(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, users.size());
        employeeTable.setItems(FXCollections.observableArrayList(users.subList(fromIndex, toIndex)));
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Utilisateur, Void>() {
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/editIcon.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/deleteIcon.png")));
            private final HBox hbox = new HBox(10, editIcon, deleteIcon);

            {
                editIcon.setFitWidth(40);
                editIcon.setFitHeight(40);
                deleteIcon.setFitWidth(40);
                deleteIcon.setFitHeight(40);

                editIcon.setOnMouseClicked(event -> showEditPopup(getTableView().getItems().get(getIndex())));
                deleteIcon.setOnMouseClicked(event -> confirmDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void showEditPopup(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editUser.fxml"));
            Parent root = loader.load();
            EditUserController controller = loader.getController();
            controller.setUserData(user);
            controller.setListUsersController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshUserList();
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
                new UtilisateurService().delete(user);
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

            refreshUserList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshUserList() {
        loadUsers();
        pagination.setPageCount((int) Math.ceil((double) users.size() / ROWS_PER_PAGE));
        updateTable(pagination.getCurrentPageIndex());
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase();

        if (keyword.isEmpty()) {
            refreshUserList();
            pagination.setCurrentPageIndex(0); // revenir à la liste complète et à la première page de pagination
        } else {
            List<Utilisateur> filteredList = users.stream()
                    .filter(u -> (u.getFirstname() + " " + u.getLastname()).toLowerCase().contains(keyword)
                            || u.getEmail().contains(keyword)
                            || u.getRole().toString().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());

            employeeTable.setItems(FXCollections.observableArrayList(filteredList));
        }
    }

}
