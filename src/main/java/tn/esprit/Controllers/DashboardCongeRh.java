package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import tn.esprit.Entities.Conge;
import tn.esprit.Entities.Tt;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.CongeService;
import tn.esprit.Services.TtService;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @FXML
    private Button searchButton;

    private final CongeService congeService = new CongeService();
    private final TtService ttService = new TtService();
    private ObservableList<Conge> congeList = FXCollections.observableArrayList();
    private ObservableList<Tt> ttList = FXCollections.observableArrayList();

    private Map<Integer, String> employeNames = new HashMap<>();

    @FXML
    public void initialize() {
        setupTableColumns();
        setupTtTableColumns();
        loadConges();
        loadTt();
        searchButton.setOnAction(event -> searchConges());
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
                    private final Button editButton = new Button("Éditer");
                    private final Button deleteButton = new Button("Supprimer");
                    private final HBox actionButtons = new HBox(5, editButton, deleteButton);

                    {
                        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                        editButton.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            editConge(conge);
                        });
                        deleteButton.setOnAction(event -> {
                            Conge conge = getTableView().getItems().get(getIndex());
                            deleteConge(conge);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionButtons);
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
                    private final Button editButtonTT = new Button("Éditer");
                    private final Button deleteButtonTT = new Button("Supprimer");
                    private final HBox actionButtons = new HBox(5, editButtonTT, deleteButtonTT);

                    {
                        editButtonTT.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                        deleteButtonTT.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                        editButtonTT.setOnAction(event -> {
                            Tt tt = getTableView().getItems().get(getIndex());
                            editTt(tt);
                        });
                        deleteButtonTT.setOnAction(event -> {
                            Tt tt = getTableView().getItems().get(getIndex());
                            deleteTt(tt);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionButtons);
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
            ttList.clear();
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
            employeNames.clear();

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

    private void deleteConge(Conge conge) {
        try {
            congeService.delete(conge);
            congeList.remove(conge);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteTt(Tt tt) {
        try {
            ttService.delete(tt);
            ttList.remove(tt);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editConge(Conge conge) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditConge.fxml"));
            Parent root = loader.load();
            EditCongeController controller = loader.getController();
            controller.setConge(conge);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshUserList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editTt(Tt tt) {
        /*try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditTt.fxml"));
            Parent root = loader.load();
            EditTtController controller = loader.getController();
            controller.setTt(tt);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadTt();  // Rafraîchir après modification
        } catch (IOException e) {
            e.printStackTrace();
        }*/
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

    public void handleSearch(ActionEvent event) {
        // handle search logic here
    }

    public void refreshUserList() {
        try {
            List<Conge> conges = congeService.showAll();
            congeList.clear();
            congeList.addAll(conges);

            leaveTable.setItems(congeList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
