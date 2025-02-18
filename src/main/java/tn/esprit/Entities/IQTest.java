package tn.esprit.Entities;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class IQTest {
    private int id;
    private int score;
    private Date datePassage;
    private int duree; // en minutes
    private Status status;
    private List<Integer> listOffreId; // Liste des IDs des condidates

    // Enum pour le statut du test
    public enum Status {
        PASSE, EN_ATTENTE, ECHEC
    }

    // Constructeur par défaut
    public IQTest() {}

    // Constructeur avec paramètres
    public IQTest(int id, int score, Date datePassage, int duree, Status status, List<Integer> listOffreId) {
        this.id = id;
        this.score = score;
        this.datePassage = datePassage;
        this.duree = duree;
        this.status = status;
        this.listOffreId = listOffreId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public Date getDatePassage() { return datePassage; }
    public void setDatePassage(Date datePassage) { this.datePassage = datePassage; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<Integer> getListOffreId() { return listOffreId; }
    public void setListOffreId(List<Integer> listOffreId) { this.listOffreId = listOffreId; }

    // Vérifie si le test est récent (passé dans les 30 derniers jours)
    public boolean isRecent() {
        Date now = new Date();
        long diff = now.getTime() - datePassage.getTime();
        return diff <= 30L * 24 * 60 * 60 * 1000; // 30 jours en millisecondes
    }

    // Méthode toString
    @Override
    public String toString() {
        return "IQTest{" +
                "id=" + id +
                ", score=" + score +
                ", datePassage=" + datePassage +
                ", duree=" + duree + " min" +
                ", status=" + status +
                ", listOffreId=" + listOffreId +
                '}';
    }

    // Méthode equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IQTest iqTest = (IQTest) o;
        return id == iqTest.id;
    }

    // Méthode hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

