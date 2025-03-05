package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import tn.esprit.Entities.Conge;
import tn.esprit.Entities.Tt;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Entities.Statut;
import tn.esprit.Services.CongeService;
import tn.esprit.Services.TtService;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DashboardCongeRh {

    @FXML
    private TableView<Conge> leaveTable;

    @FXML
    private TableColumn<Conge, String> colEmployee, colType;
    @FXML
    private TableColumn<Conge, String> colStartDate, colEndDate;
    @FXML
    private TableColumn<Conge, Integer> colDays;
    @FXML
    private TableColumn<Conge, String> colReason, colStatus, colActions;

    @FXML
    private TableView<Tt> leaveTt;  // Déclaration de la TableView pour TT
    @FXML
    private TableColumn<Tt, String> colEmployeeTT, colStartDateTT, colEndDateTT, colStatusTT, colActionsTT;
    @FXML
    private TableColumn<Tt, Integer> colDaysTT;

    @FXML
    private Button addLeaveButton;
    @FXML
    private TextField searchField;
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final CongeService congeService = new CongeService();
    private final TtService ttService = new TtService();
    private ObservableList<Conge> congeList = FXCollections.observableArrayList();
    private ObservableList<Tt> ttList = FXCollections.observableArrayList();

    private Map<Integer, String> employeNames = new HashMap<>();

    @FXML
    public void initialize() {
        leaveTt.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        leaveTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        setupTtTableColumns();
        loadConges();
        loadTt();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchConges());
    }

    private void setupTableColumns() {
        colEmployee.setCellValueFactory(cellData -> {
            int idEmploye = cellData.getValue().getId_employe();
            String employeName = employeNames.getOrDefault(idEmploye, "Inconnu");
            return new javafx.beans.property.SimpleStringProperty(employeName);
        });

        colStartDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLeave_start().toString()));
        colEndDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLeave_end().toString()));
        colDays.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getNumber_of_days()).asObject());
        colReason.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReason().toString()));
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));

        colActions.setCellFactory(new Callback<TableColumn<Conge, String>, TableCell<Conge, String>>() {
            @Override
            public TableCell<Conge, String> call(TableColumn<Conge, String> param) {
                return new TableCell<Conge, String>() {
                    private final Button acceptButton = new Button("Accepter");
                    private final Button rejectButton = new Button("Refuser");
                    private final HBox hbox = new HBox(10, acceptButton, rejectButton);

                    {
                        // Styles des boutons
                        acceptButton.getStyleClass().add("accept-button");
                        rejectButton.getStyleClass().add("reject-button");

                        // Action sur le clic des boutons
                        acceptButton.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            approveConge(conge);
                        });

                        rejectButton.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            rejectConge(conge);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Conge conge = getTableView().getItems().get(getIndex());
                            // N'afficher les boutons que si le statut est "EN_COURS"
                            if (conge.getStatus() == Statut.EN_COURS) {
                                setGraphic(hbox);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });
    }

    private void setupTtTableColumns() {
        colEmployeeTT.setCellValueFactory(cellData -> {
            int idEmploye = cellData.getValue().getId_employe();
            String employeName = employeNames.getOrDefault(idEmploye, "Inconnu");
            return new javafx.beans.property.SimpleStringProperty(employeName);
        });

        colStartDateTT.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLeave_start().toString()));
        colEndDateTT.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLeave_end().toString()));
        colDaysTT.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getNumber_of_days()).asObject());
        colStatusTT.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));

        colActionsTT.setCellFactory(new Callback<TableColumn<Tt, String>, TableCell<Tt, String>>() {
            @Override
            public TableCell<Tt, String> call(TableColumn<Tt, String> param) {
                return new TableCell<Tt, String>() {
                    private final Button acceptButton = new Button("Accepter");
                    private final Button rejectButton = new Button("Refuser");
                    private final HBox hbox = new HBox(10, acceptButton, rejectButton);

                    {
                        // Styles des boutons
                        acceptButton.getStyleClass().add("accept-button");
                        rejectButton.getStyleClass().add("reject-button");

                        // Action sur le clic des boutons
                        acceptButton.setOnAction(event -> {
                            Tt tt = getTableView().getItems().get(getIndex());
                            approveTt(tt);
                        });

                        rejectButton.setOnAction(event -> {
                            Tt tt = getTableView().getItems().get(getIndex());
                            rejectTt(tt);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Tt tt = getTableView().getItems().get(getIndex());
                            // N'afficher les boutons que si le statut est "EN_COURS"
                            if (tt.getStatus() == Statut.EN_COURS) {
                                setGraphic(hbox);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });
    }

    private void loadConges() {
        try {
            List<Conge> conges = congeService.showAll();
            congeList.clear();
            employeNames.clear();

            for (Conge conge : conges) {
                congeList.add(conge);
                employeNames.put(conge.getId_employe(), congeService.getEmployeNameById(conge.getId_employe()));
            }

            leaveTable.setItems(FXCollections.observableArrayList(congeList));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTt() {
        try {
            List<Tt> tts = ttService.showAll();
            ttList.clear();

            for (Tt tt : tts) {
                ttList.add(tt);
                employeNames.put(tt.getId_employe(), congeService.getEmployeNameById(tt.getId_employe()));
            }

            leaveTt.setItems(FXCollections.observableArrayList(ttList));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchConges() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            loadConges();
            return;
        }

        ObservableList<Conge> filteredList = FXCollections.observableArrayList();
        for (Conge conge : congeList) {
            String employeName = employeNames.getOrDefault(conge.getId_employe(), "Inconnu").toLowerCase();
            if (employeName.contains(searchText)) {
                filteredList.add(conge);
            }
        }
        leaveTable.setItems(filteredList);
    }

    // Nouvelle méthode pour accepter une demande de congé
    private void approveConge(Conge conge) {
        try {
            // Demander confirmation
            if (showConfirmationDialog("Accepter la demande",
                    "Êtes-vous sûr de vouloir accepter cette demande de congé ?")) {

                conge.setStatus(Statut.ACCEPTE);
                congeService.update(conge);

                // Récupérer le nom et l'email de l'employé
                String employeeName = employeNames.getOrDefault(conge.getId_employe(), "Employé");
                String employeeEmail = getEmployeeEmail(conge.getId_employe());

                // Envoyer un e-mail de notification
                if (employeeEmail != null) {
                    utilisateurService.sendLeaveRequestStatusEmail(
                            employeeEmail,
                            employeeName,
                            "congé",
                            "ACCEPTÉ",
                            conge.getLeave_start().toString(),
                            conge.getLeave_end().toString()
                    );
                }

                // Notification de succès
                showAlert(Alert.AlertType.INFORMATION, "Demande acceptée",
                        "La demande de congé a été acceptée avec succès.");

                loadConges(); // Rafraîchir la liste des congés
                refreshTableView(); // Rafraîchir l'affichage immédiatement
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de l'acceptation de la demande: " + e.getMessage());
        }
    }

    // Update the rejectConge method
    private void rejectConge(Conge conge) {
        try {
            // Demander confirmation
            if (showConfirmationDialog("Refuser la demande",
                    "Êtes-vous sûr de vouloir refuser cette demande de congé ?")) {

                conge.setStatus(Statut.REFUSE);
                congeService.update(conge);

                // Récupérer le nom et l'email de l'employé
                String employeeName = employeNames.getOrDefault(conge.getId_employe(), "Employé");
                String employeeEmail = getEmployeeEmail(conge.getId_employe());

                // Envoyer un e-mail de notification
                if (employeeEmail != null) {
                    utilisateurService.sendLeaveRequestStatusEmail(
                            employeeEmail,
                            employeeName,
                            "congé",
                            "REFUSÉ",
                            conge.getLeave_start().toString(),
                            conge.getLeave_end().toString()
                    );
                }

                // Notification de succès
                showAlert(Alert.AlertType.INFORMATION, "Demande refusée",
                        "La demande de congé a été refusée.");

                loadConges(); // Rafraîchir la liste des congés
                refreshTableView(); // Rafraîchir l'affichage immédiatement
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors du refus de la demande: " + e.getMessage());
        }
    }

    // Update the approveTt method
    private void approveTt(Tt tt) {
        try {
            // Demander confirmation
            if (showConfirmationDialog("Accepter la demande",
                    "Êtes-vous sûr de vouloir accepter cette demande de télétravail ?")) {

                tt.setStatus(Statut.ACCEPTE);
                ttService.update(tt);

                // Récupérer le nom et l'email de l'employé
                String employeeName = employeNames.getOrDefault(tt.getId_employe(), "Employé");
                String employeeEmail = getEmployeeEmail(tt.getId_employe());

                // Envoyer un e-mail de notification
                if (employeeEmail != null) {
                    utilisateurService.sendLeaveRequestStatusEmail(
                            employeeEmail,
                            employeeName,
                            "télétravail",
                            "ACCEPTÉ",
                            tt.getLeave_start().toString(),
                            tt.getLeave_end().toString()
                    );
                }

                // Notification de succès
                showAlert(Alert.AlertType.INFORMATION, "Demande acceptée",
                        "La demande de télétravail a été acceptée avec succès.");

                loadTt(); // Rafraîchir la liste de télétravail
                refreshTableView(); // Rafraîchir l'affichage immédiatement
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de l'acceptation de la demande: " + e.getMessage());
        }
    }

    // Update the rejectTt method
    private void rejectTt(Tt tt) {
        try {
            // Demander confirmation
            if (showConfirmationDialog("Refuser la demande",
                    "Êtes-vous sûr de vouloir refuser cette demande de télétravail ?")) {

                tt.setStatus(Statut.REFUSE);
                ttService.update(tt);

                // Récupérer le nom et l'email de l'employé
                String employeeName = employeNames.getOrDefault(tt.getId_employe(), "Employé");
                String employeeEmail = getEmployeeEmail(tt.getId_employe());

                // Envoyer un e-mail de notification
                if (employeeEmail != null) {
                    utilisateurService.sendLeaveRequestStatusEmail(
                            employeeEmail,
                            employeeName,
                            "télétravail",
                            "REFUSÉ",
                            tt.getLeave_start().toString(),
                            tt.getLeave_end().toString()
                    );
                }

                // Notification de succès
                showAlert(Alert.AlertType.INFORMATION, "Demande refusée",
                        "La demande de télétravail a été refusée.");

                loadTt(); // Rafraîchir la liste de télétravail
                refreshTableView(); // Rafraîchir l'affichage immédiatement
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors du refus de la demande: " + e.getMessage());
        }
    }
    // Add this helper method to retrieve the employee's email
    private String getEmployeeEmail(int employeeId) {
        try {
            UtilisateurService userService = new UtilisateurService();
            Utilisateur user = userService.getUserById(employeeId);
            return user != null ? user.getEmail() : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    // Nouvelle méthode pour rafraîchir l'affichage des tableaux
    private void refreshTableView() {
        leaveTable.refresh();
        leaveTt.refresh();

        // Mettre à jour les observableLists pour conserver les modifications
        if (leaveTable.isVisible()) {
            leaveTable.setItems(FXCollections.observableArrayList(congeList));
        }

        if (leaveTt.isVisible()) {
            leaveTt.setItems(FXCollections.observableArrayList(ttList));
        }
    }

    // Méthode utilitaire pour afficher une boîte de dialogue de confirmation
    private boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Méthode utilitaire pour afficher une alerte
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAddLeave(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/carteCongé.fxml"));
            Scene scene = new Scene(root);

            Stage stage = (Stage) addLeaveButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showAddCongePopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/carteCongé.fxml"));
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

    @FXML
    private void showConges() {
        leaveTable.setVisible(true);
        leaveTt.setVisible(false);
        loadConges();  // Charger les congés
    }

    @FXML
    private void showTT() {
        leaveTable.setVisible(false);
        leaveTt.setVisible(true);
        loadTt();  // Charger le TT
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        searchConges();
    }

    public void refreshUserList() {
        loadConges();
        loadTt();
    }
}