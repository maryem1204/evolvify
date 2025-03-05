package tn.esprit.Controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.AbsenceService;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.Utils.AbsenceUpdateSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

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
    private javafx.scene.control.TextField searchEmployeeField;
    @FXML
    private ComboBox<StatutAbsence> statusFilterComboBox;
    @FXML
    private Button generateAllPdfButton;
    @FXML
    private Button generateSelectedPdfButton;

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

        // Initialize PDF generation buttons
        generateAllPdfButton.setOnAction(event -> generateAbsenceReport(null));
        generateSelectedPdfButton.setOnAction(event -> {
            if (absenceTable.getSelectionModel().getSelectedItem() != null) {
                try {
                    Map<String, Object> selectedRow = absenceTable.getSelectionModel().getSelectedItem();
                    Utilisateur selectedUser = getUtilisateurFromRow(selectedRow);
                    generateAbsenceReport(selectedUser);
                } catch (SQLException e) {
                    showErrorAlert("Erreur", "Impossible de récupérer l'employé sélectionné: " + e.getMessage());
                }
            } else {
                showAlert("Aucune sélection", "Veuillez sélectionner un employé pour générer son rapport d'absences.");
            }
        });

        // Register this controller as an observer for absence updates
        AbsenceUpdateSystem.getInstance().addObserver(v -> {
            try {
                loadAbsences();
            } catch (SQLException e) {
                System.err.println("Error reloading absences after update: " + e.getMessage());
                e.printStackTrace();
            }
        });

        try {
            loadAbsences();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Optional: Add a cleanup method to be called when the controller is no longer needed
    public void cleanup() {
        // Use this method if you need to unregister the observer when the HR interface is closed
        AbsenceUpdateSystem.getInstance().removeObserver(v -> {
            try {
                loadAbsences();
            } catch (SQLException e) {
                System.err.println("Error reloading absences after update: " + e.getMessage());
            }
        });
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

    /**
     * Génère un rapport PDF des absences
     * @param selectedUser L'utilisateur spécifique (null pour tous les employés)
     */
    private void generateAbsenceReport(Utilisateur selectedUser) {
        try {
            // Demander à l'utilisateur où enregistrer le fichier
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Sélectionner le dossier d'enregistrement");
            File selectedDirectory = directoryChooser.showDialog(new Stage());

            if (selectedDirectory == null) {
                return; // L'utilisateur a annulé la sélection
            }

            // Créer le nom du fichier
            String fileName = "Rapport_Absences_";
            fileName += selectedUser == null ? "Tous_Employes" : selectedUser.getFirstname() + "_" + selectedUser.getLastname();
            fileName += "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";

            String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName;

            // Créer le document PDF
            Document document = new Document(PageSize.A4.rotate()); // Paysage pour plus de colonnes
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Ajouter le titre
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Historique des Absences", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Ajouter la période
            Font periodFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            String period = "Mois: " + monthComboBox.getValue() + " " + yearComboBox.getValue();
            Paragraph periodPara = new Paragraph(period, periodFont);
            periodPara.setAlignment(Element.ALIGN_CENTER);
            document.add(periodPara);

            // Ajouter le sous-titre
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            String subtitle = selectedUser == null ? "Tous les employés" : "Employé: " + selectedUser.getFirstname() + " " + selectedUser.getLastname();
            Paragraph subPara = new Paragraph(subtitle, subtitleFont);
            subPara.setAlignment(Element.ALIGN_CENTER);
            document.add(subPara);

            document.add(Chunk.NEWLINE);

            // Récupérer les données
            int year = Integer.parseInt(yearComboBox.getValue());
            int month = monthComboBox.getSelectionModel().getSelectedIndex() + 1;
            int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

            // Créer le tableau
            PdfPTable table = new PdfPTable(daysInMonth + 1); // +1 pour la colonne des noms
            table.setWidthPercentage(100);

            // Ajouter l'en-tête du tableau
            PdfPCell headerCell = new PdfPCell(new Phrase("Employé", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(headerCell);

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                if (date.getDayOfWeek().getValue() < 6) { // Exclure samedi et dimanche
                    headerCell = new PdfPCell(new Phrase(date.getDayOfWeek().toString().substring(0, 3) + " " + day,
                            new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD)));
                    headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    table.addCell(headerCell);
                } else {
                    // Ajout de cellules vides pour les week-ends
                    PdfPCell emptyCell = new PdfPCell(new Phrase("Weekend", new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC)));
                    emptyCell.setBackgroundColor(new BaseColor(240, 240, 240));
                    table.addCell(emptyCell);
                }
            }

            // Récupérer toutes les absences du mois
            AbsenceService absenceService = new AbsenceService();
            List<Absence> allAbsences = absenceService.getAbsencesForMonth(year, month);

            // Filtrer les utilisateurs à inclure
            List<Utilisateur> employeesToInclude;
            if (selectedUser == null) {
                // Tous les employés
                UtilisateurService uService = new UtilisateurService();
                employeesToInclude = uService.showAll();
            } else {
                // Juste l'employé sélectionné
                employeesToInclude = List.of(selectedUser);
            }

            // Organiser les absences par employé
            Map<Integer, Map<LocalDate, StatutAbsence>> absenceByEmployee = new HashMap<>();
            for (Absence absence : allAbsences) {
                if (selectedUser == null || absence.getIdEmployee() == selectedUser.getId_employe()) {
                    // Conversion java.sql.Date à LocalDate
                    LocalDate absenceDate = new java.sql.Date(absence.getDate().getTime()).toLocalDate();
                    absenceByEmployee
                            .computeIfAbsent(absence.getIdEmployee(), k -> new HashMap<>())
                            .put(absenceDate, absence.getStatus());
                }
            }

            // Remplir le tableau avec les données
            for (Utilisateur employee : employeesToInclude) {
                // Ajouter le nom de l'employé
                table.addCell(new Phrase(employee.getFirstname() + " " + employee.getLastname()));

                // Ajouter le statut pour chaque jour
                for (int day = 1; day <= daysInMonth; day++) {
                    LocalDate date = LocalDate.of(year, month, day);

                    String statusText;
                    BaseColor cellColor;

                    // Vérifier si c'est un jour de semaine
                    if (date.getDayOfWeek().getValue() < 6) {
                        // Récupérer le statut d'absence
                        StatutAbsence status = absenceByEmployee
                                .getOrDefault(employee.getId_employe(), new HashMap<>())
                                .getOrDefault(date, StatutAbsence.PRESENT);

                        // Définir le texte et la couleur
                        switch (status) {
                            case PRESENT:
                                statusText = "Présent";
                                cellColor = new BaseColor(230, 255, 230); // Vert pâle
                                break;
                            case ABSENT:
                                statusText = "Absent";
                                cellColor = new BaseColor(255, 230, 230); // Rouge pâle
                                break;
                            case EN_CONGE:
                                statusText = "En Congé";
                                cellColor = new BaseColor(230, 230, 255); // Bleu pâle
                                break;
                            default:
                                statusText = "";
                                cellColor = BaseColor.WHITE;
                        }
                    } else {
                        // Week-end
                        statusText = "";
                        cellColor = new BaseColor(240, 240, 240); // Gris pâle
                    }

                    PdfPCell cell = new PdfPCell(new Phrase(statusText, new Font(Font.FontFamily.HELVETICA, 8)));
                    cell.setBackgroundColor(cellColor);
                    table.addCell(cell);
                }
            }

            document.add(table);

            // Ajouter un résumé
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Légende:", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
            document.add(new Paragraph("- Présent: L'employé était présent", new Font(Font.FontFamily.HELVETICA, 10)));
            document.add(new Paragraph("- Absent: L'employé était absent", new Font(Font.FontFamily.HELVETICA, 10)));
            document.add(new Paragraph("- En Congé: L'employé était en congé autorisé", new Font(Font.FontFamily.HELVETICA, 10)));

            // Ajouter les statistiques
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Rapport généré le " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)));

            document.close();

            showSuccessAlert("PDF généré avec succès",
                    "Le rapport a été enregistré dans: " + filePath);

        } catch (Exception e) {
            showErrorAlert("Erreur lors de la génération du PDF", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche une boîte de dialogue d'information
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue de succès
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}