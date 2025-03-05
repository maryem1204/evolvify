package tn.esprit.Entities;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import tn.esprit.Services.UtilisateurService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

public class Absence {

    private int id_absence;
    private StatutAbsence status;
    private Date date;
    private int id_employe;

    // Constructeur par défaut
    public Absence() {}

    // Constructeur paramétré
    public Absence(int idAbsence, StatutAbsence status, Date date, int idEmployee) {
        this.id_absence = idAbsence;
        this.status = status;
        this.date = date;
        this.id_employe = idEmployee;

    }
    // Ajout de la propriété JavaFX
    private StringProperty employeeName = new SimpleStringProperty();

    public StringProperty employeeNameProperty() {
        if (employeeName.get() == null || employeeName.get().isEmpty()) {
            employeeName.set(getEmployeeName()); // Initialise avec la valeur réelle
        }
        return employeeName;
    }

    // Getters et Setters
    public int getIdAbsence() {
        return id_absence;
    }

    public void setIdAbsence(int idAbsence) {
        this.id_absence = idAbsence;
    }

    public StatutAbsence getStatus() {
        return status;
    }

    public void setStatus(StatutAbsence status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getIdEmployee() {
        return id_employe;
    }

    public void setIdEmployee(int idEmployee) {
        this.id_employe = idEmployee;
    }

    // Méthode pour récupérer le nom de l'employé via son ID
    public String getEmployeeName() {
        UtilisateurService utilisateurService = new UtilisateurService();
        try {
            return utilisateurService.getUserById(id_employe).getFirstname() + " " + utilisateurService.getUserById(id_employe).getLastname();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Inconnu";  // Si l'employé n'est pas trouvé
        }
    }

    // Propriété JavaFX pour le statut
    private ObjectProperty<StatutAbsence> statusProperty = new SimpleObjectProperty<>();

    public ObjectProperty<StatutAbsence> statusProperty() {
        statusProperty.set(getStatus()); // Initialise avec la valeur actuelle
        return statusProperty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Absence absence = (Absence) o;
        return id_absence == absence.id_absence && id_employe == absence.id_employe && Objects.equals(status, absence.status) && Objects.equals(date, absence.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_absence, status, date, id_employe);
    }

    @Override
    public String toString() {
        return "Absence{" +
                "idAbsence=" + id_absence +
                ", status='" + status + '\'' +
                ", date=" + date +
                ", idEmployee=" + id_employe +
                '}';
    }

    public void setId_employe(int idEmploye) {
        this.id_employe = idEmploye;
    }

    public int getId_employe() {
        return id_employe;
    }
}

