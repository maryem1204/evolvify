package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import tn.esprit.Entities.Gender;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;


import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class DashAdminRHController {

    @FXML
    private PieChart pieChart;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private TableView<Utilisateur> employeeTable;
    @FXML
    private TableColumn<Utilisateur, String> nameColumn;
    @FXML
    private TableColumn<Utilisateur, String> numberColumn;
    @FXML
    private TableColumn<Utilisateur, String> dateColumn;
    @FXML
    private TableColumn<Utilisateur, String> statusColumn;
    @FXML
    private TextField searchField;
    @FXML
    private VBox progressionBox;
    @FXML
    private Label labelHommes;
    @FXML
    private Label labelFemmes;
    @FXML
    private Label labelTotalEmployes;
    @FXML
    private Label labelCongesAcceptes;
    @FXML
    private Label labelCandidats;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private ObservableList<Utilisateur> masterData = FXCollections.observableArrayList();

    public void initialize() {
        updateEffectifs();
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        AnchorPane.setLeftAnchor(progressionBox, 260.0);
        AnchorPane.setTopAnchor(progressionBox, 520.0);

        setupPieChart();
        setupBarChart();
        setupLineChart();
        setupEmployeeTable();
        setupSearch();
    }

    @FXML
    private void updateEffectifs() {
        int hommes = utilisateurService.countByGender(Gender.HOMME);
        int femmes = utilisateurService.countByGender(Gender.FEMME);
        int totalEmployes = utilisateurService.countUsers();
        int congesAcceptes = utilisateurService.countAcceptedLeaves();
        int candidats = utilisateurService.countByRole(Role.CONDIDAT);

        labelHommes.setText(hommes + " hommes");
        labelFemmes.setText(femmes + " femmes");
        labelTotalEmployes.setText("Total employés " + totalEmployes);
        labelCongesAcceptes.setText(congesAcceptes + " en congé");
        labelCandidats.setText(candidats + " à recruter");
    }

    private void setupPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Males", 889),
                new PieChart.Data("Females", 591)
        );
        pieChart.setData(pieChartData);
    }

    private void setupBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Week 1", 50));
        series.getData().add(new XYChart.Data<>("Week 2", 75));
        series.getData().add(new XYChart.Data<>("Week 3", 110));
        series.getData().add(new XYChart.Data<>("Week 4", 90));
        barChart.getData().add(series);
    }

    private void setupLineChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Week 1", 30));
        series.getData().add(new XYChart.Data<>("Week 2", 60));
        series.getData().add(new XYChart.Data<>("Week 3", 100));
        series.getData().add(new XYChart.Data<>("Week 4", 80));
        lineChart.getData().add(series);
    }

    private void setupEmployeeTable() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstname() + " " + cellData.getValue().getLastname()));
        numberColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJoiningDate().toString()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().toString()));

        loadEmployeeData();
    }

    private void loadEmployeeData() {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.showAll();
            masterData.setAll(utilisateurs);
            employeeTable.setItems(masterData);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement des utilisateurs : " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEmployeeList(newValue);
        });
    }

    private void filterEmployeeList(String query) {
        if (query == null || query.trim().isEmpty()) {
            employeeTable.setItems(masterData);
            return;
        }

        String lowerCaseQuery = query.toLowerCase();
        ObservableList<Utilisateur> filteredList = masterData.stream()
                .filter(utilisateur -> (utilisateur.getFirstname() + " " + utilisateur.getLastname()).toLowerCase().contains(lowerCaseQuery)
                        || utilisateur.getFirstname().toLowerCase().contains(lowerCaseQuery)
                        || utilisateur.getLastname().toLowerCase().contains(lowerCaseQuery)
                        || utilisateur.getEmail().contains(lowerCaseQuery)
                        || utilisateur.getRole().toString().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        employeeTable.setItems(filteredList);
    }
}
