package tn.esprit.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Services.AbsenceService;
import tn.esprit.Services.SmsService;

import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EmployeeAbsenceController implements Initializable {

    @FXML
    private DatePicker filterDatePicker;
    @FXML
    private TableView<Absence> absenceTable;
    @FXML
    private TableColumn<Absence, LocalDate> dateColumn;
    @FXML
    private TableColumn<Absence, String> statusColumn;
    @FXML
    private Button registerPresentButton;
    @FXML
    private Button registerAbsentButton;
    @FXML
    private Button registerLeaveButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Label currentDateLabel;
    @FXML
    private Button applyFilterButton;
    @FXML
    private Button resetFilterButton;

    private final AbsenceService absenceService = new AbsenceService();
    private final SmsService smsService = new SmsService();
    // Add a class to store employee information
    private static final String EMPLOYEE_NAME = "John Doe"; // Replace with actual employee name or fetch from DB

    // Hard-coded employee ID for testing; replace with a real ID as needed.
    private static final int EMPLOYEE_ID = 1;
    private LocalDate currentDate = LocalDate.now();

    // Liste pour conserver toutes les absences sans filtre
    private List<Absence> allAbsenceRecords;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifiez que filterDatePicker n'est pas null avant d'appeler des méthodes dessus
        if (filterDatePicker != null) {
            filterDatePicker.setPromptText("Choisir une date");
        } else {
            System.err.println("filterDatePicker est null");
        }
        // Set column resize policy
        absenceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configure table columns.
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(getDisplayText(data.getValue().getStatus())));

        // Display the current date.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        currentDateLabel.setText("Date: " + currentDate.format(formatter));

        // Setup button actions.
        setupButtons();
        // Configuration du format de date pour le DatePicker
        String pattern = "dd/MM/yyyy";
        filterDatePicker.setPromptText(pattern.toLowerCase());

        // Définir un convertisseur personnalisé
        filterDatePicker.setConverter(new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
        // Pour éviter les entrées manuelles problématiques
        filterDatePicker.setEditable(false);
        // Setup filter controls
        setupFilterControls();

        // Check if the employee has already registered a status today.
        checkTodayStatus();

        // Load absence history into the table.
        loadAbsenceHistory();
    }

    private void setupButtons() {
        registerPresentButton.setOnAction(event -> registerPresence(StatutAbsence.PRESENT));
        registerAbsentButton.setOnAction(event -> registerPresence(StatutAbsence.ABSENT));
        registerLeaveButton.setOnAction(event -> registerPresence(StatutAbsence.EN_CONGE));
    }

    private void setupFilterControls() {
        // Configuration du DatePicker
        filterDatePicker.setValue(null);

        // Action pour le bouton d'application du filtre
        applyFilterButton.setOnAction(event -> {
            LocalDate selectedDate = filterDatePicker.getValue();
            if (selectedDate != null) {
                filterAbsencesByDate(selectedDate);
            }
        });

        // Action pour réinitialiser le filtre
        resetFilterButton.setOnAction(event -> {
            filterDatePicker.setValue(null);
            resetFilter();
        });
    }

    private void filterAbsencesByDate(LocalDate date) {
        if (allAbsenceRecords != null) {
            // Filtrer les absences par la date sélectionnée
            List<Absence> filteredRecords = allAbsenceRecords.stream()
                    .filter(absence -> {
                        // Convert java.sql.Date to LocalDate correctly
                        LocalDate absenceDate = new java.sql.Date(absence.getDate().getTime()).toLocalDate();
                        return absenceDate.equals(date);
                    })
                    .collect(Collectors.toList());

            // Mettre à jour la table avec les résultats filtrés
            absenceTable.setItems(FXCollections.observableArrayList(filteredRecords));

            // Mettre à jour le label de statut
            if (filteredRecords.isEmpty()) {
                statusLabel.setText("Aucun enregistrement trouvé pour la date sélectionnée");
                statusLabel.setTextFill(Color.ORANGE);
            } else {
                statusLabel.setText(filteredRecords.size() + " enregistrement(s) trouvé(s)");
                statusLabel.setTextFill(Color.BLACK);
            }
        }
    }

    private void resetFilter() {
        // Réinitialiser la table pour afficher toutes les absences
        if (allAbsenceRecords != null) {
            absenceTable.setItems(FXCollections.observableArrayList(allAbsenceRecords));
            statusLabel.setText("Filtre réinitialisé");
            statusLabel.setTextFill(Color.BLACK);
        }
    }

    private void registerPresence(StatutAbsence status) {
        try {
            Absence absence = new Absence();
            absence.setIdEmployee(EMPLOYEE_ID);
            absence.setDate(Date.valueOf(currentDate)); // Utilisation de java.sql.Date
            absence.setStatus(status);

            // Vérifier si un enregistrement existe déjà pour aujourd'hui
            Absence existingRecord = absenceService.getAbsenceForEmployeeByDate(EMPLOYEE_ID, Date.valueOf(currentDate));

            if (existingRecord != null) {
                // Mettre à jour l'enregistrement existant
                existingRecord.setStatus(status);
                absenceService.update(existingRecord);
                updateStatusLabel("Status updated: " + getDisplayText(status), status);
            } else {
                // Ajouter un nouvel enregistrement d'absence
                absenceService.add(absence);
                updateStatusLabel("Status registered: " + getDisplayText(status), status);
            }

            // ✅ Envoi du SMS après l'enregistrement
            String numeroDestinataire = "+21697129381"; // Numéro de l'administrateur RH
            String message = "📌 Présence enregistrée\n"
                    + "👤 Employé: " + EMPLOYEE_NAME + "\n"
                    + "📆 Date: " + currentDate + "\n"
                    + "📌 Statut: " + getDisplayText(status);

            // Appel au service SMS
            String messageSid = smsService.notifyHRAboutAttendance(EMPLOYEE_ID, EMPLOYEE_NAME, getDisplayText(status));

            if (messageSid != null) {
                System.out.println("📩 SMS envoyé avec succès à " + numeroDestinataire + ". Message SID: " + messageSid);
            } else {
                System.err.println("❌ Échec de l'envoi du SMS.");
            }

            // ✅ Rafraîchir l'affichage après enregistrement
            loadAbsenceHistory();

        } catch (SQLException e) {
            statusLabel.setText("Erreur: " + e.getMessage());
            statusLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private void checkTodayStatus() {
        Absence todayRecord = absenceService.getAbsenceForEmployeeByDate(EMPLOYEE_ID, Date.valueOf(currentDate));
        if (todayRecord != null) {
            updateStatusLabel("Today's status: " + getDisplayText(todayRecord.getStatus()), todayRecord.getStatus());
        } else {
            statusLabel.setText("You haven't registered your presence today");
            statusLabel.setTextFill(Color.ORANGE);
        }
    }

    private void updateStatusLabel(String message, StatutAbsence status) {
        statusLabel.setText(message);
        switch (status) {
            case PRESENT -> statusLabel.setTextFill(Color.GREEN);
            case ABSENT -> statusLabel.setTextFill(Color.RED);
            case EN_CONGE -> statusLabel.setTextFill(Color.BLUE);
            default -> statusLabel.setTextFill(Color.BLACK);
        }
    }

    private void loadAbsenceHistory() {
        try {
            // Charger toutes les absences et les conserver dans allAbsenceRecords
            allAbsenceRecords = absenceService.getAbsencesForEmployee(EMPLOYEE_ID);
            absenceTable.setItems(FXCollections.observableArrayList(allAbsenceRecords));
        } catch (SQLException e) {
            System.err.println("Error loading absence history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getDisplayText(StatutAbsence status) {
        if (status == null) return "";
        return switch (status) {
            case PRESENT -> "Présent";
            case ABSENT -> "Absent";
            case EN_CONGE -> "En Congé";
        };
    }
}