package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.Tache;
import tn.esprit.Services.TacheService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ModifierTacheController {

    @FXML
    private TextField descriptionField;
    @FXML
    private ComboBox<String> priorityComboBox, statusComboBox, locationComboBox;
    @FXML
    private DatePicker createdAtPicker;
    @FXML
    private Button submitButton;

    private final TacheService tacheService = new TacheService();
    private Tache tache; // La tâche en cours de modification

    private final Map<String, Tache.Priority> priorityMap = new HashMap<>();
    private final Map<String, Tache.Status> statusMap = new HashMap<>();

    @FXML
    private void initialize() {
        // Initialisation des ComboBox
        priorityComboBox.getItems().addAll("Low", "Medium", "High");
        statusComboBox.getItems().addAll("To Do", "In Progress", "Done", "Canceled");
        locationComboBox.getItems().addAll("Télétravail", "Présentiel");

        priorityMap.put("Low", Tache.Priority.LOW);
        priorityMap.put("Medium", Tache.Priority.MEDIUM);
        priorityMap.put("High", Tache.Priority.HIGH);

        statusMap.put("To Do", Tache.Status.TO_DO);
        statusMap.put("In Progress", Tache.Status.IN_PROGRESS);
        statusMap.put("Done", Tache.Status.DONE);
        statusMap.put("Canceled", Tache.Status.CANCELED);
    }

    // Méthode pour pré-remplir les champs avec les valeurs actuelles
    public void setTache(Tache tache) {
        this.tache = tache;
        descriptionField.setText(tache.getDescription());
        priorityComboBox.setValue(tache.getPriority().toString());
        statusComboBox.setValue(tache.getStatus().toString());
        locationComboBox.setValue(tache.getLocation());
        createdAtPicker.setValue(tache.getCreated_at());
    }

    @FXML
    private void handleEdit() {
        String description = descriptionField.getText().trim();
        String priorityString = priorityComboBox.getValue();
        String statusString = statusComboBox.getValue();
        String location = locationComboBox.getValue();
        LocalDate createdAt = createdAtPicker.getValue();

        if (description.isEmpty() || priorityString == null || statusString == null || location == null || createdAt == null) {
            showError("Tous les champs doivent être remplis !");
            return;
        }

        // Mise à jour des valeurs de la tâche
        tache.setDescription(description);
        tache.setPriority(priorityMap.get(priorityString));
        tache.setStatus(statusMap.get(statusString));
        tache.setLocation(location);
        tache.setCreated_at(createdAt);

        try {
            int result = tacheService.update(tache);
            if (result > 0) {
                showSuccessMessage("Tâche modifiée avec succès !");
                closeWindow();
            }
        } catch (SQLException e) {
            showError("Erreur lors de la modification de la tâche.");
        }
    }


    private void closeWindow() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
}
