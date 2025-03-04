package tn.esprit.Controllers;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.DateCell;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import tn.esprit.Entities.*;
import tn.esprit.Services.CongeService;
import tn.esprit.Services.TtService;
import tn.esprit.Utils.SessionManager;
import javafx.scene.layout.Pane;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CongeEmployeController implements Initializable {

    // Services (using the updated ready-to-paste versions)
    private final CongeService congeService = new CongeService();
    private final TtService ttService = new TtService();

    // Current user ID (would normally be set from login)
    Utilisateur currentUser = SessionManager.getInstance().getUtilisateurConnecte(); // Default for demonstration

    // Dashboard components
    @FXML private Label availableLeaveCount;
    @FXML private Label pendingRequestsCount;
    @FXML private Label ttDaysUsedCount;
    @FXML private TableView<RequestViewModel> recentRequestsTable;
    @FXML private TableColumn<RequestViewModel, String> typeColumn;
    @FXML private TableColumn<RequestViewModel, String> periodColumn;
    @FXML private TableColumn<RequestViewModel, String> durationColumn;
    @FXML private TableColumn<RequestViewModel, String> statusColumn;
    @FXML private Button newRequestBtn,submitBtn,cancelBtn;
    @FXML
    private TableColumn<RequestViewModel, Void> actionsColumn;
    @FXML private BorderPane borderPane;
    // New Request (Leave) form components
    @FXML private ToggleGroup requestTypeToggle;
    @FXML private RadioButton leaveRequestRadio;
    @FXML private RadioButton telecommutingRadio;
    @FXML private GridPane leaveFormFields;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> startTimeCombo;
    @FXML private ComboBox<String> endTimeCombo;
    @FXML private ComboBox<String> leaveTypeCombo;
    @FXML private TextField daysRequestedField;
    @FXML private TextArea reasonField;
    // (Cancel and Submit buttons for the leave form remain defined via FXML if needed)

    // Telecommuting form components
    @FXML private DatePicker leaveStartDate;
    @FXML private DatePicker leaveEndDate;
    @FXML private TextField numberOfDays;
    @FXML private ComboBox<String> statusComboBox;
    // Remove cancelButton and submitButton FXML fields because they will be generated programmatically

    // Container for telecommuting form buttons (make sure this container is defined in your FXML)
    @FXML private VBox telecommutingFormContainer;
    @FXML private VBox leaveFormContainer;
    // Dynamic Calendar components (for telecommuting form)
    @FXML private GridPane calendarGrid;
    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    @FXML private Label monthLabel;

    // Current month for dynamic calendar
    private LocalDate currentMonth;

    // Model class for request data view
    public static class RequestViewModel {
        private String type;
        private Date startDate;
        private Date endDate;
        private int duration;
        private Statut status;
        private int id;

        // Single constructor for both Congé and TT requests
        public RequestViewModel(String type, Date startDate, Date endDate, int duration, Statut status, int id) {
            this.type = type;
            this.startDate = startDate;
            this.endDate = endDate;
            this.duration = duration;
            this.status = status;
            this.id = id;
        }

        public String getType() { return type; }
        public Date getStartDate() { return startDate; }
        public Date getEndDate() { return endDate; }
        public int getDuration() { return duration; }
        public Statut getStatus() { return status; }
        public int getId() { return id; }

        public String getPeriod() {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            return formatter.format(startDate) + " - " + formatter.format(endDate);
        }

        public String getDurationString() {
            return duration + (duration == 1 ? " day" : " days");
        }

        public String getStatusString() {
            return status.name();
        }
    }

    private ObservableList<RequestViewModel> requests = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Determine which view is loaded based on FXML elements present
        if (recentRequestsTable != null) {
            initializeDashboard();
        } else if (requestTypeToggle != null) {
            initializeNewRequestForm();
        } else if (leaveStartDate != null) {
            initializeTelecommutingForm();
        }
    }

    /**
     * Initialize the dashboard view.
     */
    private void initializeDashboard() {
        loadRequestsFromDatabase();

        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        periodColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPeriod()));
        durationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDurationString()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusString()));

        // Configure Actions column with modify and delete buttons
        actionsColumn.setCellFactory(param -> new TableCell<RequestViewModel, Void>() {
            private final Button modifyButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox actionButtons = new HBox(10, modifyButton, deleteButton);

            {
                modifyButton.setOnAction(event -> {
                    RequestViewModel request = getTableView().getItems().get(getIndex());
                    handleModifyRequest(request);
                });

                deleteButton.setOnAction(event -> {
                    RequestViewModel request = getTableView().getItems().get(getIndex());
                    handleDeleteRequest(request);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Disable buttons if request is not in PENDING status
                    boolean isPending = getTableView().getItems().get(getIndex()).getStatus() == Statut.EN_COURS;
                    modifyButton.setDisable(!isPending);
                    deleteButton.setDisable(!isPending);

                    setGraphic(actionButtons);
                }
            }
        });

        recentRequestsTable.setItems(requests);
        updateDashboardCounters();
        newRequestBtn.setOnAction(this::handleNewRequest);
    }

    /**
     * Handle modification of a request.
     */
    private void handleModifyRequest(RequestViewModel request) {
        try {
            FXMLLoader loader;
            if (request.getType().equals("Congé")) {
                loader = new FXMLLoader(getClass().getResource("/fxml/CongeRequest.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/fxml/TtRequest.fxml"));
            }

            Parent modifyView = loader.load();

            // You might want to pass the selected request to the modify form's controller
            // Uncomment and adjust based on your specific implementation
            // Object controller = loader.getController();
            // if (controller instanceof CongeRequestController) {
            //     ((CongeRequestController) controller).setRequestToModify(request);
            // }

            Stage stage = (Stage) recentRequestsTable.getScene().getWindow();
            stage.setScene(new Scene(modifyView));
            stage.show();
        } catch (IOException e) {
            showAlert("Error loading modify request form: " + e.getMessage());
        }
    }

    /**
     * Handle deletion of a request.
     */
    private void handleDeleteRequest(RequestViewModel request) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Are you sure you want to delete this request?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean deleted = false;
                if (request.getType().equals("Congé")) {
                    // Create a Conge object with just the ID
                    Conge conge = new Conge();
                    conge.setId_Conge(request.getId());
                    deleted = congeService.delete(conge) > 0;
                } else {
                    // Create a Tt object with just the ID
                    Tt tt = new Tt();
                    tt.setId_tt(request.getId());
                    deleted = ttService.delete(tt) > 0;
                }

                if (deleted) {
                    requests.remove(request);
                    updateDashboardCounters();
                    showInformation("Request deleted successfully");
                } else {
                    showAlert("Failed to delete request");
                }
            } catch (SQLException e) {
                showAlert("Error deleting request: " + e.getMessage());
            }
        }
    }

    /**
     * Load requests from the database.
     */
    private void loadRequestsFromDatabase() {
        requests.clear();
        try {
            // Load leave requests
            List<Conge> conges = congeService.showAll().stream()
                    .filter(c -> c.getId_employe() == currentUser.getId_employe())
                    .collect(Collectors.toList());

            for (Conge conge : conges) {
                requests.add(new RequestViewModel(
                        "Congé",
                        conge.getLeave_start(),
                        conge.getLeave_end(),
                        conge.getNumber_of_days(),
                        conge.getStatus(),
                        conge.getId_Conge()
                ));
            }

            // Load telecommuting requests (TT)
            List<Tt> tts = ttService.showAll().stream()
                    .filter(t -> t.getId_employe() == currentUser.getId_employe())
                    .collect(Collectors.toList());

            for (Tt tt : tts) {
                requests.add(new RequestViewModel(
                        "TT",
                        tt.getLeave_start(),
                        tt.getLeave_end(),
                        tt.getNumber_of_days(),
                        tt.getStatus(),
                        tt.getId_tt()
                ));
            }
            // Sort requests by start date (most recent first)
            requests.sort((r1, r2) -> r2.getStartDate().compareTo(r1.getStartDate()));
        } catch (SQLException e) {
            showAlert("Error loading requests: " + e.getMessage());
        }
    }

    /**
     * Initialize the new request (leave) form.
     */
    private void initializeNewRequestForm() {
        setupDatePicker(startDatePicker);
        setupDatePicker(endDatePicker);

        startTimeCombo.setItems(FXCollections.observableArrayList("AM", "PM"));
        endTimeCombo.setItems(FXCollections.observableArrayList("AM", "PM"));

        leaveTypeCombo.setItems(FXCollections.observableArrayList(
                "Paid Leave (CONGE_PAYE)",
                "Unpaid Leave (CONGE_SANS_SOLDE)",
                "Sick Leave (MALADIE)",
                "Maternity Leave (MATERNITE)",
                "Training (FORMATION)",
                "Schedule Adjustment (AMMENAGEMENT)"
        ));

        startTimeCombo.setValue("AM");
        endTimeCombo.setValue("PM");
        leaveTypeCombo.setValue("Paid Leave (CONGE_PAYE)");

        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateDaysRequested());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateDaysRequested());
        startTimeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDaysRequested());
        endTimeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDaysRequested());

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(5));

        requestTypeToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == telecommutingRadio) {
                switchToTelecommutingForm();
                switchToLeaveForm();
            }
        });

        // Création du bouton submit
        Button submitBtn = new Button("Submit");
        submitBtn.getStyleClass().add("btn-submit");
        submitBtn.setOnAction(this::handleSubmitLeave);

        // Créer un HBox avec un spacer pour pousser le bouton à droite
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonContainer = new HBox(spacer, submitBtn);
        submitBtn.setStyle("-fx-start-margin: 1000px 0 0 0;"); // Ajoute un peu d'espace en haut

        // Ajouter le conteneur de bouton à leaveFormContainer dans le GridPane
        leaveFormContainer.getChildren().add(buttonContainer);
    }

    /**
     * Initialize the telecommuting form.
     *
     * Note: The cancel and submit buttons are created programmatically here and added
     * to the telecommutingFormContainer (which must be defined in your FXML).
     */
    private void initializeTelecommutingForm() {
        setupDatePicker(leaveStartDate);
        setupDatePicker(leaveEndDate);

        statusComboBox.setItems(FXCollections.observableArrayList(
                Statut.EN_COURS.name(),
                Statut.ACCEPTE.name(),
                Statut.REFUSE.name()
        ));
        statusComboBox.setValue(Statut.EN_COURS.name());

        leaveStartDate.valueProperty().addListener((obs, oldVal, newVal) -> updateTelecommutingDays());
        leaveEndDate.valueProperty().addListener((obs, oldVal, newVal) -> updateTelecommutingDays());

        leaveStartDate.setValue(LocalDate.now());
        leaveEndDate.setValue(LocalDate.now().plusDays(2));

        updateTelecommutingDays();

        // Initialize the dynamic calendar view.
        initializeCalendar();

        // Programmatically create cancel and submit buttons.

        Button submitBtn = new Button("Submit");
        submitBtn.getStyleClass().add("btn-submit");

        // Set event handlers for the buttons.
        submitBtn.setOnAction(this::handleSubmitTelecommuting);

        // Create an HBox to hold the buttons and add it to the container.
        HBox buttonBox = new HBox(10, submitBtn);
        buttonBox.setStyle("-fx-alignment: center-right;");
        telecommutingFormContainer.getChildren().add(buttonBox);
    }

    /**
     * Initialize the dynamic calendar.
     */
    private void initializeCalendar() {
        currentMonth = LocalDate.now().withDayOfMonth(1);
        updateCalendar();

        prevMonthButton.setOnAction(event -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });
        nextMonthButton.setOnAction(event -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });
    }

    /**
     * Update the calendar grid based on currentMonth.
     */
    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthLabel.setText(currentMonth.format(formatter));

        String[] dayHeaders = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
        for (int i = 0; i < dayHeaders.length; i++) {
            Label header = new Label(dayHeaders[i]);
            header.getStyleClass().add("calendar-day-header");
            calendarGrid.add(header, i, 0);
        }

        int startDayOfWeek = currentMonth.getDayOfWeek().getValue(); // Monday=1, Sunday=7
        int column = startDayOfWeek - 1;
        int row = 1;
        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.withDayOfMonth(day);
            Button dayButton = new Button(String.valueOf(day));
            dayButton.getStyleClass().add("calendar-day");

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                dayButton.getStyleClass().clear();
                dayButton.getStyleClass().add("calendar-day-weekend");
            }

            // Optional: add a click handler for additional behavior.
            dayButton.setOnAction(e -> {
                System.out.println("Selected date: " + date);
                // Update selection or DatePicker as needed.
            });

            calendarGrid.add(dayButton, column, row);
            column++;
            if (column > 6) {
                column = 0;
                row++;
            }
        }
    }

    /**
     * Update dashboard counters.
     */
    private void updateDashboardCounters() {
        long pendingCount = requests.stream()
                .filter(r -> r.getStatus().equals(Statut.EN_COURS))
                .count();
        pendingRequestsCount.setText(String.valueOf(pendingCount));

        int ttDaysUsed = requests.stream()
                .filter(r -> r.getType().equals("TT") && r.getStatus().equals(Statut.ACCEPTE))
                .mapToInt(RequestViewModel::getDuration)
                .sum();
        ttDaysUsedCount.setText(String.valueOf(ttDaysUsed));

        int totalLeaveAllowed = 30;
        int leaveUsed = requests.stream()
                .filter(r -> r.getType().equals("Congé") && r.getStatus().equals(Statut.ACCEPTE))
                .mapToInt(RequestViewModel::getDuration)
                .sum();
        availableLeaveCount.setText(String.valueOf(totalLeaveAllowed - leaveUsed));
    }

    /**
     * Calculate and update days requested (for leave form).
     */
    private void updateDaysRequested() {
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            if (!end.isBefore(start)) {
                long days = calculateBusinessDays(start, end);
                double totalDays = days;
                if (startTimeCombo.getValue().equals("PM")) totalDays -= 0.5;
                if (endTimeCombo.getValue().equals("AM")) totalDays -= 0.5;

                // Convert to int and display as integer
                int totalDaysInt = (int)totalDays;
                daysRequestedField.setText(String.valueOf(totalDaysInt));
            } else {
                daysRequestedField.setText("0");
            }
        }
    }

    /**
     * Calculate and update telecommuting days.
     */
    private void updateTelecommutingDays() {
        if (leaveStartDate.getValue() != null && leaveEndDate.getValue() != null) {
            LocalDate start = leaveStartDate.getValue();
            LocalDate end = leaveEndDate.getValue();

            if (!end.isBefore(start)) {
                long days = calculateBusinessDays(start, end);
                numberOfDays.setText(String.valueOf(days));
            } else {
                numberOfDays.setText("0");
            }
        }
    }

    /**
     * Calculate business days (excluding weekends).
     */
    private long calculateBusinessDays(LocalDate startDate, LocalDate endDate) {
        long days = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (date.getDayOfWeek().getValue() < 6) {
                days++;
            }
            date = date.plusDays(1);
        }
        return days;
    }

    /**
     * Configure a DatePicker with a custom format and disable past dates.
     */
    private void setupDatePicker(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, formatter) : null;
            }
        });
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.compareTo(LocalDate.now()) < 0);
            }
        });
    }

    /**
     * Handle navigation to the new request form.
     */
    @FXML
    private void handleNewRequest(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CongeRequest.fxml"));
            Parent newRequestView = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UTILITY);
            popupStage.setTitle("New Leave Request");
            popupStage.setScene(new Scene(newRequestView));
            popupStage.centerOnScreen();
            popupStage.showAndWait();
        } catch (IOException e) {
            showAlert("Error loading new request form: " + e.getMessage());
        }
    }


    /**
     * Switch to telecommuting form.
     */
    private void switchToTelecommutingForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TtRequest.fxml"));
            Parent telecommutingView = loader.load();
            Stage stage = (Stage) leaveRequestRadio.getScene().getWindow();
            stage.setScene(new Scene(telecommutingView));
            stage.show();
        } catch (IOException e) {
            showAlert("Error loading telecommuting form: " + e.getMessage());
        }
    }

    private void switchToLeaveForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CongeRequest.fxml"));
            Parent telecommutingView = loader.load();
            Stage stage = (Stage) telecommutingRadio.getScene().getWindow();
            stage.setScene(new Scene(telecommutingView));
            stage.show();
        } catch (IOException e) {
            showAlert("Error loading telecommuting form: " + e.getMessage());
        }
    }
    /**
     * Handle leave request submission.
     */
    @FXML
    private void handleSubmitLeave(ActionEvent event) {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null ||
                leaveTypeCombo.getValue() == null || reasonField.getText().isEmpty()) {
            showAlert("Please fill in all fields");
            return;
        }
        try {
            Date startDate = Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            int daysRequested = (Integer.parseInt(daysRequestedField.getText()));

            Reason reason;
            String leaveType = leaveTypeCombo.getValue();
            if (leaveType.contains("CONGE_PAYE")) {
                reason = Reason.CONGE_PAYE;
            } else if (leaveType.contains("CONGE_SANS_SOLDE")) {
                reason = Reason.CONGE_SANS_SOLDE;
            } else if (leaveType.contains("MALADIE")) {
                reason = Reason.MALADIE;
            } else if (leaveType.contains("MATERNITE")) {
                reason = Reason.MATERNITE;
            } else if (leaveType.contains("FORMATION")) {
                reason = Reason.FORMATION;
            } else {
                reason = Reason.AMMENAGEMENT;
            }

            Conge conge = new Conge(
                    startDate,
                    endDate,
                    daysRequested,
                    Statut.EN_COURS,
                    currentUser.getId_employe(),
                    reason,
                    reasonField.getText()
            );

            int result = congeService.add(conge);
            if (result > 0) {
                showInformation("Leave request submitted successfully!");
                navigateToDashboard(event);
            } else {
                showAlert("Failed to submit leave request.");
            }
        } catch (Exception e) {
            showAlert("Error submitting leave request: " + e.getMessage());
        }
    }

    /**
     * Handle telecommuting request submission.
     */
    @FXML
    private void handleSubmitTelecommuting(ActionEvent event) {
        if (leaveStartDate.getValue() == null || leaveEndDate.getValue() == null) {
            showAlert("Please fill in all fields");
            return;
        }
        try {
            Date startDate = Date.from(leaveStartDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(leaveEndDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            int daysRequested = Integer.parseInt(numberOfDays.getText());

            Tt tt = new Tt(
                    0,
                    startDate,
                    endDate,
                    daysRequested,
                    Statut.EN_COURS,
                    currentUser.getId_employe()
            );

            int result = ttService.add(tt);
            if (result > 0) {
                showInformation("Telecommuting request submitted successfully!");
                navigateToDashboard(event);
            } else {
                showAlert("Failed to submit telecommuting request.");
            }
        } catch (Exception e) {
            showAlert("Error submitting telecommuting request: " + e.getMessage());
        }
    }

    /**
     * Navigate to the dashboard view.
     */
    private void navigateToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashEmployee.fxml"));
            Parent dashboardView = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(dashboardView));
            stage.show();
        } catch (IOException e) {
            showAlert("Error returning to dashboard: " + e.getMessage());
        }
    }

    /**
     * Display an error alert.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Display an information alert.
     */
    private void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
