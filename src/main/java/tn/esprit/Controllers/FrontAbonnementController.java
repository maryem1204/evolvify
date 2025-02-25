package tn.esprit.Controllers;

        import javafx.fxml.FXML;
        import javafx.fxml.FXMLLoader;
        import javafx.fxml.Initializable;
        import javafx.geometry.Insets;
        import javafx.geometry.Pos;
        import javafx.scene.Parent;
        import javafx.scene.Scene;
        import javafx.scene.chart.BarChart;
        import javafx.scene.chart.LineChart;
        import javafx.scene.chart.XYChart;
        import javafx.scene.control.*;
        import javafx.scene.layout.HBox;
        import javafx.scene.layout.StackPane;
        import javafx.scene.layout.VBox;
        import javafx.scene.paint.Color;
        import javafx.stage.Modality;
        import javafx.stage.Stage;
        import tn.esprit.Entities.StatusAbonnement;
        import tn.esprit.Entities.Abonnement;
        import tn.esprit.Services.AbonnementCRUD;

        import java.io.IOException;
        import java.net.URL;
        import java.sql.SQLException;
        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.List;
        import java.util.Map;
        import java.util.Optional;
        import java.util.ResourceBundle;
        import java.util.stream.Collectors;

public class FrontAbonnementController  implements Initializable {

    @FXML private VBox abonnementsContainer;
    @FXML private TextField searchField;
    @FXML private Label totalRevenueLabel;
    @FXML private Label activeRateLabel;
    @FXML private ProgressIndicator activeRateIndicator;
    @FXML private Button addAbonnementButton;
    @FXML private VBox popularPlansContainer;
    @FXML private LineChart<String, Number> subscriptionTypeChart;
    @FXML private LineChart<String, Number> subscriptions30dChart;
    @FXML private BarChart<String, Number> employeeActivityChart;
    @FXML private Label subscriptionTypesLabel;
    @FXML private Label subscriptions30dLabel;
    @FXML private Label activeEmployeesLabel;
    @FXML private Label userInitialsLabel;

    private List<Abonnement> allAbonnements;
    private final AbonnementCRUD abonnementCRUD = new AbonnementCRUD();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAbonnements();
        setupSearch();
        setupAddButton();
        updateStatistics();
        populateCharts();
        try {
            loadPopularPlans();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        updateSubscriptionStatistics();
        setUserInitials();
    }

    private void loadAbonnements() {
        try {
            allAbonnements = abonnementCRUD.showAll();
            renderAbonnements(allAbonnements);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement des abonnements");
        }
    }

    private void renderAbonnements(List<Abonnement> abonnements) {
        abonnementsContainer.getChildren().clear();
        abonnements.forEach(abonnement -> {
            HBox row = createAbonnementRow(abonnement);
            abonnementsContainer.getChildren().add(row);
        });
    }

    private HBox createAbonnementRow(Abonnement abonnement) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: #2A2E3C; -fx-background-radius: 10; -fx-padding: 10;");
        row.setSpacing(10);

        // Type d'abonnement
        Label type = createLabel(abonnement.getType_Ab(), 130);

        // Date de début
        Label dateDebut = createLabel(formatDate(abonnement.getDate_debut()), 130);

        // Date d'expiration
        Label dateExp = createLabel(formatDate(abonnement.getDate_exp()), 130);

        // Prix
        Label prix = createLabel(abonnement.getPrix() + " DT", 100);

        // Employé ID
        Label employe = createLabel("ID: " + abonnement.getId_employe(), 100);

        // Statut
        Label statut = createStatusLabel(abonnement.getStatus());

        // Actions
        HBox actionsBox = createActionButtons(abonnement);

