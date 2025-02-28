package tn.esprit.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ListCell;
import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.AbsenceService;
import tn.esprit.Services.UtilisateurService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class AbsenceController implements Initializable {
    @FXML
    private TableView<Map<String, Object>> absenceTable;
    @FXML
    private ComboBox<String> monthComboBox;
    @FXML
    private ComboBox<String> yearComboBox;
    @FXML
    private TableColumn<Map<String, Object>, String> employeeColumn;
    @FXML
    private TextField searchEmployeeField;
    @FXML
    private ComboBox<StatutAbsence> statusFilterComboBox;

    // Déplacer dataMap en tant que variable de classe pour être accessible dans applyFilters()
    private Map<Integer, Map<String, Object>> dataMap = new HashMap<>();

    private final AbsenceService absenceService = new AbsenceService();
    private final UtilisateurService uService = new UtilisateurService();
    private final String[] months = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        statusFilterComboBox.getItems().setAll(StatutAbsence.values());
        monthComboBox.getItems().addAll(months);
        for (int year = 2020; year <= 2030; year++) {
            yearComboBox.getItems().add(String.valueOf(year));
        }

        employeeColumn.setCellValueFactory(data -> new SimpleStringProperty((String) data.getValue().get("employe")));

        LocalDate now = LocalDate.now();
        monthComboBox.getSelectionModel().select(now.getMonthValue() - 1);
        yearComboBox.getSelectionModel().select(String.valueOf(now.getYear()));


        try {
            loadAbsences();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    private void loadAbsences() throws SQLException {
        absenceTable.getItems().clear();
        absenceTable.getColumns().clear();

        int year = Integer.parseInt(yearComboBox.getValue());
        int month = monthComboBox.getSelectionModel().getSelectedIndex() + 1;

        List<Utilisateur> allEmployees = uService.showAll();
        List<Absence> absences = absenceService.getAbsencesForMonth(year, month);

        dataMap.clear(); // S'assurer qu'on vide la map avant de charger

        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

        for (Utilisateur user : allEmployees) {
            Map<String, Object> row = new HashMap<>();
            row.put("employe", user.getFirstname() + " " + user.getLastname());

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (date.getDayOfWeek().getValue() < 6) { // Exclure samedi et dimanche
                    String dateKey = date.toString();
                    row.put(dateKey, StatutAbsence.PRESENT);
                }
            }
            dataMap.put(user.getId_employe(), row);
        }

        for (Absence absence : absences) {
            Map<String, Object> row = dataMap.get(absence.getIdEmployee());
            if (row != null) {
                String dateKey = absence.getDate().toString();
                row.put(dateKey, absence.getStatus());
            }
        }

        absenceTable.getColumns().add(employeeColumn);

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            if (date.getDayOfWeek().getValue() < 6) { // Exclure samedi et dimanche
                TableColumn<Map<String, Object>, String> column = new TableColumn<>(date.getDayOfWeek().toString().substring(0, 3) + " " + day);
                column.setMinWidth(120);
                final String dateKey = date.toString();

                column.setCellValueFactory(data -> {
                    Object value = data.getValue().get(dateKey);
                    return new SimpleStringProperty(value != null ? getDisplayText((StatutAbsence) value) : "");
                });

                column.setCellFactory(col -> new TableCell<>() {
                    private final ComboBox<StatutAbsence> comboBox = new ComboBox<>();

                    {
                        comboBox.getItems().setAll(StatutAbsence.values());
                        comboBox.setCellFactory(listView -> new ListCell<>() {
                            @Override
                            protected void updateItem(StatutAbsence item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(empty || item == null ? null : getDisplayText(item));
                                setGraphic(empty || item == null ? null : getIcon(item));
                            }
                        });

                        comboBox.setButtonCell(new ListCell<>() {
                            @Override
                            protected void updateItem(StatutAbsence item, boolean empty) {
                                super.updateItem(item, empty);
                                setText(empty || item == null ? null : getDisplayText(item));
                                setGraphic(empty || item == null ? null : getIcon(item));
                            }
                        });

                        comboBox.setOnAction(event -> {
                            if (getTableRow() != null) {
                                Map<String, Object> rowData = getTableRow().getItem();
                                if (rowData != null) {
                                    rowData.put(dateKey, comboBox.getValue());
                                    Utilisateur user = null;
                                    try {
                                        user = getUtilisateurFromRow(rowData);
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                    if (user != null) {
                                        try {
                                            absenceService.update(new Absence(0, comboBox.getValue(), java.sql.Date.valueOf(date), user.getId_employe()));
                                        } catch (SQLException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            comboBox.setValue(getStatutFromText(item));
                            setGraphic(comboBox);
                        }
                    }
                });

                absenceTable.getColumns().add(column);
            }
        }

        applyFilters();
    }

    private String getDisplayText(StatutAbsence status) {
        if (status == null) return "";
        return switch (status) {
            case PRESENT -> "Présent";
            case ABSENT -> "Absent";
            case EN_CONGE -> "En Congé";
        };
    }
    @FXML
    private void applyFilters() {
        String employeeFilter = searchEmployeeField.getText().toLowerCase();
        StatutAbsence selectedStatus = statusFilterComboBox.getValue();

        absenceTable.getItems().setAll(FXCollections.observableArrayList(
                dataMap.values().stream()
                        .filter(row -> row.get("employe") != null)
                        .filter(row -> employeeFilter.isEmpty() || ((String) row.get("employe")).toLowerCase().contains(employeeFilter))
                        .filter(row -> selectedStatus == null || row.values().stream().anyMatch(value -> value.equals(selectedStatus)))
                        .toList()
        ));
    }

    private StatutAbsence getStatutFromText(String text) {
        return switch (text) {
            case "Présent" -> StatutAbsence.PRESENT;
            case "Absent" -> StatutAbsence.ABSENT;
            case "En Congé" -> StatutAbsence.EN_CONGE;
            default -> null;
        };
    }

    private Utilisateur getUtilisateurFromRow(Map<String, Object> rowData) throws SQLException {
        String fullName = (String) rowData.get("employe");
        return uService.showAll().stream()
                .filter(user -> (user.getFirstname() + " " + user.getLastname()).equals(fullName))
                .findFirst()
                .orElse(null);
    }

    private Node getIcon(StatutAbsence status) {
        Label iconLabel = new Label();
        switch (status) {
            case PRESENT -> {
                iconLabel.setText("✔");
                iconLabel.setStyle("-fx-text-fill: green;");
            }
            case ABSENT -> {
                iconLabel.setText("❌");
                iconLabel.setStyle("-fx-text-fill: red;");
            }
            case EN_CONGE -> {
                iconLabel.setText("🌴");
            }
        }
        return iconLabel;
    }
}
