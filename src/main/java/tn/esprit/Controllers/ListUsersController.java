package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
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
    private TextField searchField; // Ajout du champ de recherche

    private ObservableList<Utilisateur> users = FXCollections.observableArrayList();
    private ObservableList<Utilisateur> filteredUsers = FXCollections.observableArrayList();

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

            // Initialiser la liste filtrée avec tous les utilisateurs
            filteredUsers.setAll(users);

            // Ajouter un listener au champ de recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> filterUsers(newValue));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔍 Méthode appelée par le champ de recherche dans le FXML
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        filterUsers(keyword);
    }

    // 🔎 Méthode pour filtrer les utilisateurs
    private void filterUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            employeeTable.setItems(users);
            return;
        }

        final String searchKeyword = keyword.toLowerCase();

        List<Utilisateur> filteredList = users.stream()
                .filter(user -> user.getFirstname().toLowerCase().contains(searchKeyword)
                        || user.getLastname().toLowerCase().contains(searchKeyword)
                        || user.getEmail().toLowerCase().contains(searchKeyword))
                .collect(Collectors.toList());

        filteredUsers.setAll(filteredList);
        employeeTable.setItems(filteredUsers);
    }

    @FXML
    private void showAddEmployeePopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterUser.fxml"));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setTitle("Ajouter Employé");

            Scene scene = new Scene(root);
            popupStage.setScene(scene);

            popupStage.showAndWait();

            // Mettre à jour la liste après l'ajout d'un employé
            refreshUserList();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔄 Méthode pour recharger la liste après un ajout
    private void refreshUserList() {
        users.clear();
        try {
            UtilisateurService userService = new UtilisateurService();
            List<Utilisateur> userList = userService.showAll();

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