        row.getChildren().addAll(type, dateDebut, dateExp, prix, employe, statut, actionsBox);
        return row;
    }

    private Label createLabel(String text, double minWidth) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setMinWidth(minWidth);
        label.setStyle("-fx-font-size: 14;");
        return label;
    }

    private Label createStatusLabel(StatusAbonnement status) {
        Label label = new Label(status.name());
        label.setMinWidth(100);
        label.setStyle("-fx-font-size: 14;");

        switch (status) {
            case ACTIF:
                label.setTextFill(Color.valueOf("#4CAF50"));
                break;
            case INACTIF:
                label.setTextFill(Color.valueOf("#FFC107"));
                break;
            case SUSPENDU:
                label.setTextFill(Color.valueOf("#f44336"));
                break;
            default:
                label.setTextFill(Color.GRAY);
        }
        return label;
    }

    private HBox createActionButtons(Abonnement abonnement) {
        HBox actionsBox = new HBox(5);
        actionsBox.setMinWidth(100);

        Button details = createActionButton("DÉTAILS", "#3D435A", e -> showDetails(abonnement));
        Button modifier = createActionButton("MODIFIER", "#6C5CE7", e -> editAbonnement(abonnement));
        Button supprimer = createActionButton("SUPPRIMER", "#f44336", e -> deleteAbonnement(abonnement));

        actionsBox.getChildren().addAll(details, modifier, supprimer);
        return actionsBox;
    }

    private Button createActionButton(String text, String color, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 5;");
        button.setMinWidth(70);
        button.setOnAction(handler);
        return button;
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Abonnement> filtered = allAbonnements.stream()
                    .filter(a -> a.getType_Ab().toLowerCase().contains(newVal.toLowerCase()) ||
                            formatDate(a.getDate_debut()).contains(newVal) ||
                            formatDate(a.getDate_exp()).contains(newVal) ||
                            String.valueOf(a.getPrix()).contains(newVal) ||
                            String.valueOf(a.getId_employe()).contains(newVal) ||
                            a.getStatus().name().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toList());
            renderAbonnements(filtered);
        });
    }

    private void setupAddButton() {
        addAbonnementButton.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_abonnement.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();
                loadAbonnements();
            } catch (IOException ex) {
                showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout");
            }
        });
    }

    private void editAbonnement(Abonnement abonnement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifier_abonnement.fxml"));
            Parent root = loader.load();
            ModifierAbonnementController controller = loader.getController();
            controller.setAbonnement(abonnement);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadAbonnements();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur");
        }
    }

    private void deleteAbonnement(Abonnement abonnement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet abonnement ?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                abonnementCRUD.delete(abonnement);
                loadAbonnements();
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression");
            }
        }
    }

    private void updateStatistics() {
        // Update total revenue
        if (totalRevenueLabel != null) {
            totalRevenueLabel.setText("0 DT");
        }
        if (activeRateLabel != null) {
            activeRateLabel.setText("0%");
        }

        double totalRevenue = allAbonnements.stream()
                .mapToDouble(Abonnement::getPrix)
                .sum();
        totalRevenueLabel.setText(String.format("%.1f DT", totalRevenue));

        // Update active rate
        long active = allAbonnements.stream()
                .filter(a -> a.getStatus() == StatusAbonnement.ACTIF)
                .count();
        double rate = allAbonnements.isEmpty() ? 0 : (double) active / allAbonnements.size();
        activeRateLabel.setText(String.format("%.0f%%", rate * 100));
        activeRateIndicator.setProgress(rate);
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    private void showDetails(Abonnement abonnement) {
        // Implement details view logic
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void populateCharts() {
        populateSubscriptionTypeChart();
        populateSubscriptions30dChart();
        populateEmployeeActivityChart();
    }

    private void populateSubscriptionTypeChart() {
        subscriptionTypeChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        Map<String, Long> typeCounts = allAbonnements.stream()
                .collect(Collectors.groupingBy(Abonnement::getType_Ab, Collectors.counting()));

        typeCounts.forEach((type, count) ->
                series.getData().add(new XYChart.Data<>(type, count))
        );

        subscriptionTypeChart.getData().add(series);
    }

    private void populateSubscriptions30dChart() {
        // Implement based on subscriptions in the last 30 days
        subscriptions30dChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Example of how to implement (replace with actual logic based on your data)
        Date today = new Date();
        Date thirtyDaysAgo = new Date(today.getTime() - 30L * 24 * 60 * 60 * 1000);

        Map<String, Long> subscriptionsPerDay = allAbonnements.stream()
                .filter(a -> a.getDate_debut().after(thirtyDaysAgo) && a.getDate_debut().before(today))
                .collect(Collectors.groupingBy(
                        a -> formatDate(a.getDate_debut()),
                        Collectors.counting()
                ));

        // Add sample data points (replace with your actual data)
        subscriptionsPerDay.forEach((date, count) ->
                series.getData().add(new XYChart.Data<>(date, count))
        );

        subscriptions30dChart.getData().add(series);
    }

    private void populateEmployeeActivityChart() {
        // Implement based on employee subscription activity
        employeeActivityChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Group subscriptions by employee ID
        Map<Integer, Long> subscriptionsPerEmployee = allAbonnements.stream()
                .collect(Collectors.groupingBy(
                        Abonnement::getId_employe,
                        Collectors.counting()
                ));

        // Add top 5 employees (for example)
        subscriptionsPerEmployee.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(entry ->
                        series.getData().add(new XYChart.Data<>("Emp " + entry.getKey(), entry.getValue()))
                );

        employeeActivityChart.getData().add(series);
    }

    private void updateSubscriptionStatistics() {
        long distinctTypes = allAbonnements.stream()
                .map(Abonnement::getType_Ab)
                .distinct()
                .count();
        subscriptionTypesLabel.setText(String.valueOf(distinctTypes));

        // Update 30d count
        Date today = new Date();
        Date thirtyDaysAgo = new Date(today.getTime() - 30L * 24 * 60 * 60 * 1000);
        long recentSubscriptions = allAbonnements.stream()
                .filter(a -> a.getDate_debut().after(thirtyDaysAgo))
                .count();
        subscriptions30dLabel.setText(String.valueOf(recentSubscriptions));

        // Update active employees
        long activeEmployees = allAbonnements.stream()
                .filter(a -> a.getStatus() == StatusAbonnement.ACTIF)
                .map(Abonnement::getId_employe)
                .distinct()
                .count();
        activeEmployeesLabel.setText(String.valueOf(activeEmployees));
    }

    private void loadPopularPlans() throws SQLException {
        popularPlansContainer.getChildren().clear();

        // Get popular plans by counting type occurrences
        Map<String, Long> typeCounts = allAbonnements.stream()
                .collect(Collectors.groupingBy(Abonnement::getType_Ab, Collectors.counting()));

        // Sort by count and take top 4
        List<Map.Entry<String, Long>> topPlans = typeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(4)
                .collect(Collectors.toList());

        // Create UI for each popular plan
        topPlans.forEach(entry -> {
            HBox planEntry = createPopularPlanEntry(entry.getKey(), entry.getValue());
            popularPlansContainer.getChildren().add(planEntry);
        });
    }

    private HBox createPopularPlanEntry(String planType, Long count) {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);

        // Plan Icon
        StackPane icon = new StackPane();
        icon.setStyle("-fx-background-radius: 10; -fx-min-width: 50; -fx-min-height: 50;");

        Label iconLabel = new Label();
        iconLabel.setTextFill(Color.WHITE);
        iconLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 24;");

        // Different colors for different plan types
        switch (planType.toUpperCase()) {
            case "MENSUEL" -> {
                icon.setStyle("-fx-background-color: #4e5bf2;");
                iconLabel.setText("M");
            }
            case "TRIMESTRIEL" -> {
                icon.setStyle("-fx-background-color: #5cc6f2;");
                iconLabel.setText("T");
            }
            case "SEMESTRIEL" -> {
                icon.setStyle("-fx-background-color: #a7e155;");
                iconLabel.setText("S");
            }
            case "ANNUEL" -> {
                icon.setStyle("-fx-background-color: #b659e3;");
                iconLabel.setText("A");
            }
            default -> {
                icon.setStyle("-fx-background-color: #f44336;");
                iconLabel.setText(planType.substring(0, 1).toUpperCase());
            }
        }
        icon.getChildren().add(iconLabel);

        // Plan Details
        VBox details = new VBox();
        Label type = new Label(planType);
        type.setTextFill(Color.WHITE);

        Label subscribers = new Label(count + " abonnés");
        subscribers.setTextFill(Color.web("#9395A6"));

        details.getChildren().addAll(type, subscribers);

        // Popularity percentage (just for display, calculated based on proportion of all subscriptions)
        double percentage = (double) count / allAbonnements.size() * 100;
        Label popularity = new Label(String.format("%.1f%%", percentage));
        popularity.setTextFill(Color.web("#4CAF50"));

        hbox.getChildren().addAll(icon, details, popularity);
        return hbox;
    }

    private void setUserInitials() {
        // Example: Retrieve current user's initials
        String initials = "JD"; // Replace with actual user data
        userInitialsLabel.setText(initials);
    }
}