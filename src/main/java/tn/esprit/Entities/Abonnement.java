package tn.esprit.Entities;

import java.util.Date;

public class Abonnement {

    // Attributs
    private int id_Ab;
    private String type_Ab;
    private Date date_debut;
    private Date date_exp;
    private double prix;
    private int id_employe;
    private StatusAbonnement status;

    // Constructeurs
    public Abonnement() {
    }

    public Abonnement(int id_Ab, String type_Ab, Date date_debut, Date date_exp, double prix, int id_employe, StatusAbonnement status) {
        this.id_Ab = id_Ab;
        this.type_Ab = type_Ab;
        this.date_debut = date_debut;
        this.date_exp = date_exp;
        this.prix = prix;
        this.id_employe = id_employe;
        this.status = status;
    }

    // Getters et Setters
    public int getId_Ab() {
        return id_Ab;
    }

    public void setId_Ab(int id_Ab) {
        this.id_Ab = id_Ab;
    }

    public String getType_Ab() {
        return type_Ab;
    }

    public void setType_Ab(String type_Ab) {
        this.type_Ab = type_Ab;
    }

    public Date getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(Date date_debut) {
        this.date_debut = date_debut;
    }

    public Date getDate_exp() {
        return date_exp;
    }

    public void setDate_exp(Date date_exp) {
        this.date_exp = date_exp;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getId_employe() {
        return id_employe;
    }

    public void setId_employe(int id_employe) {
        this.id_employe = id_employe;
    }

    public StatusAbonnement getStatus() {
        return status;
    }

    public void setStatus(StatusAbonnement status) {
        this.status = status;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Abonnement{" +
                "id_Ab=" + id_Ab +
                ", type_Ab='" + type_Ab + '\'' +
                ", date_debut=" + date_debut +
                ", date_exp=" + date_exp +
                ", prix=" + prix +
                ", id_employe=" + id_employe +
                ", status=" + status.name() +
                '}';
    }
}
