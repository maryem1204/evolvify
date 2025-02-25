package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Entities.Gender;
import tn.esprit.Entities.Tache;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.TacheService;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AjoutTacheController {

    @FXML
    private TextField descriptionField;
    @FXML
    private ComboBox<String> priorityComboBox, statusComboBox, locationComboBox;
    @FXML
    private DatePicker createdAtPicker;
    @FXML
    private Button submitButton;
    @FXML
    private Label employeNameLabel;



    private final TacheService tacheService = new TacheService();

    // Maps pour la conversion des chaînes en énumérations
    private final Map<String, Tache.Priority> priorityMap = new HashMap<>();
    private final Map<String, Tache.Status> statusMap = new HashMap<>();

    @FXML
    private void initialize() {
        // Initialisation des ComboBox
        priorityComboBox.getItems().addAll("Low", "Medium", "High");
        priorityComboBox.setValue("Medium");

        statusComboBox.getItems().addAll("To Do", "In Progress", "Done", "Canceled");
        statusComboBox.setValue("To Do"); // Valeur par défaut : To Do

        locationComboBox.getItems().addAll("Télétravail", "Présentiel");
        locationComboBox.setValue("Télétravail");

        // Initialisation des maps
        priorityMap.put("Low", Tache.Priority.LOW);
        priorityMap.put("Medium", Tache.Priority.MEDIUM);
        priorityMap.put("High", Tache.Priority.HIGH);

        statusMap.put("To Do", Tache.Status.TO_DO);
        statusMap.put("In Progress", Tache.Status.IN_PROGRESS);
        statusMap.put("Done", Tache.Status.DONE);
        statusMap.put("Canceled", Tache.Status.CANCELED);

        try {
            List<Utilisateur> utilisateurs = getEmployees();
            employeNameLabel.setText("Employé : " + utilisateurs.get(0).getFirstname() + " " + utilisateurs.get(0).getLastname());
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des employés: " + e.getMessage());
        }
    }

    private List<Utilisateur> getEmployees() throws SQLException {
        List<Utilisateur> employees = new ArrayList<>();
        Connection cnx = MyDataBase.getInstance().getCnx();
        String query = "SELECT * FROM Users WHERE role = 'EMPLOYEE'";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            employees.add(new Utilisateur(
                    rs.getInt("id_employe"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getBytes("profilePhoto"),
                    rs.getDate("birthdayDate"),
                    rs.getDate("joiningDate"),
                    null,
                    0, 0, null, rs.getString("num_tel"),
                    Gender.valueOf(rs.getString("gender"))

            ));
        }
        return employees;
    }

    @FXML
    private void handleAddTache() {
        String description = descriptionField.getText().trim();
        String priorityString = priorityComboBox.getValue();
        String statusString = statusComboBox.getValue();
        String location = locationComboBox.getValue();
        LocalDate createdAt = createdAtPicker.getValue();

        if (description.isEmpty() || priorityString == null || statusString == null || location == null || createdAt == null) {
            showError("Tous les champs doivent être remplis !");
            return;
        }

        // Utilisation des maps pour convertir les chaînes en énumérations
        Tache.Priority priority = priorityMap.get(priorityString);
        Tache.Status status = statusMap.get(statusString);

        // Convertir LocalDate en java.util.Date
        LocalDate createdAtDate = Date.valueOf(createdAt).toLocalDate();

        // Assignation d'id_employe et id_projet statiques
        int idEmploye = 5;  // Exemple d'ID d'employé statique
        int idProjet = 72;   // Exemple d'ID de projet statique

        // Créer la tâche avec des id statiques
        Tache tache = new Tache(description, status, createdAtDate, idEmploye, idProjet, priority, location);

        try {
            int result = tacheService.add(tache);
            if (result > 0) {
                showSuccessMessage("Tâche ajoutée avec succès !");
                closeWindow();
            }
        } catch (SQLException e) {
            showError("Une erreur est survenue lors de l'ajout de la tâche.");
        }
    }
    @FXML
    private void handleClose() {
        closeWindow();
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
}
