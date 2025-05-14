package tn.esprit.Controllers;
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
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.Entities.*;
import tn.esprit.Services.CongeService;
import tn.esprit.Services.TtService;
import tn.esprit.Utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CongeEmployeController implements Initializable {
    private boolean isModification = false;
    private int originalRequestId = 0;
    // Services (using the updated ready-to-paste versions)
    private final CongeService congeService = new CongeService();
    private final TtService ttService = new TtService();

    // Current user ID (would normally be set from login)
    private Utilisateur currentUser = SessionManager.getInstance().getUtilisateurConnecte(); // Default for demonstration

    // Total allowed leave days constant (matching the Symfony TOTAL_CONGE_DAYS constant)
    private static final int TOTAL_CONGE_DAYS = 26;

    // Dashboard components
    @FXML private ComboBox<String> reasonCombo; // For the "Raison" dropdown field
    @FXML private Label availableLeaveCount;
    @FXML private Label pendingRequestsCount;
    @FXML private Label ttDaysUsedCount; // We'll use this for "Congés pris" instead
    @FXML private TableView<RequestViewModel> recentRequestsTable;
    @FXML private TableColumn<RequestViewModel, String> typeColumn;
    @FXML private TableColumn<RequestViewModel, String> periodColumn;
    @FXML private TableColumn<RequestViewModel, String> durationColumn;
    @FXML private TableColumn<RequestViewModel, String> statusColumn;
    @FXML private Button newRequestBtn, submitBtn, cancelBtn;
    @FXML private TableColumn<RequestViewModel, Void> actionsColumn;
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
        private String description; // Add this field
        private Reason reason;      // Add this field


        // Single constructor for both Congé and TT requests
        public RequestViewModel(String type, Date startDate, Date endDate, int duration, Statut status, int id, String description, Reason reason) {
            this.type = type;
            this.startDate = startDate;
            this.endDate = endDate;
            this.duration = duration;
            this.status = status;
            this.id = id;
            this.description = description;
            this.reason = reason;
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
        // Add getters
        public String getDescription() { return description; }
        public Reason getReason() { return reason; }
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
        recentRequestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        periodColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPeriod()));
        durationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDurationString()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatusString()));

        // Configure Actions column with modify and delete buttons
        actionsColumn.setCellFactory(param -> new TableCell<RequestViewModel, Void>() {
            private final Button modifyButton = createStyledButton("Modifier", "-fx-background-color: linear-gradient(to right, #4a6fdc, #825ee4); -fx-text-fill: white;");
            private final Button deleteButton = createStyledButton("Supprimer", "-fx-background-color: #e74c3c; -fx-text-fill: white;");
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

            private Button createStyledButton(String text, String style) {
                Button button = new Button(text);
                button.setStyle(
                        style +
                                "-fx-background-radius: 20px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 5px 15px;" +
                                "-fx-cursor: hand;"
                );

                // Add hover and press effects
                button.setOnMouseEntered(e -> button.setStyle(
                        style +
                                "-fx-background-radius: 20px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 5px 15px;" +
                                "-fx-cursor: hand;" +
                                "-fx-opacity: 0.8;"
                ));

                button.setOnMouseExited(e -> button.setStyle(
                        style +
                                "-fx-background-radius: 20px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 5px 15px;" +
                                "-fx-cursor: hand;" +
                                "-fx-opacity: 1;"
                ));

                return button;
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

                    // Add opacity effect for disabled buttons
                    modifyButton.setStyle(
                            modifyButton.getStyle() +
                                    (isPending ? "" : "-fx-opacity: 0.5;")
                    );
                    deleteButton.setStyle(
                            deleteButton.getStyle() +
                                    (isPending ? "" : "-fx-opacity: 0.5;")
                    );

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
            Stage modifyStage = new Stage();
            modifyStage.initModality(Modality.APPLICATION_MODAL);
            modifyStage.initStyle(StageStyle.UTILITY);

            if (request.getType().equals("Congé")) {
                // Load the leave request form
                loader = new FXMLLoader(getClass().getResource("/fxml/CongeRequest.fxml"));
                Parent modifyView = loader.load();

                // Get the controller of the loaded form
                CongeEmployeController controller = loader.getController();

                // Make sure the controller is properly initialized before accessing its fields
                if (controller == null) {
                    showAlert("Erreur: Impossible de charger le contrôleur de formulaire");
                    return;
                }

                // Set the form title to indicate modification
                modifyStage.setTitle("Modifier une demande de congé");

                // Convert java.util.Date to LocalDate safely
                LocalDate startDate = ((java.sql.Date)request.getStartDate()).toLocalDate();
                LocalDate endDate = ((java.sql.Date)request.getEndDate()).toLocalDate();

                // Check for null fields before setting values
                if (controller.startDatePicker != null) {
                    controller.startDatePicker.setValue(startDate);
                }

                if (controller.endDatePicker != null) {
                    controller.endDatePicker.setValue(endDate);
                }

                if (controller.daysRequestedField != null) {
                    controller.daysRequestedField.setText(String.valueOf(request.getDuration()));
                } else {
                    System.err.println("Warning: daysRequestedField is null in CongeEmployeController");
                }

                // Set the reason in the combo box
                if (request.getReason() != null && controller.leaveTypeCombo != null) {
                    // Get the enum string
                    String reasonEnum = request.getReason().toString();

                    // Find the matching reason string from the combo box items
                    String reasonToSelect = null;

                    // Map from enum values to displayed values
                    switch(request.getReason()) {
                        case CONGE_PAYE:
                            reasonToSelect = "CONGE PAYE";
                            break;
                        case CONGE_SANS_SOLDE:
                            reasonToSelect = "CONGE SANS SOLDE";
                            break;
                        case MALADIE:
                            reasonToSelect = "MALADIE";
                            break;
                        case MATERNITE:
                            reasonToSelect = "MATERNITE";
                            break;
                        case FORMATION:
                            reasonToSelect = "FORMATION";
                            break;
                        case AMMENAGEMENT:
                            reasonToSelect = "AMMENAGEMENT";
                            break;
                        default:
                            // If no match found, try using the enum value directly
                            reasonToSelect = reasonEnum;
                    }

                    // Set the selection in the combo box
                    if (reasonToSelect != null) {
                        controller.leaveTypeCombo.getSelectionModel().select(reasonToSelect);
                    }
                }
                // Set the description/reason text
                if (controller.reasonField != null) {
                    controller.reasonField.setText(request.getDescription() != null ?
                            request.getDescription() : "");
                }

                // Add a hidden field or property to track that this is a modification
                // and store the original request ID
                controller.isModification = true;
                controller.originalRequestId = request.getId();

                modifyStage.setScene(new Scene(modifyView));
            } else if (request.getType().equals("TT")) {
                // Load the telecommuting form
                loader = new FXMLLoader(getClass().getResource("/fxml/ttRequest.fxml"));
                Parent modifyView = loader.load();

                // Get the controller
                CongeEmployeController controller = loader.getController();

                // Make sure the controller is properly initialized
                if (controller == null) {
                    showAlert("Erreur: Impossible de charger le contrôleur de formulaire");
                    return;
                }

                // Set the form title to indicate modification
                modifyStage.setTitle("Modifier une demande de télétravail");

                // Convert java.util.Date to LocalDate safely
                LocalDate startDate = ((java.sql.Date)request.getStartDate()).toLocalDate();
                LocalDate endDate = ((java.sql.Date)request.getEndDate()).toLocalDate();

                // Check for null fields before setting values
                if (controller.leaveStartDate != null) {
                    controller.leaveStartDate.setValue(startDate);
                }

                if (controller.leaveEndDate != null) {
                    controller.leaveEndDate.setValue(endDate);
                }

                if (controller.numberOfDays != null) {
                    controller.numberOfDays.setText(String.valueOf(request.getDuration()));
                } else {
                    System.err.println("Warning: numberOfDays is null in CongeEmployeController");
                }

                // Add a hidden field or property to track that this is a modification
                // and store the original request ID
                controller.isModification = true;
                controller.originalRequestId = request.getId();

                modifyStage.setScene(new Scene(modifyView));
            } else {
                showAlert("Type de demande inconnu");
                return;
            }

            modifyStage.centerOnScreen();
            modifyStage.showAndWait();

            // After closing the modify form, refresh the requests list
            loadRequestsFromDatabase();
            updateDashboardCounters();

        } catch (IOException e) {
            showAlert("Erreur lors du chargement du formulaire de modification: " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            showAlert("Erreur: Un élément requis est introuvable: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Prepopulate leave form fields for modification
     */
    private void prepopulateLeaveForm(CongeEmployeController controller, RequestViewModel request) {
        try {
            // Convert java.util.Date to LocalDate correctly
            LocalDate startDate = ((java.sql.Date)request.getStartDate()).toLocalDate();
            LocalDate endDate = ((java.sql.Date)request.getEndDate()).toLocalDate();

            // Populate form fields
            controller.startDatePicker.setValue(startDate);
            controller.endDatePicker.setValue(endDate);

            // Additional fields to prepopulate
            controller.daysRequestedField.setText(String.valueOf(request.getDuration()));
            controller.reasonField.setText(request.getDescription() != null ? request.getDescription() : "");

            // Try to select the right reason in the combo box if available
            // Try to select the right reason in the combo box if available
            // Populate the Type de congé dropdown based on the Reason enum
            if (request.getReason() != null && controller.leaveTypeCombo != null) {
                String typeToSelect = null;

                // Map from reason enum to type dropdown values
                switch(request.getReason()) {
                    case CONGE_PAYE:
                        typeToSelect = "CONGE PAYE";
                        break;
                    case CONGE_SANS_SOLDE:
                        typeToSelect = "CONGE SANS SOLDE";
                        break;
                    case MALADIE:
                        typeToSelect = "MALADIE";
                        break;
                    case MATERNITE:
                        typeToSelect = "MATERNITE";
                        break;
                    case FORMATION:
                        typeToSelect = "FORMATION";
                        break;
                    case AMMENAGEMENT:
                        typeToSelect = "AMMENAGEMENT";
                        break;
                }

                if (typeToSelect != null) {
                    controller.leaveTypeCombo.getSelectionModel().select(typeToSelect);
                }
            }
        } catch (Exception e) {
            System.err.println("Error prepopulating leave form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prepopulate telecommuting form fields for modification
     */
    private void prepopulateTelecommutingForm(CongeEmployeController controller, RequestViewModel request) {
        // Convert Date to LocalDate
        LocalDate startDate = request.getStartDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate endDate = request.getEndDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Populate telecommuting form fields
        controller.leaveStartDate.setValue(startDate);
        controller.leaveEndDate.setValue(endDate);

        controller.numberOfDays.setText(String.valueOf(request.getDuration()));

        // Optional: Set status if applicable
        if (controller.statusComboBox != null) {
            controller.statusComboBox.setValue(request.getStatus().name());
        }

        // Set a flag or tag to indicate this is a modification
        // You might want to add a method or field in the form to track this
        // For example:
        // controller.setModificationMode(true);
        // controller.setOriginalRequestId(request.getId());
    }

    /**
     * Handle deletion of a request.
     */
    private void handleDeleteRequest(RequestViewModel request) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cette demande ?");

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
                    showInformation("Demande supprimée avec succès");
                } else {
                    showAlert("Échec de la suppression de la demande");
                }
            } catch (SQLException e) {
                showAlert("Erreur lors de la suppression de la demande: " + e.getMessage());
            }
        }
    }

    /**
     * Load requests from the database.
     */
    // The issue is in the loadRequestsFromDatabase() method
// Let's update the part that creates RequestViewModel for Tt objects

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
                        conge.getId_Conge(),
                        conge.getDescription(),
                        conge.getReason()  // This correctly passes the Reason enum
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
                        tt.getId_tt(),
                        "Télétravail",  // Default description for telecommuting
                        null  // Add null for reason as TT doesn't have a reason field
                ));
            }

            // Sort requests by start date (most recent first)
            requests.sort((r1, r2) -> r2.getStartDate().compareTo(r1.getStartDate()));
        } catch (SQLException e) {
            showAlert("Erreur lors du chargement des demandes: " + e.getMessage());
        }
        // Adjust table height based on number of rows
        adjustTableHeight();
    }
    /**
     * Adjust table height based ona the number of rows.
     */
    private void adjustTableHeight() {
        // Calculate the preferred height based on number of rows
        // Use a base height for empty table plus height per row
        final int ROW_HEIGHT = 35; // Adjust based on your CSS
        final int HEADER_HEIGHT = 30; // Adjust based on your header height
        final int MINIMUM_TABLE_HEIGHT = 100; // Minimum height if table is empty

        int rowCount = requests.size();
        int calculatedHeight = HEADER_HEIGHT + (rowCount * ROW_HEIGHT);

        // Set a minimum height if table is empty or has few rows
        int newHeight = Math.max(calculatedHeight, MINIMUM_TABLE_HEIGHT);

        // Limit maximum height if needed
        int maxHeight = 400; // Maximum height you want for the table
        newHeight = Math.min(newHeight, maxHeight);

        // Apply the calculated height
        recentRequestsTable.setPrefHeight(newHeight);
        recentRequestsTable.setMinHeight(newHeight);
        recentRequestsTable.setMaxHeight(newHeight);
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
                "Conge",
                "CTELETRAVAIL",
                "AUTORISATION"

        ));
        // Initialize the reason combo (Raison field)
        if (reasonCombo != null) {
            reasonCombo.setItems(FXCollections.observableArrayList(
                    "CONGE PAYE",
                    "CONGE SANS SOLDE",
                    "MALADIE",
                    "MATERNITE",
                    "FORMATION",
                    "AMMENAGEMENT"
            ));
        }

        startTimeCombo.setValue("AM");
        endTimeCombo.setValue("PM");
        leaveTypeCombo.setValue("CONGE PAYE");

        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateDaysRequested());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateDaysRequested());
        startTimeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDaysRequested());
        endTimeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDaysRequested());

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(5));

        requestTypeToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == telecommutingRadio) {
                switchToTelecommutingForm();
            }
        });

        // Création du bouton submit
        Button submitBtn = new Button("Soumettre");
        submitBtn.getStyleClass().add("btn-submit");
        submitBtn.setOnAction(this::handleSubmitLeave);

        // Créer un HBox pour aligner le bouton à droite
        HBox buttonBox = new HBox(submitBtn);
        buttonBox.setStyle("-fx-alignment: center-right;");

        // Trouver le conteneur de formulaire et ajouter le bouton
        GridPane parentGridPane = (GridPane) leaveFormContainer.getParent();
        parentGridPane.add(buttonBox, 0, 8, 2, 1);
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

        // Programmatically create submit button.
        Button submitBtn = new Button("Soumettre");
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

        String[] dayHeaders = {"Lu", "Ma", "Me", "Je", "Ve", "Sa", "Di"};
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
                System.out.println("Date sélectionnée: " + date);
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
     * This method calculates:
     * - Number of pending requests
     * - Total days of accepted leave requests (congesPris)
     * - Available leave days as TOTAL_CONGE_DAYS - congesPris
     */
    private void updateDashboardCounters() {
        // Count pending requests
        long pendingCount = requests.stream()
                .filter(r -> r.getStatus().equals(Statut.EN_COURS))
                .count();
        pendingRequestsCount.setText(String.valueOf(pendingCount));

        // Calculate "Congés pris" (count of accepted leave requests)
        long congesCount = requests.stream()
                .filter(r -> r.getType().equals("Congé") && r.getStatus().equals(Statut.ACCEPTE))
                .count();

        // Calculate total days used from accepted leave requests
        int totalDaysUsed = requests.stream()
                .filter(r -> r.getType().equals("Congé") && r.getStatus().equals(Statut.ACCEPTE))
                .mapToInt(RequestViewModel::getDuration)
                .sum();

        // Update the "Congés pris" label with the count of accepted requests
        ttDaysUsedCount.setText(String.valueOf(congesCount));

        // Calculate remaining leave days by subtracting total days used
        int availableLeave = Math.max(0, TOTAL_CONGE_DAYS - totalDaysUsed);
        availableLeaveCount.setText(String.valueOf(availableLeave));
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
            popupStage.setTitle("Nouvelle demande ");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ttRequest.fxml"));
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
     * Handle leave request submission (for both new requests and modifications).
     */
    @FXML
    private void handleSubmitLeave(ActionEvent event) {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null ||
                leaveTypeCombo.getValue() == null || reasonField.getText().isEmpty()) {
            showAlert("Veuillez remplir tous les champs");
            return;
        }

        try {
            Date startDate = Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Calculate days between dates instead of relying on daysRequestedField
            long daysBetween = ChronoUnit.DAYS.between(
                    startDatePicker.getValue(),
                    endDatePicker.getValue()) + 1; // +1 to include both start and end dates
            int daysRequested = (int) daysBetween;

            // Convert string selection to Reason enum
            Reason reason;
            String leaveType = leaveTypeCombo.getValue();
            if (leaveType.contains("CONGE PAYE")) {
                reason = Reason.CONGE_PAYE;
            } else if (leaveType.contains("CONGE SANS SOLDE")) {
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

            int result;

            // Check if this is a new request or a modification
            if (isModification && originalRequestId > 0) {
                // This is a modification of an existing request
                Conge conge = new Conge(
                        originalRequestId,  // Use the original ID for the update
                        startDate,
                        endDate,
                        daysRequested,
                        Statut.EN_COURS, // Reset status to pending when modified
                        currentUser.getId_employe(),
                        Type.CONGE,
                        reason,
                        reasonField.getText()
                );

                result = congeService.update(conge);
                if (result > 0) {
                    showInformation("Demande de congé modifiée avec succès !");
                    closeStage(event);  // Close the form
                } else {
                    showAlert("Échec de la modification de la demande de congé.");
                }
            } else {
                // This is a new request
                Conge conge = new Conge(
                        startDate,
                        endDate,
                        daysRequested,
                        Statut.EN_COURS,
                        currentUser.getId_employe(),
                        Type.CONGE,
                        reason,
                        reasonField.getText()
                );

                result = congeService.add(conge);
                if (result > 0) {
                    showInformation("Demande de congé soumise avec succès !");
                    closeStage(event);  // Close the form
                } else {
                    showAlert("Échec de l'ajout de la demande de congé.");
                }
            }
        } catch (Exception e) {
            showAlert("Erreur lors de la soumission de la demande de congé: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Handle telecommuting request submission (for both new requests and modifications).
     */
    @FXML
    private void handleSubmitTelecommuting(ActionEvent event) {
        if (leaveStartDate.getValue() == null || leaveEndDate.getValue() == null) {
            showAlert("Veuillez remplir tous les champs");
            return;
        }
        try {
            Date startDate = Date.from(leaveStartDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(leaveEndDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            int daysRequested = Integer.parseInt(numberOfDays.getText());

            int result;

            // Check if this is a new request or a modification
            if (isModification && originalRequestId > 0) {
                // This is a modification of an existing request
                Tt tt = new Tt(
                        originalRequestId,  // Use the original ID for the update
                        startDate,
                        endDate,
                        daysRequested,
                        Statut.EN_COURS, // Reset status to pending when modified
                        currentUser.getId_employe()
                );

                result = ttService.update(tt);
                if (result > 0) {
                    showInformation("Demande de télétravail modifiée avec succès !");
                    closeStage(event);  // Close the form
                } else {
                    showAlert("Échec de la modification de la demande de télétravail.");
                }
            } else {
                // This is a new request
                Tt tt = new Tt(
                        0,  // New request gets default ID (will be assigned by database)
                        startDate,
                        endDate,
                        daysRequested,
                        Statut.EN_COURS,
                        currentUser.getId_employe()
                );

                result = ttService.add(tt);
                if (result > 0) {
                    showInformation("Demande de télétravail soumise avec succès !");
                    closeStage(event);  // Close the form
                } else {
                    showAlert("Échec de l'ajout de la demande de télétravail.");
                }
            }
        } catch (Exception e) {
            showAlert("Erreur lors de la soumission de la demande de télétravail: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Close the current stage.
     */
    private void closeStage(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
    /**
     * Refresh the dashboard after changes.
     */
    public void refreshDashboard() {
        loadRequestsFromDatabase();
        updateDashboardCounters();
        adjustTableHeight();
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