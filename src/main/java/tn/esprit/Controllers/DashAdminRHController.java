package tn.esprit.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import tn.esprit.Entities.Gender;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DashAdminRHController {
    private static final Logger LOGGER = Logger.getLogger(DashAdminRHController.class.getName());

    // FXML Injected Components
    @FXML private PieChart pieChart;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private BarChart<String, Number> progressChart;
    @FXML private TableView<Utilisateur> employeeTable;
    @FXML private TableColumn<Utilisateur, String> nameColumn;
    @FXML private TableColumn<Utilisateur, String> numberColumn;
    @FXML private TableColumn<Utilisateur, String> dateColumn;
    @FXML private TableColumn<Utilisateur, String> statusColumn;
    @FXML private TextField searchField;
    @FXML private VBox progressionBox;
    @FXML private AnchorPane effectifs;
    @FXML private Label labelHommes;
    @FXML private Label labelFemmes;
    @FXML private Label labelTotalEmployes;
    @FXML private Label labelCongesAcceptes;
    @FXML private Label labelCandidats;
    @FXML private Label absenceCount;
    @FXML private Label abonnementsCount;
    @FXML private Label projectsCount;
    @FXML private Label tasksCount;
    @FXML private Button resizeButton;

    // Service Instances
    private final UtilisateurService utilisateurService;

    // Master data list for employees
    private ObservableList<Utilisateur> masterData;

    // Layout state
    private boolean isExpanded = false;

    // Constructor with dependency injection
    public DashAdminRHController() {
        this.utilisateurService = new UtilisateurService();
        this.masterData = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        try {
            // Initialize UI components and data
            initializeCharts();
            initializeEmployeeTable();
            initializeLabels();
            setupSearch();
            setupResizeButton();

            // Check if components are not null before configuring layout
            if (progressionBox != null && employeeTable != null) {
                configureAnchorPaneLayout();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Initialization error", e);
            showErrorAlert("Initialization Error", "Could not load dashboard data: " + e.getMessage());
        }

        // Add window resize listener
        if (employeeTable.getScene() != null) {
            employeeTable.getScene().widthProperty().addListener((obs, oldVal, newVal) -> handleWindowResize());
            employeeTable.getScene().heightProperty().addListener((obs, oldVal, newVal) -> handleWindowResize());
        }


    }

    private void setupResizeButton() {
        if (resizeButton != null) {
            resizeButton.setOnAction(event -> toggleDashboardLayout());
        }
    }

    private void toggleDashboardLayout() {
        isExpanded = !isExpanded;
        updateDashboardLayout();
    }

    private void updateDashboardLayout() {
        if (isExpanded) {
            // Expanded view - show all details
            if (progressionBox != null) progressionBox.setVisible(true);
            if (employeeTable != null) employeeTable.setVisible(true);
            // Add more components to show/hide as needed
        } else {
            // Compact view - hide some details
            if (progressionBox != null) progressionBox.setVisible(false);
            if (employeeTable != null) employeeTable.setVisible(false);
            // Add more components to show/hide as needed
        }
    }

    private void handleWindowResize() {
        // Adjust layout dynamically
        if (progressionBox != null) {
            progressionBox.setPrefWidth(employeeTable.getScene().getWidth());
        }

        configureAnchorPaneLayout();

        // Responsive layout based on window size
        double windowWidth = employeeTable.getScene().getWidth();
        double windowHeight = employeeTable.getScene().getHeight();

        if (windowWidth > 1200 && windowHeight > 800) {
            // Large window - ensure expanded view
            if (!isExpanded) {
                isExpanded = true;
                updateDashboardLayout();
            }
        } else {
            // Smaller window - compact view
            if (isExpanded) {
                isExpanded = false;
                updateDashboardLayout();
            }
        }
    }

    private void initializeCharts() {
        try {
            if (progressChart != null) setupProgressionChart();
            if (lineChart != null) {
                setupPerformanceChart();
                setupLineChart();
            }
            if (pieChart != null) loadPieChart();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Chart initialization error", e);
        }
    }

    private void initializeEmployeeTable() {
        if (employeeTable == null) return;

        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFirstname() + " " + cellData.getValue().getLastname()));
        numberColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getJoiningDate().toString()));
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole().toString()));

        loadEmployeeData();
    }

    private void initializeLabels() {
        updateEffectifs();
        updateAbsenceLabel();
        updateAbonnementCount();
        updateProjectCount();
        updateTaskCount();
    }

    private void configureAnchorPaneLayout() {
        // Use percentage-based or responsive anchoring
        if (progressionBox != null) {
            AnchorPane.setLeftAnchor(progressionBox, 0.0);
            AnchorPane.setTopAnchor(progressionBox, 0.0);
            AnchorPane.setRightAnchor(progressionBox, 0.0);
        }

        if (employeeTable != null) {
            AnchorPane.setLeftAnchor(employeeTable, 0.0);
            AnchorPane.setTopAnchor(employeeTable, 250.0);
            AnchorPane.setRightAnchor(employeeTable, 0.0);
            AnchorPane.setBottomAnchor(employeeTable, 0.0);
        }
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
            projectSeries.getData().add(new XYChart.Data<>(weekLabel,
                    projectsPerWeek.getOrDefault(weekNum, 0)));
            taskSeries.getData().add(new XYChart.Data<>(weekLabel,
                    tasksPerWeek.getOrDefault(weekNum, 0)));
        }

        progressChart.getData().addAll(projectSeries, taskSeries);
    }

    private void setupPerformanceChart() {
        lineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Évolution des tâches");

        try {
            Map<String, Integer> taskStatusCount = utilisateurService.getTaskStatusCount();
            LOGGER.info("Task Status Count: " + taskStatusCount);

            // Add data for each task status
            series.getData().add(new XYChart.Data<>("TO_DO",
                    taskStatusCount.getOrDefault("TO_DO", 0)));
            series.getData().add(new XYChart.Data<>("IN_PROGRESS",
                    taskStatusCount.getOrDefault("IN_PROGRESS", 0)));
            series.getData().add(new XYChart.Data<>("DONE",
                    taskStatusCount.getOrDefault("DONE", 0)));
            series.getData().add(new XYChart.Data<>("CANCELED",
                    taskStatusCount.getOrDefault("CANCELED", 0)));

            lineChart.getData().add(series);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error setting up performance chart", e);
        }
    }

    private void setupLineChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Weekly Performance");
        series.getData().addAll(
                new XYChart.Data<>("Week 1", 30),
                new XYChart.Data<>("Week 2", 60),
                new XYChart.Data<>("Week 3", 100),
                new XYChart.Data<>("Week 4", 80)
        );
        lineChart.getData().add(series);
    }

    private void loadEmployeeData() {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.showAll();
            masterData.setAll(utilisateurs);
            employeeTable.setItems(masterData);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading employees", e);
            showErrorAlert("Data Load Error", "Could not load employee data.");
        }
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

    private void loadPieChart() {
        pieChart.getData().clear();
        Map<String, Integer> stats = utilisateurService.getAbonnementStatistics();

        stats.forEach((key, value) -> {
            PieChart.Data slice = new PieChart.Data(key, value);
            pieChart.getData().add(slice);
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) ->
                filterEmployeeList(newValue));
    }

    private void filterEmployeeList(String query) {
        if (query == null || query.trim().isEmpty()) {
            employeeTable.setItems(masterData);
            return;
        }

        String lowerCaseQuery = query.toLowerCase();
        ObservableList<Utilisateur> filteredList = masterData.stream()
                .filter(utilisateur ->
                        (utilisateur.getFirstname() + " " + utilisateur.getLastname()).toLowerCase().contains(lowerCaseQuery)
                                || utilisateur.getFirstname().toLowerCase().contains(lowerCaseQuery)
                                || utilisateur.getLastname().toLowerCase().contains(lowerCaseQuery)
                                || utilisateur.getEmail().contains(lowerCaseQuery)
                                || utilisateur.getRole().toString().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        employeeTable.setItems(filteredList);
    }

    public void updateAbsenceLabel() {
        int count = utilisateurService.updateAbsenceCount();
        absenceCount.setText(String.valueOf(count));
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

    // Utility method to show error alerts
    private void showErrorAlert(String title, String content) {
        try {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing alert", e);
        }
    }

    // Additional method to refresh dashboard
    public void refreshDashboard() {
        initializeCharts();
        initializeEmployeeTable();
        initializeLabels();
    }
}