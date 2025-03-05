package tn.esprit.Entities;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Projet {

    private int id_projet;
    private String name;
    private String description;
    private Status status;
    private LocalDate end_date;
    private LocalDate starter_at;
    private String abbreviation;
    private byte[] uploaded_files;
    private List<Integer> employes; // Liste des IDs des employés associés

    public enum Status {
        IN_PROGRESS,
        COMPLETED
    }

    // Constructeur par défaut (obligatoire pour certaines utilisations comme JSON serialization)
    public Projet() {}

    // Constructeur sans ID (pour l'ajout d'un projet)
    public Projet(String name, String description, Status status, LocalDate end_date, LocalDate starter_at,
                  String abbreviation, byte[] uploaded_files, List<Integer> employes) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.end_date = end_date;
        this.starter_at = starter_at;
        this.abbreviation = abbreviation;
        this.uploaded_files = (uploaded_files != null) ? uploaded_files : new byte[0];
        this.employes = employes;
    }

    // Constructeur avec ID (pour la récupération d'un projet existant)
    public Projet(int id_projet, String name, String description, Status status, LocalDate end_date,
                  LocalDate starter_at, String abbreviation, byte[] uploaded_files, List<Integer> employes) {
        this.id_projet = id_projet;
        this.name = name;
        this.description = description;
        this.status = status;
        this.end_date = end_date;
        this.starter_at = starter_at;
        this.abbreviation = abbreviation;
        this.uploaded_files = (uploaded_files != null) ? uploaded_files : new byte[0];
        this.employes = employes;
    }

    // Getters et Setters
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

    public byte[] getUploaded_files() {
        return uploaded_files;
    }

    public void setUploaded_files(byte[] uploaded_files) {
        this.uploaded_files = (uploaded_files != null) ? uploaded_files : new byte[0];
    }

    public List<Integer> getEmployes() {
        return employes;
    }

    public void setEmployes(List<Integer> employes) {
        this.employes = employes;
    }

    // Méthodes equals et hashCode (basées sur id_projet)
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

    // Méthode toString (affichage détaillé)
    @Override
    public String toString() {
        return "Projet{" +
                "id_projet=" + id_projet +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", end_date=" + end_date +
                ", starter_at=" + starter_at +
                ", abbreviation='" + abbreviation + '\'' +
                ", employes=" + employes +
                '}';
    }
}
