package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.Tache;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.TacheService;
import tn.esprit.Services.UtilisateurService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ModifierTacheController {

    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private DatePicker createdAtPicker;
    @FXML private ComboBox<Utilisateur> employeComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private ComboBox<String> locationComboBox;
    @FXML private Button enregistrerButton, annulerButton;
    private TacheService tacheService = new TacheService();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private Tache tacheToEdit;

    public void initialize() {
        // Remplir le ComboBox des statuts
        statusComboBox.getItems().addAll(
                List.of(Tache.Status.values()).stream().map(Enum::name).toList()
        );

        // Remplir le ComboBox des priorités
        priorityComboBox.getItems().addAll(
                List.of(Tache.Priority.values()).stream().map(Enum::name).toList()
        );
        locationComboBox.getItems().addAll("Télétravail", "Présentiel");
        locationComboBox.setValue("Télétravail");

        // Charger les employés dans le ComboBox
        chargerEmployes();
    }

    private void chargerEmployes() {
        employeComboBox.getItems().clear();
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAllEmployees();
            employeComboBox.getItems().addAll(utilisateurs);

            employeComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Utilisateur utilisateur, boolean empty) {
                    super.updateItem(utilisateur, empty);
                    setText(empty ? "" : utilisateur.getFirstname() + " " + utilisateur.getLastname());
                }
            });

            employeComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Utilisateur utilisateur, boolean empty) {
                    super.updateItem(utilisateur, empty);
                    setText(empty ? "" : utilisateur.getFirstname() + " " + utilisateur.getLastname());
                }
            });
        } catch (SQLException e) {
            showError("Erreur lors du chargement des employés : " + e.getMessage());
        }
    }

    public void setTacheData(Tache tache) {
        if (tache != null) {
            this.tacheToEdit = tache;

            descriptionField.setText(tache.getDescription());
            createdAtPicker.setValue(tache.getCreated_at());
            statusComboBox.setValue(tache.getStatus().name());
            priorityComboBox.setValue(tache.getPriority().name());
            locationComboBox.setValue(tache.getLocation());

            for (Utilisateur user : employeComboBox.getItems()) {
                if (user.getId_employe() == tache.getId_employe()) {
                    employeComboBox.setValue(user);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleEdit() {
        if (tacheToEdit == null) {
            showError("Aucune tâche sélectionnée pour modification.");
            return;
        }

        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            showError("Le champ Description est obligatoire.");
            return;
        }

        Tache.Status status;
        Tache.Priority priority;

        try {
            status = Tache.Status.valueOf(statusComboBox.getValue());
            priority = Tache.Priority.valueOf(priorityComboBox.getValue());
        } catch (IllegalArgumentException | NullPointerException e) {
            showError("Veuillez sélectionner un statut et une priorité valides.");
            return;
        }

        LocalDate createdAt = createdAtPicker.getValue();
        Utilisateur selectedUser = employeComboBox.getValue();
        String location = locationComboBox.getValue();

        if (createdAt == null || selectedUser == null) {
            showError("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Mise à jour des données de la tâche
        tacheToEdit.setDescription(description);
        tacheToEdit.setStatus(status);
        tacheToEdit.setPriority(priority);
        tacheToEdit.setCreated_at(createdAt);
        tacheToEdit.setId_employe(selectedUser.getId_employe());
        tacheToEdit.setLocation(location);

        try {
            tacheService.update(tacheToEdit);
            closeWindow();
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour de la tâche : " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) descriptionField.getScene().getWindow();
        stage.close();
    }
}
