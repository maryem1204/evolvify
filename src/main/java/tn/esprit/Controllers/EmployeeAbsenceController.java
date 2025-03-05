package tn.esprit.Controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.AbsenceService;
import tn.esprit.Services.SmsService;
import tn.esprit.Utils.AbsenceUpdateSystem;
import tn.esprit.Utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

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
    @FXML
    private Button generatePdfButton; // Nouveau bouton pour générer PDF

    private final AbsenceService absenceService = new AbsenceService();
    private final SmsService smsService = new SmsService();

    // Récupérer l'utilisateur connecté via SessionManager
    private Utilisateur currentUser;
    private LocalDate currentDate = LocalDate.now();

    // Liste pour conserver toutes les absences sans filtre
    private List<Absence> allAbsenceRecords;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Récupérer l'utilisateur connecté depuis SessionManager
        currentUser = SessionManager.getUtilisateurConnecte();

        // Vérifier si l'utilisateur est connecté
        if (currentUser == null) {
            statusLabel.setText("Erreur: Aucun utilisateur connecté");
            statusLabel.setTextFill(Color.RED);
            disableButtons(true);
            return;
        }

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

    private void disableButtons(boolean disable) {
        registerPresentButton.setDisable(disable);
        registerAbsentButton.setDisable(disable);
        registerLeaveButton.setDisable(disable);
        applyFilterButton.setDisable(disable);
        resetFilterButton.setDisable(disable);
        if (generatePdfButton != null) {
            generatePdfButton.setDisable(disable);
        }
    }

    private void setupButtons() {
        registerPresentButton.setOnAction(event -> registerPresence(StatutAbsence.PRESENT));
        registerAbsentButton.setOnAction(event -> registerPresence(StatutAbsence.ABSENT));
        registerLeaveButton.setOnAction(event -> registerPresence(StatutAbsence.EN_CONGE));

        // Configurer le bouton de génération PDF s'il existe dans le fxml
        if (generatePdfButton != null) {
            generatePdfButton.setOnAction(event -> generateAbsencePdf());
        } else {
            System.err.println("Le bouton generatePdfButton n'est pas défini dans le FXML");
        }
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
            // Verify that the current user exists and has a valid ID
            if (currentUser == null || currentUser.getId_employe() <= 0) {
                statusLabel.setText("Erreur: Utilisateur invalide ou non connecté");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            // Log the employee ID for debugging
            int employeeId = currentUser.getId_employe();
            System.out.println("Using employee ID: " + employeeId);
            String employeeName = currentUser.getFirstname() + " " + currentUser.getLastname();

            Absence absence = new Absence();
            absence.setIdEmployee(employeeId);
            absence.setDate(Date.valueOf(currentDate));
            absence.setStatus(status);

            // Vérifier si un enregistrement existe déjà pour aujourd'hui
            Absence existingRecord = absenceService.getAbsenceForEmployeeByDate(employeeId, Date.valueOf(currentDate));

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

            // Notify the HR interface about the update
            AbsenceUpdateSystem.getInstance().notifyAbsenceUpdate();

            // ✅ Envoi du SMS après l'enregistrement
            String message = "📌 Présence enregistrée\n"
                    + "👤 Employé: " + employeeName + "\n"
                    + "📆 Date: " + currentDate + "\n"
                    + "📌 Statut: " + getDisplayText(status);

            // Appel au service SMS
            String messageSid = smsService.notifyHRAboutAttendance(employeeId, employeeName, getDisplayText(status));

            if (messageSid != null) {
                System.out.println("📩 SMS envoyé avec succès. Message SID: " + messageSid);
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
        if (currentUser == null) return;

        int employeeId = currentUser.getId_employe();
        Absence todayRecord = absenceService.getAbsenceForEmployeeByDate(employeeId, Date.valueOf(currentDate));
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
            if (currentUser == null) return;

            int employeeId = currentUser.getId_employe();
            // Charger toutes les absences et les conserver dans allAbsenceRecords
            allAbsenceRecords = absenceService.getAbsencesForEmployee(employeeId);
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

    // Méthode pour générer un PDF avec l'historique des absences
    private void generateAbsencePdf() {
        if (currentUser == null || allAbsenceRecords == null || allAbsenceRecords.isEmpty()) {
            showAlert("Aucune donnée à exporter", "Il n'y a pas d'historique d'absences à exporter.", AlertType.WARNING);
            return;
        }

        // Demander à l'utilisateur de choisir l'emplacement de sauvegarde
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choisir un dossier pour sauvegarder le PDF");
        File selectedDirectory = directoryChooser.showDialog(null);

        if (selectedDirectory == null) {
            return; // L'utilisateur a annulé la sélection
        }

        String fileName = "Historique_Absences_" + currentUser.getFirstname() + "_" +
                currentUser.getLastname() + "_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".pdf";

        String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName;

        try {
            // Création du document PDF
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Ajout des métadonnées
            document.addTitle("Historique des absences");
            document.addSubject("Rapport d'absences pour " + currentUser.getFirstname() + " " + currentUser.getLastname());
            document.addKeywords("absence, présence, historique, rapport");
            document.addCreator("Application de gestion des absences");

            // Ajout du logo de l'entreprise (à remplacer avec votre propre logo)
            try {
                // Image logo = Image.getInstance("path/to/your/logo.png");
                // logo.setAlignment(Element.ALIGN_CENTER);
                // logo.scaleToFit(150, 150);
                // document.add(logo);
                // document.add(new Paragraph(" "));  // Espace
            } catch (Exception e) {
                System.err.println("Logo de l'entreprise non trouvé: " + e.getMessage());
            }

            // Ajout du titre
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("Historique des Absences", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));  // Espace

            // Ajout des informations de l'employé
            Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
            Paragraph employeeInfo = new Paragraph("Informations de l'employé", subTitleFont);
            employeeInfo.setAlignment(Element.ALIGN_LEFT);
            document.add(employeeInfo);

            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
            document.add(new Paragraph("Nom: " + currentUser.getLastname(), normalFont));
            document.add(new Paragraph("Prénom: " + currentUser.getFirstname(), normalFont));
            document.add(new Paragraph("ID Employé: " + currentUser.getId_employe(), normalFont));
            document.add(new Paragraph("Date du rapport: " + currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
            document.add(new Paragraph(" "));  // Espace

            // Création du tableau
            PdfPTable table = new PdfPTable(2);  // 2 colonnes
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Définir les largeurs relatives des colonnes
            float[] columnWidths = {1.5f, 1f};
            table.setWidths(columnWidths);

            // En-têtes du tableau
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            PdfPCell dateHeader = new PdfPCell(new Phrase("Date", headerFont));
            dateHeader.setBackgroundColor(BaseColor.DARK_GRAY);
            dateHeader.setPadding(5);
            dateHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(dateHeader);

            PdfPCell statusHeader = new PdfPCell(new Phrase("Statut", headerFont));
            statusHeader.setBackgroundColor(BaseColor.DARK_GRAY);
            statusHeader.setPadding(5);
            statusHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(statusHeader);

            // Ajout des données
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            boolean alternate = false;
            BaseColor altColor = new BaseColor(240, 240, 240);

            for (Absence absence : allAbsenceRecords) {
                // Date
                PdfPCell dateCell = new PdfPCell(new Phrase(sdf.format(absence.getDate()), cellFont));
                dateCell.setPadding(5);
                if (alternate) dateCell.setBackgroundColor(altColor);
                table.addCell(dateCell);

                // Statut
                String statusText = getDisplayText(absence.getStatus());
                PdfPCell statusCell = new PdfPCell(new Phrase(statusText, cellFont));
                statusCell.setPadding(5);
                if (alternate) statusCell.setBackgroundColor(altColor);

                // Colorer le statut
                if (absence.getStatus() == StatutAbsence.PRESENT) {
                    statusCell.setBackgroundColor(new BaseColor(220, 255, 220));  // Vert clair
                } else if (absence.getStatus() == StatutAbsence.ABSENT) {
                    statusCell.setBackgroundColor(new BaseColor(255, 220, 220));  // Rouge clair
                } else if (absence.getStatus() == StatutAbsence.EN_CONGE) {
                    statusCell.setBackgroundColor(new BaseColor(220, 220, 255));  // Bleu clair
                }

                table.addCell(statusCell);

                alternate = !alternate;
            }

            document.add(table);

            // Ajout des statistiques
            document.add(new Paragraph(" "));  // Espace
            Paragraph statsTitle = new Paragraph("Statistiques", subTitleFont);
            document.add(statsTitle);

            // Calculer les statistiques
            long totalDays = allAbsenceRecords.size();
            long presentDays = allAbsenceRecords.stream()
                    .filter(a -> a.getStatus() == StatutAbsence.PRESENT)
                    .count();
            long absentDays = allAbsenceRecords.stream()
                    .filter(a -> a.getStatus() == StatutAbsence.ABSENT)
                    .count();
            long leaveDays = allAbsenceRecords.stream()
                    .filter(a -> a.getStatus() == StatutAbsence.EN_CONGE)
                    .count();

            double presentPercentage = (double) presentDays / totalDays * 100;
            double absentPercentage = (double) absentDays / totalDays * 100;
            double leavePercentage = (double) leaveDays / totalDays * 100;

            document.add(new Paragraph("Nombre total de jours enregistrés: " + totalDays, normalFont));
            document.add(new Paragraph("Jours de présence: " + presentDays + " (" + String.format("%.1f", presentPercentage) + "%)", normalFont));
            document.add(new Paragraph("Jours d'absence: " + absentDays + " (" + String.format("%.1f", absentPercentage) + "%)", normalFont));
            document.add(new Paragraph("Jours de congé: " + leaveDays + " (" + String.format("%.1f", leavePercentage) + "%)", normalFont));

            // Ajout du pied de page
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            Font footerFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Ce document a été généré automatiquement par le système de gestion des absences. " +
                    "Pour toute question, veuillez contacter le service des ressources humaines.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            writer.close();

            showAlert("PDF Généré avec Succès", "Le fichier a été sauvegardé sous:\n" + filePath, AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de la génération du PDF:\n" + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}