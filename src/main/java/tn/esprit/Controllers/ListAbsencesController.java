package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Services.AbsenceService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class ListAbsencesController {

    @FXML
    private GridPane calendarGrid;

    private final AbsenceService absenceService = new AbsenceService();

    @FXML
    public void initialize() {
        generateCalendar(LocalDate.now().getYear(), LocalDate.now().getMonthValue());
    }

    private void generateCalendar(int year, int month) {
        calendarGrid.getChildren().clear();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();

        int column = firstDayOfMonth.getDayOfWeek().getValue() % 7;
        int row = 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            Button dayButton = new Button(String.valueOf(day));
            dayButton.setOnAction(e -> handleDayClick(date));
            calendarGrid.add(dayButton, column, row);
            column++;
            if (column == 7) {
                column = 0;
                row++;
            }
        }
    }

    private void handleDayClick(LocalDate date) {
        try {
            List<Absence> absences = absenceService.getAbsencesByDate(java.sql.Date.valueOf(date));
            showStatusSelectionDialog(date, absences);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showStatusSelectionDialog(LocalDate date, List<Absence> absences) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Modifier les statuts");
        GridPane grid = new GridPane();

        int row = 0;
        for (Absence absence : absences) {
            Label nameLabel = new Label(absence.getEmployeeName());
            ComboBox<StatutAbsence> statusComboBox = new ComboBox<>();
            statusComboBox.getItems().setAll(StatutAbsence.values());
            statusComboBox.setValue(absence.getStatus());

            statusComboBox.setOnAction(e -> {
                absence.setStatus(statusComboBox.getValue());
                try {
                    absenceService.update(absence);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            grid.addRow(row++, nameLabel, statusComboBox);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }
}
