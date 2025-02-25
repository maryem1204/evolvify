package tn.esprit.Controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import tn.esprit.Entities.Conge;
import tn.esprit.Entities.Tt;
import tn.esprit.Entities.Statut;
import tn.esprit.Entities.Reason;
import tn.esprit.Services.CongeService;
import tn.esprit.Services.TtService;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class CongeEmployeController implements Initializable {

    // TableView and columns for Conges
    @FXML
    private TableView<Conge> tableConges;
    @FXML
    private TableColumn<Conge, Date> colCongeDebut;
    @FXML
    private TableColumn<Conge, Date> colCongeFin;
    @FXML
    private TableColumn<Conge, String> colCongeRaison;
    @FXML
    private TableColumn<Conge, Integer> colCongeJours;
    @FXML
    private TableColumn<Conge, String> colCongeStatut;

    // DatePickers, TextField, ComboBox and Button for Conges
    @FXML
    private DatePicker dateDebutConge, dateFinConge;
    @FXML
    private TextField descConge, joursConge;
    @FXML
    private ComboBox<Reason> raisonConge;
    @FXML
    private Button submitConge, modifierConge, supprimerConge;

    // TableView and columns for TT
    @FXML
    private TableView<Tt> tableTT;
    @FXML
    private TableColumn<Tt, Date> colTTDebut;
    @FXML
    private TableColumn<Tt, Date> colTTFin;
    @FXML
    private TableColumn<Tt, Integer> colTTJours;
    @FXML
    private TableColumn<Tt, String> colTTStatut;

    // DatePickers, TextField and Button for TT
    @FXML
    private DatePicker dateDebutTT, dateFinTT;
    @FXML
    private TextField joursTT;
    @FXML
    private Button submitTT, modifierTT, supprimerTT;

    private final CongeService congeService = new CongeService();
    private final TtService ttService = new TtService();
    private final int currentUserId = 1; // Remplacer par l'ID de l'utilisateur connecté

    // Variables pour stocker les éléments sélectionnés
    private Conge selectedConge;
    private Tt selectedTt;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize ComboBox with Reason enum values
        raisonConge.setItems(FXCollections.observableArrayList(Reason.values()));
// Configurer les boutons de modification pour qu'ils soient invisibles au départ
        modifierConge.setVisible(false);
        modifierTT.setVisible(false);
        // Custom cell factory to display reason names in the ComboBox
        raisonConge.setCellFactory(param -> new ListCell<Reason>() {
            @Override
            protected void updateItem(Reason item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Set ComboBox button cell to display selected reason
        raisonConge.setButtonCell(new ListCell<Reason>() {
            @Override
            protected void updateItem(Reason item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Configure Conge TableView columns
        colCongeDebut.setCellValueFactory(new PropertyValueFactory<>("leave_start"));
        colCongeFin.setCellValueFactory(new PropertyValueFactory<>("leave_end"));
        colCongeJours.setCellValueFactory(new PropertyValueFactory<>("number_of_days"));
        colCongeRaison.setCellValueFactory(cellData -> {
            Reason reason = cellData.getValue().getReason();
            return new SimpleStringProperty(reason != null ? reason.toString() : "");
        });
        colCongeStatut.setCellValueFactory(cellData -> {
            Statut statut = cellData.getValue().getStatus();
            return new SimpleStringProperty(statut != null ? statut.toString() : "");
        });

        // Configure TT TableView columns
        colTTDebut.setCellValueFactory(new PropertyValueFactory<>("leave_start"));
        colTTFin.setCellValueFactory(new PropertyValueFactory<>("leave_end"));
        colTTJours.setCellValueFactory(new PropertyValueFactory<>("number_of_days"));
        colTTStatut.setCellValueFactory(cellData -> {
            Statut statut = cellData.getValue().getStatus();
            return new SimpleStringProperty(statut != null ? statut.toString() : "");
        });

        // Set button actions
        submitConge.setOnAction(event -> addConge());
        submitTT.setOnAction(event -> addTt());
        modifierConge.setOnAction(event -> modifierConge());
        supprimerConge.setOnAction(event -> supprimerConge());
        modifierTT.setOnAction(event -> modifierTt());
        supprimerTT.setOnAction(event -> supprimerTt());

        // Désactiver les boutons de modification et suppression par défaut
        modifierConge.setDisable(true);
        supprimerConge.setDisable(true);
        modifierTT.setDisable(true);
        supprimerTT.setDisable(true);

        // In the tableConges selection listener (around line 158)
        tableConges.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedConge = newSelection;
                boolean isEnCours = newSelection.getStatus() == Statut.EN_COURS;

                // Rendre visible et activer/désactiver les boutons
                submitConge.setVisible(!isEnCours);
                modifierConge.setVisible(isEnCours);
                modifierConge.setDisable(!isEnCours); // S'assurer que le bouton est activé
                supprimerConge.setDisable(!isEnCours);

                if (isEnCours) {
                    // Remplir les champs avec les données du congé sélectionné
                    LocalDate startDate = new java.sql.Date(newSelection.getLeave_start().getTime()).toLocalDate();
                    LocalDate endDate = new java.sql.Date(newSelection.getLeave_end().getTime()).toLocalDate();

                    dateDebutConge.setValue(startDate);
                    dateFinConge.setValue(endDate);
                    raisonConge.setValue(newSelection.getReason());
                    descConge.setText(newSelection.getDescription());
                    joursConge.setText(String.valueOf(newSelection.getNumber_of_days()));
                }
            } else {
                selectedConge = null;
                // Restaurer l'état initial des boutons
                submitConge.setVisible(true);
                modifierConge.setVisible(false);
                supprimerConge.setDisable(true);
            }
        });

        // In the tableTT selection listener (around line 185)
        tableTT.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTt = newSelection;
                boolean isEnCours = newSelection.getStatus() == Statut.EN_COURS;

                // Rendre visible et activer/désactiver les boutons
                submitTT.setVisible(!isEnCours);
                modifierTT.setVisible(isEnCours);
                modifierTT.setDisable(!isEnCours); // S'assurer que le bouton est activé
                supprimerTT.setDisable(!isEnCours);


                if (isEnCours) {
                    // Remplir les champs avec les données du TT sélectionné
                    LocalDate startDate = new java.sql.Date(newSelection.getLeave_start().getTime()).toLocalDate();
                    LocalDate endDate = new java.sql.Date(newSelection.getLeave_end().getTime()).toLocalDate();

                    dateDebutTT.setValue(startDate);
                    dateFinTT.setValue(endDate);
                    joursTT.setText(String.valueOf(newSelection.getNumber_of_days()));
                }
            } else {
                selectedTt = null;
                // Restaurer l'état initial des boutons
                submitTT.setVisible(true);
                modifierTT.setVisible(false);
                modifierTT.setDisable(true); // Désactiver explicitement
                supprimerTT.setDisable(true);
            }
        });

        // Load initial data
        loadConges();
        loadTts();

        // Calculate days automatically when dates are selected
        dateDebutConge.valueProperty().addListener((obs, oldVal, newVal) -> calculateDays());
        dateFinConge.valueProperty().addListener((obs, oldVal, newVal) -> calculateDays());

        dateDebutTT.valueProperty().addListener((obs, oldVal, newVal) -> calculateTtDays());
        dateFinTT.valueProperty().addListener((obs, oldVal, newVal) -> calculateTtDays());
    }

    private void calculateDays() {
        if (dateDebutConge.getValue() != null && dateFinConge.getValue() != null) {
            // Calculate days between start and end dates
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    dateDebutConge.getValue(),
                    dateFinConge.getValue()) + 1;

            if (days > 0) {
                joursConge.setText(String.valueOf(days));
            } else {
                joursConge.clear();
                showAlert("Erreur", "La date de fin doit être après la date de début");
            }
        }
    }

    private void calculateTtDays() {
        if (dateDebutTT.getValue() != null && dateFinTT.getValue() != null) {
            // Calculate days between start and end dates
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    dateDebutTT.getValue(),
                    dateFinTT.getValue()) + 1;

            if (days > 0) {
                joursTT.setText(String.valueOf(days));
            } else {
                joursTT.clear();
                showAlert("Erreur", "La date de fin doit être après la date de début");
            }
        }
    }

    @FXML
    private void addConge() {
        try {
            if (dateDebutConge.getValue() == null || dateFinConge.getValue() == null ||
                    raisonConge.getValue() == null || joursConge.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs requis");
                return;
            }

            Conge conge = new Conge(
                    0, // ID will be set by the database
                    Date.from(dateDebutConge.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Date.from(dateFinConge.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Integer.parseInt(joursConge.getText()),
                    Statut.EN_COURS, // Statut par défaut
                    currentUserId,
                    raisonConge.getValue(),
                    descConge.getText()
            );

            congeService.add(conge);
            clearCongeFields();
            loadConges();
            showAlert("Succès", "Demande de congé ajoutée avec succès");
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Nombre de jours invalide");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout du congé: " + e.getMessage());
        }
    }

    @FXML
    private void modifierConge() {
        try {
            if (selectedConge == null) {
                showAlert("Erreur", "Aucune demande de congé sélectionnée");
                return;
            }

            if (selectedConge.getStatus() != Statut.EN_COURS) {
                showAlert("Erreur", "Seules les demandes en cours peuvent être modifiées");
                return;
            }

            if (dateDebutConge.getValue() == null || dateFinConge.getValue() == null ||
                    raisonConge.getValue() == null || joursConge.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs requis");
                return;
            }

            // Convertir les nouvelles valeurs depuis les champs de l'interface
            Date nouvelleDebut = Date.from(dateDebutConge.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date nouvelleFin = Date.from(dateFinConge.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            int nouveauxJours = Integer.parseInt(joursConge.getText());
            Reason nouvelleRaison = raisonConge.getValue();
            String nouvelleDesc = descConge.getText() != null ? descConge.getText() : "";
            String ancienneDesc = selectedConge.getDescription() != null ? selectedConge.getDescription() : "";

            // Vérifier si des modifications ont été apportées
            boolean modifie = false;

            // Comparaison des dates en tenant compte des différences de millisecondes
            if (!isSameDay(nouvelleDebut, selectedConge.getLeave_start())) modifie = true;
            else if (!isSameDay(nouvelleFin, selectedConge.getLeave_end())) modifie = true;
            else if (nouveauxJours != selectedConge.getNumber_of_days()) modifie = true;
            else if (nouvelleRaison != selectedConge.getReason()) modifie = true;
            else if (!nouvelleDesc.equals(ancienneDesc)) modifie = true;

            if (!modifie) {
                showAlert("Information", "Aucune modification n'a été détectée");
                return;
            }

            // Créer l'objet Conge avec les nouvelles valeurs
            Conge conge = new Conge(
                    selectedConge.getId_Conge(),
                    nouvelleDebut,
                    nouvelleFin,
                    nouveauxJours,
                    Statut.EN_COURS,
                    currentUserId,
                    nouvelleRaison,
                    nouvelleDesc
            );

            congeService.update(conge);
            clearCongeFields();
            loadConges();
            showAlert("Succès", "Demande de congé modifiée avec succès");
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Nombre de jours invalide");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification du congé: " + e.getMessage());
        }
    }

    // Méthode auxiliaire pour comparer les dates sans tenir compte de l'heure
    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return date1 == date2;
        }

        // Convertir en LocalDate pour comparer seulement les dates
        LocalDate localDate1 = new java.sql.Date(date1.getTime()).toLocalDate();
        LocalDate localDate2 = new java.sql.Date(date2.getTime()).toLocalDate();

        return localDate1.equals(localDate2);
    }

    @FXML
    private void supprimerConge() {
        try {
            if (selectedConge == null) {
                showAlert("Erreur", "Aucune demande de congé sélectionnée");
                return;
            }

            if (selectedConge.getStatus() != Statut.EN_COURS) {
                showAlert("Erreur", "Seules les demandes en cours peuvent être supprimées");
                return;
            }

            // Confirmation avant suppression
            boolean confirmed = showConfirmation("Confirmation", "Voulez-vous vraiment supprimer cette demande de congé ?");
            if (!confirmed) {
                return;
            }

            congeService.delete(selectedConge);
            clearCongeFields();
            loadConges();
            showAlert("Succès", "Demande de congé supprimée avec succès");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression du congé: " + e.getMessage());
        }
    }

    @FXML
    private void addTt() {
        try {
            if (dateDebutTT.getValue() == null || dateFinTT.getValue() == null ||
                    joursTT.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs requis");
                return;
            }

            Tt tt = new Tt(
                    0, // ID will be set by the database
                    Date.from(dateDebutTT.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Date.from(dateFinTT.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Integer.parseInt(joursTT.getText()),
                    Statut.EN_COURS, // Statut par défaut
                    currentUserId
            );

            ttService.add(tt);
            clearTtFields();
            loadTts();
            showAlert("Succès", "Demande de télétravail ajoutée avec succès");
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Nombre de jours invalide");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout du télétravail: " + e.getMessage());
        }
    }

    @FXML
    private void modifierTt() {
        try {
            if (selectedTt == null) {
                showAlert("Erreur", "Aucune demande de télétravail sélectionnée");
                return;
            }

            if (selectedTt.getStatus() != Statut.EN_COURS) {
                showAlert("Erreur", "Seules les demandes en cours peuvent être modifiées");
                return;
            }

            if (dateDebutTT.getValue() == null || dateFinTT.getValue() == null ||
                    joursTT.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs requis");
                return;
            }

            // Convertir les nouvelles valeurs
            Date nouvelleDebut = Date.from(dateDebutTT.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date nouvelleFin = Date.from(dateFinTT.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            int nouveauxJours = Integer.parseInt(joursTT.getText());

            // Vérifier si des modifications ont été apportées
            boolean modifie = false;
            if (!isSameDay(nouvelleDebut, selectedTt.getLeave_start())) modifie = true;
            else if (!isSameDay(nouvelleFin, selectedTt.getLeave_end())) modifie = true;
            else if (nouveauxJours != selectedTt.getNumber_of_days()) modifie = true;

            if (!modifie) {
                showAlert("Information", "Aucune modification n'a été détectée");
                return;
            }

            Tt tt = new Tt(
                    selectedTt.getId_tt(),
                    nouvelleDebut,
                    nouvelleFin,
                    nouveauxJours,
                    Statut.EN_COURS,
                    currentUserId
            );

            ttService.update(tt);
            clearTtFields();
            loadTts();
            showAlert("Succès", "Demande de télétravail modifiée avec succès");
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Nombre de jours invalide");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la modification du télétravail: " + e.getMessage());
        }
    }

    @FXML
    private void supprimerTt() {
        try {
            if (selectedTt == null) {
                showAlert("Erreur", "Aucune demande de télétravail sélectionnée");
                return;
            }

            if (selectedTt.getStatus() != Statut.EN_COURS) {
                showAlert("Erreur", "Seules les demandes en cours peuvent être supprimées");
                return;
            }

            // Confirmation avant suppression
            boolean confirmed = showConfirmation("Confirmation", "Voulez-vous vraiment supprimer cette demande de télétravail ?");
            if (!confirmed) {
                return;
            }

            ttService.delete(selectedTt);
            clearTtFields();
            loadTts();
            showAlert("Succès", "Demande de télétravail supprimée avec succès");
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression du télétravail: " + e.getMessage());
        }
    }

    private void loadConges() {
        try {
            ObservableList<Conge> conges = FXCollections.observableArrayList();
            // Filter only the current user's conges
            for (Conge c : congeService.showAll()) {
                if (c.getId_employe() == currentUserId) {
                    conges.add(c);
                }
            }
            tableConges.setItems(conges);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des congés: " + e.getMessage());
        }
    }

    private void loadTts() {
        try {
            ObservableList<Tt> tts = FXCollections.observableArrayList();
            // Filter only the current user's TTs
            for (Tt t : ttService.showAll()) {
                if (t.getId_employe() == currentUserId) {
                    tts.add(t);
                }
            }
            tableTT.setItems(tts);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des télétravaills: " + e.getMessage());
        }
    }

    private void clearCongeFields() {
        dateDebutConge.setValue(null);
        dateFinConge.setValue(null);
        raisonConge.setValue(null);
        descConge.clear();
        joursConge.clear();
        selectedConge = null;
        submitConge.setVisible(true);
        modifierConge.setVisible(false);
        supprimerConge.setDisable(true);
    }

    private void clearTtFields() {
        dateDebutTT.setValue(null);
        dateFinTT.setValue(null);
        joursTT.clear();
        selectedTt = null;
        submitTT.setVisible(true);
        modifierTT.setVisible(false);
        supprimerTT.setDisable(true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

}