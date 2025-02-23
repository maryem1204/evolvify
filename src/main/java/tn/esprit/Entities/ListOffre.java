package tn.esprit.Entities;

import java.util.Date;

public class ListOffre {
    public enum Status {
        en_cours,
        refuse,
        accepte
    }

    private int idListOffre;
    private int idCondidate; // User est une autre classe représentant la table `user`
    private int idOffre; // Offre est une autre classe représentant la table `offre`
    private Status status; // Status est un enum
    private Date datePostulation;

    private String nomCandidat;
    private String prenomCandidat;
    private String titreOffre;

    // Constructeurs

    public ListOffre() {}

    public ListOffre(int idListOffre, int idCondidate, int idOffre, Date datePostulation) {
        this.idListOffre = idListOffre;
        this.idCondidate = idCondidate;
        this.idOffre = idOffre;
        this.datePostulation = datePostulation;
        this.status = Status.en_cours; // Par défaut, le statut est "EN_COURS"
    }

    public ListOffre(int idCondidate, int idOffre, Date datePostulation) {
        this.idCondidate = idCondidate;
        this.idOffre = idOffre;
        this.datePostulation = datePostulation;
        this.status = Status.en_cours; // Par défaut
    }

    // Getters et Setters

    public int getIdListOffre() {
        return idListOffre;
    }

    public void setIdListOffre(int idListOffre) {
        this.idListOffre = idListOffre;
    }

    public int getIdCondidate() {
        return idCondidate;
    }

    public void setIdCondidate(int condidate) {
        this.idCondidate = condidate;
    }

    public int getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(int offre) {
        this.idOffre = offre;
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

    // Méthode toString pour faciliter le débogage
    @Override
    public String toString() {
        return "ListOffre{" +
                "idListOffre=" + idListOffre +
                ", idCondidate=" + idCondidate +
                ", idOffre=" + idOffre +
                ", status=" + status +
                ", datePostulation=" + datePostulation +
                ", nomCandidat='" + nomCandidat + '\'' +
                ", prenomCandidat='" + prenomCandidat + '\'' +
                ", titreOffre='" + titreOffre + '\'' +
                '}';
    }
}
