package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.AbsenceService;
import tn.esprit.Services.UtilisateurService;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Callback;

public class CalendarController {
    @FXML
    private ComboBox<Integer> yearComboBox;
    @FXML
    private ComboBox<String> monthComboBox;
    @FXML
    private Button applyButton;
    @FXML
    private TableView<Utilisateur> attendanceTable;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private TableColumn<Utilisateur, GridPane> colCalendar;


    private final AbsenceService absenceService = new AbsenceService();
    private final int idEmploye = 1; // Remplace par l'ID dynamique de l'employé
    private final Map<Button, LocalDate> buttonDateMap = new HashMap<>();

    @FXML
    public void initialize() {
        try {

            // Remplir les sélecteurs avec les années et mois disponibles
            fillMonthComboBox();
            fillYearComboBox();

            // Récupérer les utilisateurs
            List<Utilisateur> utilisateurs = new UtilisateurService().showAll();

            // Mettre les utilisateurs dans le TableView
            attendanceTable.setItems(FXCollections.observableArrayList(utilisateurs));

            attendanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            // Définir la largeur de la colonne Calendar pour qu'elle prenne tout l'espace disponible
            colCalendar.prefWidthProperty().bind(attendanceTable.widthProperty().subtract(100)); // Ajuster le 100 selon l'espace de la table
            // Configuration de la cellule pour la colonne Calendar sans lier à une propriété
            colCalendar.setCellFactory(new Callback<TableColumn<Utilisateur, GridPane>, TableCell<Utilisateur, GridPane>>() {
                @Override
                public TableCell<Utilisateur, GridPane> call(TableColumn<Utilisateur, GridPane> param) {
                    return new TableCell<Utilisateur, GridPane>() {
                        @Override
                        protected void updateItem(GridPane item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                // Utilisation d'un GridPane dynamique
                                setGraphic(createCalendarGrid());
                            }
                        }

                        private GridPane createCalendarGrid() {
                            GridPane grid = new GridPane();
                            LocalDate today = LocalDate.now();
                            int year = today.getYear();
                            int month = today.getMonthValue();
                            int daysInMonth = today.lengthOfMonth();
                            grid.setHgap(10); // Espacement horizontal
                            grid.setVgap(10); // Espacement vertical
                            int row = 0;
                            int col = 0;

                            for (int day = 1; day <= daysInMonth; day++) {
                                LocalDate date = LocalDate.of(year, month, day);
                                Button dayButton = new Button(String.valueOf(day));
                                dayButton.setStyle("-fx-background-color: lightgreen; -fx-font-size: 14px; -fx-padding: 5px;");


                                // Ajouter un statut par défaut ou depuis la base de données
                                StatutAbsence statut = getAbsenceStatus(date);
                                updateButtonStyle(dayButton, statut);

                                // Ajouter le menu contextuel pour changer le statut
                                ContextMenu contextMenu = createContextMenu(dayButton, date);
                                dayButton.setOnContextMenuRequested(e -> contextMenu.show(dayButton, e.getScreenX(), e.getScreenY()));

                                grid.add(dayButton, col, row);

                                col++;
                                if (col == 7) { // Passage à une nouvelle ligne après 7 jours
                                    col = 0;
                                    row++;
                                }
                            }
                            return grid;
                        }
                    };
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Remplir le ComboBox des mois
    private void fillMonthComboBox() {
        monthComboBox.setItems(FXCollections.observableArrayList(
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"));
        monthComboBox.getSelectionModel().select(LocalDate.now().getMonthValue() - 1); // Mois actuel par défaut
    }

    // Remplir le ComboBox des années
    private void fillYearComboBox() {
        int currentYear = LocalDate.now().getYear();
        yearComboBox.setItems(FXCollections.observableArrayList(currentYear, currentYear - 1, currentYear + 1));
        yearComboBox.getSelectionModel().select(Integer.valueOf(String.valueOf(currentYear))); // Année actuelle par défaut
    }

    // Gérer le clic sur le bouton "Appliquer"
    @FXML
    private void onApplyButtonClicked() {
        String selectedMonth = monthComboBox.getSelectionModel().getSelectedItem();
        Integer selectedYear = yearComboBox.getSelectionModel().getSelectedItem();

        System.out.println("Mois sélectionné: " + selectedMonth);
        System.out.println("Année sélectionnée: " + selectedYear);

        // Mettre à jour le calendrier en fonction de la sélection
        // Vous pouvez appeler une méthode pour générer ou mettre à jour le calendrier ici
        // Exemple : updateCalendar(selectedYear, selectedMonth);
    }
  /*  private void generateCalendar() {
        calendarGrid.getChildren().clear();
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        int daysInMonth = today.lengthOfMonth();

        int row = 0;
        int col = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            Button dayButton = new Button(String.valueOf(day));

            // Appliquer un style par défaut
            dayButton.setStyle("-fx-background-color: lightgreen; -fx-font-size: 14px;");

            // Associer le bouton à une date spécifique
            buttonDateMap.put(dayButton, date);

            // Charger le statut actuel depuis la base de données
            StatutAbsence statut = getAbsenceStatus(date);
            updateButtonStyle(dayButton, statut);

            // Ajouter le menu contextuel pour changer le statut
            ContextMenu contextMenu = createContextMenu(dayButton, date);
            dayButton.setOnContextMenuRequested(e -> contextMenu.show(dayButton, e.getScreenX(), e.getScreenY()));

            calendarGrid.add(dayButton, col, row);

            col++;
            if (col == 7) { // Passage à une nouvelle ligne après 7 jours
                col = 0;
                row++;
            }
        }
    }*/

    private ContextMenu createContextMenu(Button dayButton, LocalDate date) {
        ContextMenu contextMenu = new ContextMenu();

        for (StatutAbsence statut : StatutAbsence.values()) {
            MenuItem menuItem = new MenuItem(statut.toString());
            menuItem.setOnAction(event -> {
                updateAbsenceStatus(date, statut);
                updateButtonStyle(dayButton, statut);
            });
            contextMenu.getItems().add(menuItem);
        }

        return contextMenu;
    }

    private StatutAbsence getAbsenceStatus(LocalDate date) {
        try {
            // Vérifier si une absence existe pour cette date et l'employé
            for (Absence absence : absenceService.showAll()) {
                if (absence.getDate().equals(date) && absence.getIdEmployee() == idEmploye) {
                    return absence.getStatus();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return StatutAbsence.PRESENT; // Statut par défaut si aucune absence n'est trouvée
    }

    private void updateAbsenceStatus(LocalDate date, StatutAbsence statut) {
        try {
            Absence absence = new Absence();
            absence.setDate(java.sql.Date.valueOf(date));
            absence.setStatus(statut);
            absence.setIdEmployee(idEmploye);

            absenceService.update(absence);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateButtonStyle(Button button, StatutAbsence statut) {
        switch (statut) {
            case PRESENT -> button.setStyle("-fx-background-color: lightgreen;");
            case ABSENT -> button.setStyle("-fx-background-color: red;");
            case EN_CONGE -> button.setStyle("-fx-background-color: yellow;");
        }
    }

}
