package tn.esprit.Entities;
import tn.esprit.Entities.StatusTransport;


public class MoyenTransport {
    private int idMoyen;
    private String typeMoyen;
    private int capacité;
    private int immatriculation;
    private StatusTransport status;


    // Constructeur par défaut
    public MoyenTransport() {
    }

    public MoyenTransport(int idMoyen, String typeMoyen) {
        this.idMoyen = idMoyen;
        this.typeMoyen = typeMoyen;
    }

    // Constructeur avec paramètres
    public MoyenTransport(int idMoyen, String typeMoyen, int capacité, int immatriculation, StatusTransport status) {
        this.idMoyen = idMoyen;
        this.typeMoyen = typeMoyen;
        this.capacité = capacité;
        this.immatriculation = immatriculation;
        this.status = status;
    }

    public MoyenTransport(String bus, int i, int i1, StatusTransport statusTransport) {

    }

    // Getters et Setters
    public int getIdMoyen() {
        return idMoyen;
    }

    public void setIdMoyen(int idMoyen) {
        this.idMoyen = idMoyen;
    }

    public String getTypeMoyen() {
        return typeMoyen;
    }

    public void setTypeMoyen(String typeMoyen) {
        this.typeMoyen = typeMoyen;
    }

    public int getCapacité() {
        return capacité;
    }

    public void setCapacité(int capacité) {
        this.capacité = capacité;
    }

    public int getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(int immatriculation) {
        this.immatriculation = immatriculation;
    }

    public StatusTransport getStatus() {
        return status;
    }

    public void setStatus(StatusTransport status) {
        this.status = status;
    }

    // Méthode toString() pour affichage
    @Override
    public String toString() {
        return "MoyenTransport{" +
                "idMoyen=" + idMoyen +
                ", typeMoyen='" + typeMoyen + '\'' +
                ", capacité=" + capacité +
                ", immatriculation=" + immatriculation +
                ", status=" + status +
                '}';
    }
}
