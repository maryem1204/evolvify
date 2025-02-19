package tn.esprit.Entities;

import java.util.Date;

public class ListOffre {
    public enum Status {
        en_cours,
        accepte,
        refuse
    }
    private int idListOffre;
    private int idcondidate; // User est une autre classe représentant la table `user`
    private int idoffre; // Offre est une autre classe représentant la table `offre`
    private Status status; // Status est un enum que tu devras définir
    private Date datePostulation;
    public ListOffre(){}
    public ListOffre(int idListOffre, int idcondidate, Date datePostulation) {
        this.idListOffre = idListOffre;
        this.idcondidate = idcondidate;
        this.idoffre = idoffre;
        this.status = status;
        this.datePostulation = datePostulation;
    }
    public ListOffre(int idcondidate, int idoffre, String s, Date datePostulation) {

        this.idcondidate = idcondidate;
        this.idoffre = idoffre;

        this.datePostulation = datePostulation;
    }

    // Getter et Setter pour chaque attribut

    public int getIdListOffre() {
        return idListOffre;
    }

    public void setIdListOffre(int idListOffre) {
        this.idListOffre = idListOffre;
    }

    public int getIdCondidate() {
        return idcondidate;
    }

    public void setIdCondidate(int condidate) {
        this.idcondidate = condidate;
    }

    public int getIdOffre() {
        return idoffre;
    }

    public void setIdOffre(int offre) {
        this.idoffre = offre;
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
    private String nomCandidat;
    private String prenomCandidat;
    private String titreOffre;
    public String getNomCandidat() {
        return nomCandidat;
    }

    public void setNomCandidat(String nomCandidat) {
        this.nomCandidat = nomCandidat;
    }

    public String getPrenomCandidat() {
        return prenomCandidat;
    }

    public void setPrenomCandidat(String prenomCandidat) {
        this.prenomCandidat = prenomCandidat;
    }

    public String getTitreOffre() {
        return titreOffre;
    }

    public void setTitreOffre(String titreOffre) {
        this.titreOffre = titreOffre;
    }
}
