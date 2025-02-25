package tn.esprit.Entities;

import java.util.Date;
import java.util.Objects;

public class Tt {
    private int id_tt;
    private Date leave_start;
    private Date leave_end;
    private int number_of_days;
    private Statut status;
    private int id_employe;

    // Constructeurs
    public Tt() {}

    public Tt(int id_tt, Date leave_start, Date leave_end, int number_of_days, Statut status, int id_employe) {
        this.id_tt = id_tt;
        this.leave_start = leave_start;
        this.leave_end = leave_end;
        this.number_of_days = number_of_days;
        this.status = status;
        this.id_employe = id_employe;
    }

    // Getters et Setters
    public int getId_tt() {
        return id_tt;
    }

    public void setId_tt(int id_tt) {
        this.id_tt = id_tt;
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

    public Statut getStatus() {
        return status;
    }

    public void setStatus(Statut status) {
        this.status = status;
    }

    public int getId_employe() {
        return id_employe;
    }

    public void setId_employe(int id_employe) {
        this.id_employe = id_employe;
    }

    @Override
    public String toString() {
        return "Tt{" +
                "id_tt=" + id_tt +
                ", leave_start=" + leave_start +
                ", leave_end=" + leave_end +
                ", number_of_days=" + number_of_days +
                ", status=" + status +
                ", id_employe=" + id_employe +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tt tt = (Tt) o;
        return id_tt == tt.id_tt && number_of_days == tt.number_of_days && id_employe == tt.id_employe && Objects.equals(leave_start, tt.leave_start) && Objects.equals(leave_end, tt.leave_end) && status == tt.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_tt, leave_start, leave_end, number_of_days, status, id_employe);
    }

}
