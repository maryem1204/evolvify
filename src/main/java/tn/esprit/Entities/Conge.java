package tn.esprit.Entities;

import java.util.Date;
import java.util.Objects;

public class Conge {
    private int id_Conge;
    private Date leave_start;
    private Date leave_end;
    private int number_of_days;
    private Statut status;
    private int id_employe;
    private Reason reason;
    private String description;
    private int num_tt_restant;
    private int num_conge_restant;

    // Constructeurs
    public Conge() {}

    public Conge(int id_conge, Date leave_start, Date leave_end, int number_of_days, Statut status, int id_employe, Reason reason, String description) {
        this.id_Conge = id_conge;
        this.leave_start = leave_start;
        this.leave_end = leave_end;
        this.number_of_days = number_of_days;
        this.status = (status == null) ? Statut.EN_COURS : status;
        this.id_employe = id_employe;

        this.reason = reason;
        this.description = description;

    }

    // Getters & Setters

    public int getId_Conge() {
        return id_Conge;
    }

    public void setId_Conge(int id_Conge) {
        this.id_Conge = id_Conge;
    }

    public Date getLeave_start() {
        return leave_start;
    }

    public void setLeave_start(Date leave_start) {
        this.leave_start = leave_start;
    }

    public Date getLeave_end() {
        return leave_end;
    }

    public void setLeave_end(Date leave_end) {
        this.leave_end = leave_end;
    }

    public int getNumber_of_days() {
        return number_of_days;
    }

    public void setNumber_of_days(int number_of_days) {
        this.number_of_days = number_of_days;
    }

    public int getId_employe() {
        return id_employe;
    }

    public void setId_employe(int id_employee) {
        this.id_employe = id_employee;
    }

    public Statut getStatus() {
        return status;
    }

    public void setStatus(Statut status) {
        this.status = status;
    }

    public int getNum_tt_restant() {
        return num_tt_restant;
    }

    public void setNum_tt_restant(int num_tt_restant) {
        this.num_tt_restant = num_tt_restant;
    }


    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNum_conge_restant() {
        return num_conge_restant;
    }

    public void setNum_conge_restant(int num_conge_restant) {
        this.num_conge_restant = num_conge_restant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conge conge = (Conge) o;
        return id_Conge == conge.id_Conge && number_of_days == conge.number_of_days && id_employe == conge.id_employe && num_tt_restant == conge.num_tt_restant && num_conge_restant == conge.num_conge_restant && Objects.equals(leave_start, conge.leave_start) && Objects.equals(leave_end, conge.leave_end) && status == conge.status && reason == conge.reason && Objects.equals(description, conge.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_Conge, leave_start, leave_end, number_of_days, status, id_employe, reason, description, num_tt_restant, num_conge_restant);
    }

    @Override
    public String toString() {
        return "Conge{" +
                "id_Conge=" + id_Conge +
                ", leave_start=" + leave_start +
                ", leave_end=" + leave_end +
                ", number_of_days=" + number_of_days +
                ", status=" + status +
                ", id_employe=" + id_employe +
                ", reason=" + reason +
                ", description='" + description + '\'' +
                ", num_tt_restant=" + num_tt_restant +
                ", num_conge_restant=" + num_conge_restant +
                '}';
    }
}
