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
import tn.esprit.Entities.StatusTrajet;
import tn.esprit.Entities.Trajet;
import tn.esprit.Services.TrajetCRUD;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FrontTransportController implements Initializable {

    @FXML private VBox trajetsContainer;
    @FXML private TextField searchField;
    @FXML private Label totalDistanceLabel;
    @FXML private Label completionRateLabel;
    @FXML private ProgressIndicator completionRateIndicator;
    @FXML private Button addTrajetButton;
    @FXML private VBox efficientRoutesContainer;
    @FXML private LineChart<String, Number> transportUsageChart;
    @FXML private LineChart<String, Number> trips24hChart;
    @FXML private BarChart<String, Number> employeeActivityChart;
    @FXML private Label transportCountLabel;
    @FXML private Label trips24hLabel;
    @FXML private Label activeEmployeesLabel;
    @FXML private Label userInitialsLabel;

    private List<Trajet> allTrajets;
    private final TrajetCRUD trajetCRUD = new TrajetCRUD();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadTrajets();
        setupSearch();
        setupAddButton();
        updateStatistics();
        populateCharts();
        try {
            loadEfficientRoutes();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        updateTransportStatistics();
        setUserInitials();
    }

    private void loadTrajets() {
        try {
            allTrajets = trajetCRUD.showAll();
            renderTrajets(allTrajets);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de chargement des trajets");
        }
    }

    private void renderTrajets(List<Trajet> trajets) {
        trajetsContainer.getChildren().clear();
        trajets.forEach(trajet -> {
            HBox row = createTrajetRow(trajet);
            trajetsContainer.getChildren().add(row);
        });
    }

    private HBox createTrajetRow(Trajet trajet) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: #2A2E3C; -fx-background-radius: 10; -fx-padding: 10;");
        row.setSpacing(10);

        // Départ
        Label depart = createLabel(trajet.getPointDep(), 130);

        // Arrivée
        Label arrivee = createLabel(trajet.getPointArr(), 130);

        // Distance
        Label distance = createLabel(trajet.getDistance() + " km", 100);

        // Durée
        Label duree = createLabel(formatTime(trajet.getDuréeEstimé()), 100);

        // Transport
        HBox transportBox = createTransportCell(trajet);

        // Statut
        Label statut = createStatusLabel(trajet.getStatus());

        // Actions
        HBox actionsBox = createActionButtons(trajet);

        row.getChildren().addAll(depart, arrivee, distance, duree, transportBox, statut, actionsBox);
        return row;
    }

    private Label createLabel(String text, double minWidth) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setMinWidth(minWidth);
        label.setStyle("-fx-font-size: 14;");
        return label;
    }

    private HBox createTransportCell(Trajet trajet) {
        HBox transportBox = new HBox(10);
        transportBox.setMinWidth(120);

        StackPane icon = new StackPane();
        icon.setStyle("-fx-background-radius: 5; -fx-min-width: 24; -fx-min-height: 24;");

        Label iconLabel = new Label();
        iconLabel.setTextFill(Color.WHITE);
        iconLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");

        try {
            String type = trajet.getTypeMoyen();
            switch (type.toUpperCase()) {
                case "VOITURE":
                    icon.setStyle("-fx-background-color: #4e5bf2;");
                    iconLabel.setText("V");
                    break;
                case "BUS":
                    icon.setStyle("-fx-background-color: #5cc6f2;");
                    iconLabel.setText("B");
                    break;
                case "TRAIN":
                    icon.setStyle("-fx-background-color: #a7e155;");
                    iconLabel.setText("T");
                    break;
                default:
                    icon.setStyle("-fx-background-color: #b659e3;");
                    iconLabel.setText("A");
            }
        } catch (Exception e) {
            icon.setStyle("-fx-background-color: #9395A6;");
            iconLabel.setText("?");
        }

        icon.getChildren().add(iconLabel);

        Label typeLabel = new Label(trajet.getTypeMoyen());
        typeLabel.setTextFill(Color.WHITE);
        typeLabel.setStyle("-fx-font-size: 14;");

        transportBox.getChildren().addAll(icon, typeLabel);
        return transportBox;
    }

    private Label createStatusLabel(StatusTrajet status) {
        Label label = new Label(status.name());
        label.setMinWidth(100);
        label.setStyle("-fx-font-size: 14;");

        switch (status) {
            case TERMINE:
                label.setTextFill(Color.valueOf("#4CAF50"));
                break;
            case EN_COURS:
                label.setTextFill(Color.valueOf("#FFC107"));
                break;
            default:
                label.setTextFill(Color.GRAY);
        }
        return label;
    }

    private HBox createActionButtons(Trajet trajet) {
        HBox actionsBox = new HBox(5);
        actionsBox.setMinWidth(100);

        Button details = createActionButton("DÉTAILS", "#3D435A", e -> showDetails(trajet));
        Button modifier = createActionButton("MODIFIER", "#6C5CE7", e -> editTrajet(trajet));
        Button supprimer = createActionButton("SUPPRIMER", "#f44336", e -> deleteTrajet(trajet));

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
            List<Trajet> filtered = allTrajets.stream()
                    .filter(t -> t.getPointDep().toLowerCase().contains(newVal.toLowerCase()) ||
                            t.getPointArr().toLowerCase().contains(newVal.toLowerCase()) ||
                            String.valueOf(t.getDistance()).contains(newVal) ||
                            t.getStatus().name().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toList());
            renderTrajets(filtered);
        });
    }

    private void setupAddButton() {
        addTrajetButton.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_trajet.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();
                loadTrajets();
            } catch (IOException ex) {
                showAlert("Erreur", "Impossible d'ouvrir la fenêtre d'ajout");
            }
        });
    }

    private void editTrajet(Trajet trajet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modifier_trajet.fxml"));
            Parent root = loader.load();
            ModifierTrajetController controller = loader.getController();
            controller.setTrajet(trajet);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadTrajets();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur");
        }
    }

    private void deleteTrajet(Trajet trajet) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce trajet ?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                trajetCRUD.delete(trajet);
                loadTrajets();
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression");
            }
        }
    }

    private void updateStatistics() {
        // Update total distance
        if (totalDistanceLabel != null) {
            totalDistanceLabel.setText("0 km");
        }
        if (completionRateLabel != null) {
            completionRateLabel.setText("0%");
        }

        double totalDistance = allTrajets.stream()
                .mapToDouble(Trajet::getDistance)
                .sum();
        totalDistanceLabel.setText(String.format("%.1f km", totalDistance));

        // Update completion rate
        long completed = allTrajets.stream()
                .filter(t -> t.getStatus() == StatusTrajet.TERMINE)
                .count();
        double rate = (double) completed / allTrajets.size();
        completionRateLabel.setText(String.format("%.0f%%", rate * 100));
        completionRateIndicator.setProgress(rate);
    }

    private String formatTime(Time time) {
        return time.toString().substring(0, 5); // Format HH:mm
    }

    private void showDetails(Trajet trajet) {
        // Implement details view logic
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();

    }



    private void populateCharts() {
        populateTransportUsageChart();
        populateTrips24hChart();
        populateEmployeeActivityChart();
    }

    private void populateTransportUsageChart() {
        transportUsageChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        Map<String, Long> transportCounts = allTrajets.stream()
                .collect(Collectors.groupingBy(Trajet::getTypeMoyen, Collectors.counting()));

        transportCounts.forEach((type, count) ->
                series.getData().add(new XYChart.Data<>(type, count))
        );

        transportUsageChart.getData().add(series);
    }

    private void populateTrips24hChart() {
        // Implement based on time-series data (example: trips per hour)
    }

    private void populateEmployeeActivityChart() {
        // Implement based on employee activity data
    }

    private void updateTransportStatistics() {
        long distinctTransports = allTrajets.stream()
                .map(Trajet::getTypeMoyen)
                .distinct()
                .count();
        transportCountLabel.setText(String.valueOf(distinctTransports));
    }

    private void loadEfficientRoutes() throws SQLException {
        efficientRoutesContainer.getChildren().clear();
        List<Trajet> efficientTrajets = trajetCRUD.findMostEfficient(4); // Fetch top 4

        efficientTrajets.forEach(trajet -> {
            HBox routeEntry = createEfficientRouteEntry(trajet);
            efficientRoutesContainer.getChildren().add(routeEntry);
        });
    }

    private HBox createEfficientRouteEntry(Trajet trajet) {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);

        // Transport Icon
        StackPane icon = new StackPane();
        icon.setStyle("-fx-background-radius: 10; -fx-min-width: 50; -fx-min-height: 50;");

        Label iconLabel = new Label();
        iconLabel.setTextFill(Color.WHITE);
        iconLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 24;");

        switch (trajet.getTypeMoyen().toUpperCase()) {
            case "VOITURE" -> {
                icon.setStyle("-fx-background-color: #4e5bf2;");
                iconLabel.setText("V");
            }
            case "BUS" -> {
                icon.setStyle("-fx-background-color: #5cc6f2;");
                iconLabel.setText("B");
            }
            case "TRAIN" -> {
                icon.setStyle("-fx-background-color: #a7e155;");
                iconLabel.setText("T");
            }
            default -> {
                icon.setStyle("-fx-background-color: #b659e3;");
                iconLabel.setText("A");
            }
        }
        icon.getChildren().add(iconLabel);

        // Route Details
        VBox details = new VBox();
        Label route = new Label(trajet.getPointDep() + " - " + trajet.getPointArr());
        route.setTextFill(Color.WHITE);

        Label type = new Label(trajet.getTypeMoyen());
        type.setTextFill(Color.web("#9395A6"));

        details.getChildren().addAll(route, type);

        // Efficiency
        Label efficiency = new Label(trajet.getDuréeEstimé() + "%");
        efficiency.setTextFill(Color.web("#4CAF50"));

        hbox.getChildren().addAll(icon, details, efficiency);
        return hbox;
    }

    private void setUserInitials() {
        // Example: Retrieve current user's initials
        String initials = "JD"; // Replace with actual user data
        userInitialsLabel.setText(initials);
    }
}