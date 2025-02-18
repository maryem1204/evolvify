package tn.esprit.Entities;

import java.util.Date;

public class ListOffre {
    public enum Status {
        en_cours,
        accepte,
        refuse
    }
    private int idListOffre;
    private Utilisateur condidate; // User est une autre classe représentant la table `user`
    private Offre offre; // Offre est une autre classe représentant la table `offre`
    private Status status; // Status est un enum que tu devras définir
    private Date datePostulation;

    public ListOffre(int idListOffre, Utilisateur condidate, Offre offre, Status status, Date datePostulation) {
        this.idListOffre = idListOffre;
        this.condidate = condidate;
        this.offre = offre;
        this.status = status;
        this.datePostulation = datePostulation;
    }

    // Getter et Setter pour chaque attribut

    public int getIdListOffre() {
        return idListOffre;
    }

    public void setIdListOffre(int idListOffre) {
        this.idListOffre = idListOffre;
    }

    public Utilisateur getCondidate() {
        return condidate;
    }

    public void setCondidate(Utilisateur condidate) {
        this.condidate = condidate;
    }

    public Offre getOffre() {
        return offre;
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getDatePostulation() {
        return datePostulation;
    }

    public void setDatePostulation(Date datePostulation) {
        this.datePostulation = datePostulation;
    }
}
