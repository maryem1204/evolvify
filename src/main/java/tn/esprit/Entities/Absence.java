package tn.esprit.Entities;

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
}

