package tn.esprit.Entities;

import java.time.LocalDate;
import java.util.Objects;

public class Projet {

    private int id_projet;
    private String name;
    private String description;
    private Status status;
    private LocalDate end_date;
    private LocalDate starter_at;
    private String abbreviation;
    private int id_employe;
    private byte[] uploaded_files;



    public enum Status {
        IN_PROGRESS,
        COMPLETED,
    }




    public Projet(String name, String description, Status status, LocalDate end_date, LocalDate starter_at,
                  String abbreviation, int id_employe, byte[] uploaded_files) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.end_date = end_date;
        this.starter_at = starter_at;
        this.abbreviation = abbreviation;
        this.id_employe = id_employe;
        this.uploaded_files = uploaded_files != null ? uploaded_files : new byte[0];
    }


    public Projet(int id_projet, String name, String description, Status status, LocalDate end_date,
                  LocalDate starter_at, String abbreviation, int id_employe, byte[] uploaded_files) {
        this.id_projet = id_projet;
        this.name = name;
        this.description = description;
        this.status = status;
        this.end_date = end_date;
        this.starter_at = starter_at;
        this.abbreviation = abbreviation;
        this.id_employe = id_employe;
        this.uploaded_files = uploaded_files;
    }


    public int getId_projet() {
        return id_projet;
    }

    public void setId_projet(int id_projet) {
        this.id_projet = id_projet;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getEnd_date() {
        return end_date;
    }

    public void setEnd_date(LocalDate end_date) {
        this.end_date = end_date;
    }

    public LocalDate getStarter_at() {
        return starter_at;
    }

    public void setStarter_at(LocalDate starter_at) {
        this.starter_at = starter_at;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public int getId_employe() {
        return id_employe;
    }

    public void setId_employe(int id_employe) {
        this.id_employe = id_employe;
    }

    public byte[] getUploaded_files() {
        return uploaded_files;
    }

    public void setUploaded_files(byte[] uploaded_files) {
        this.uploaded_files = uploaded_files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Projet projet = (Projet) o;
        return id_projet == projet.id_projet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_projet);
    }

    @Override
    public String toString() {
        return "Projet{" +
                "id_projet=" + id_projet +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +  // Affichage du statut comme Enum
                ", end_date=" + end_date +  // Affichage de la date
                ", starter_at=" + starter_at +  // Affichage de la date
                ", abbreviation='" + abbreviation + '\'' +
                ", id_employe=" + id_employe +
                '}';
    }

}
