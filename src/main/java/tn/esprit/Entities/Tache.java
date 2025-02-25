package tn.esprit.Entities;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

public class Tache {
    private int id_tache;
    private String description;
    private Status status;  // Changed to enum Status
    private LocalDate created_at;  // Changed to Date type
    private int id_employe;
    private int id_projet;
    private Priority priority;  // Changed to enum Priority
    private String location;


    public enum Status {
        TO_DO,
        IN_PROGRESS,
        DONE,
        CANCELED
    }


    public enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }


    public Tache(String description, Status status, LocalDate created_at, int id_employe, int id_projet, Priority priority, String location) {
        this.description = description;
        this.status = status;
        this.created_at = created_at;
        this.id_employe = id_employe;
        this.id_projet = id_projet;
        this.priority = priority;
        this.location = location;
    }

    // Constructor with id_tache, status as enum and created_at as Date
    public Tache(int id_tache, String description, Status status, LocalDate created_at, int id_employe, int id_projet, Priority priority, String location) {
        this.id_tache = id_tache;
        this.description = description;
        this.status = status;
        this.created_at = created_at;
        this.id_employe = id_employe;
        this.id_projet = id_projet;
        this.priority = priority;
        this.location = location;
    }

    // Getters and setters
    public int getId_tache() {
        return id_tache;
    }

    public void setId_tache(int id_tache) {
        this.id_tache = id_tache;
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

    public LocalDate getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDate created_at) {
        this.created_at = created_at;
    }

    public int getId_employe() {
        return id_employe;
    }

    public void setId_employe(int id_employe) {
        this.id_employe = id_employe;
    }

    public int getId_projet() {
        return id_projet;
    }

    public void setId_projet(int id_projet) {
        this.id_projet = id_projet;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tache tache = (Tache) o;
        return id_tache == tache.id_tache;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_tache);
    }

    @Override
    public String toString() {
        return "Tache{" +
                "id_tache=" + id_tache +
                ", description='" + description + '\'' +
                ", status=" + status +  // Enum status
                ", created_at=" + created_at +  // Date created_at
                ", id_employe=" + id_employe +
                ", id_projet=" + id_projet +
                ", priority=" + priority +  // Enum priority
                ", location='" + location + '\'' +
                '}';
    }
}
