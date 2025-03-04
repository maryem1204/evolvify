package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import tn.esprit.Entities.Gender;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.ProjetService;
import tn.esprit.Services.TacheService;
import tn.esprit.Services.UtilisateurService;


import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashAdminRHController {

    @FXML
    private PieChart pieChart;
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
    private VBox progressionBox,effectifs;
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
    @FXML
    private Label absenceCount;
    @FXML
    private Label abonnementsCount;
    @FXML
    private Label projectsCount;
    @FXML
    private Label tasksCount;
    @FXML
    private BarChart<String, Number> progressChart;


    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ProjetService projetService = new ProjetService();
    private final TacheService tacheService = new TacheService();

    private ObservableList<Utilisateur> masterData = FXCollections.observableArrayList();

    public void initialize() {
        updateEffectifs();
        updateAbsenceLabel();
        updateAbonnementCount();
        updateProjectCount();
        updateTaskCount();
        loadPieChart();

        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        AnchorPane.setLeftAnchor(progressionBox, 200.0);
        AnchorPane.setTopAnchor(progressionBox, 400.0);
        AnchorPane.setLeftAnchor(employeeTable, 260.0);
        AnchorPane.setTopAnchor(employeeTable, 470.0);


        setupLineChart();
        setupEmployeeTable();
        setupSearch();
        setupProgressionChart();
        setupPerformanceChart();

    }



    private void setupProgressionChart() {
        progressChart.getData().clear();
        XYChart.Series<String, Number> projectSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> taskSeries = new XYChart.Series<>();
        projectSeries.setName("Projets");
        taskSeries.setName("Tâches");

        Map<Integer, Integer> projectsPerWeek = utilisateurService.getProjectsPerWeekForLastFourWeeks();
        Map<Integer, Integer> tasksPerWeek = utilisateurService.getTasksPerWeekForLastFourWeeks();

        Calendar cal = Calendar.getInstance();
        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);

        for (int i = 0; i < 4; i++) {
            int weekNum = currentWeek - 3 + i;
            String weekLabel = "Semaine " + (i + 1);
            projectSeries.getData().add(new XYChart.Data<>(weekLabel, projectsPerWeek.getOrDefault(weekNum, 0)));
            taskSeries.getData().add(new XYChart.Data<>(weekLabel, tasksPerWeek.getOrDefault(weekNum, 0)));
        }

        progressChart.getData().addAll(projectSeries, taskSeries);
    }

    private void setupPerformanceChart() {
        lineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Évolution des tâches");

        Map<String, Integer> taskStatusCount = utilisateurService.getTaskStatusCount();
        System.out.println("Données récupérées pour la courbe : " + taskStatusCount);

        // Ajout des données pour chaque statut
        series.getData().add(new XYChart.Data<>("TO_DO", taskStatusCount.getOrDefault("TO_DO", 0)));
        series.getData().add(new XYChart.Data<>("IN_PROGRESS", taskStatusCount.getOrDefault("IN_PROGRESS", 0)));
        series.getData().add(new XYChart.Data<>("DONE", taskStatusCount.getOrDefault("DONE", 0)));
        series.getData().add(new XYChart.Data<>("CANCELED", taskStatusCount.getOrDefault("CANCELED", 0)));

        lineChart.getData().add(series);
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

    /*private void setupPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Males", 889),
                new PieChart.Data("Females", 591)
        );
        pieChart.setData(pieChartData);
    }*/



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

    private void updateAbsenceLabel() {
        int count = utilisateurService.updateAbsenceCount(); // Appel de la méthode du service
        absenceCount.setText(String.valueOf(count)); // Mise à jour du label
    }

    public void updateAbonnementCount() {
        int count = utilisateurService.getAbonnementCount();
        abonnementsCount.setText(String.valueOf(count));
    }


    public void updateProjectCount() {
        int count = utilisateurService.getProjectCount();
        projectsCount.setText(String.valueOf(count));
    }

    public void updateTaskCount() {
        int count = utilisateurService.getTaskCount();
        tasksCount.setText(String.valueOf(count));
    }

    private void loadPieChart() {
        pieChart.getData().clear(); // Vider les anciennes données

        Map<String, Integer> stats = utilisateurService.getAbonnementStatistics(); // Récupérer les nouvelles données

        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChart.getData().add(slice);
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
