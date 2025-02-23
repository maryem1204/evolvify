package tn.esprit.Entities;

import tn.esprit.Services.MoyenTransportCRUD;

import java.sql.SQLException;
import java.sql.Time;
import java.util.Optional;

public class Trajet {
    private int idT;
    private String pointDep;
    private String pointArr;
    private double distance;
    private Time duréeEstimé;
    private int idMoyen;
    private int idEmploye;
    private StatusTrajet status;

    private MoyenTransport moyenTransport;
    private static MoyenTransportCRUD moyenTransportCRUD = new MoyenTransportCRUD(); // ✅ Injection statique

    // ✅ Constructeur par défaut
    public Trajet() {
    }

    // ✅ Constructeur complet
    public Trajet(int idT, String pointDep, String pointArr, double distance, Time duréeEstimé, int idMoyen, int idEmploye, StatusTrajet status) {
        this.idT = idT;
        this.pointDep = pointDep;
        this.pointArr = pointArr;
        this.distance = distance;
        this.duréeEstimé = duréeEstimé;
        this.idMoyen = idMoyen;
        this.idEmploye = idEmploye;
        this.status = status;
    }

    // ✅ Getters et Setters
    public int getIdT() { return idT; }
    public void setIdT(int idT) { this.idT = idT; }

    public String getPointDep() { return pointDep; }
    public void setPointDep(String pointDep) { this.pointDep = pointDep; }

    public String getPointArr() { return pointArr; }
    public void setPointArr(String pointArr) { this.pointArr = pointArr; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public Time getDuréeEstimé() { return duréeEstimé; }
    public void setDuréeEstimé(Time duréeEstimé) { this.duréeEstimé = duréeEstimé; }

    public int getIdMoyen() { return idMoyen; }
    public void setIdMoyen(int idMoyen) { this.idMoyen = idMoyen; }

    public int getIdEmploye() { return idEmploye; }
    public void setIdEmploye(int idEmploye) { this.idEmploye = idEmploye; }

    public StatusTrajet getStatus() { return status; }
    public void setStatus(StatusTrajet status) { this.status = status; }

    public MoyenTransport getMoyenTransport() { return this.moyenTransport; }
    public void setMoyenTransport(MoyenTransport moyenTransport) { this.moyenTransport = moyenTransport; }

    // ✅ Correction de la récupération du type de moyen de transport
    public String getTypeMoyen() {
        try {
            Optional<MoyenTransport> moyenTransportOpt = moyenTransportCRUD.findById(this.idMoyen);
            return moyenTransportOpt.map(MoyenTransport::getTypeMoyen).orElse("Inconnu");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération du type de moyen de transport : " + e.getMessage());
            return "Erreur";
        }
    }

    @Override
    public String toString() {
        return "Trajet{" +
                "idT=" + idT +
                ", pointDep='" + pointDep + '\'' +
                ", pointArr='" + pointArr + '\'' +
                ", distance=" + distance +
                ", duréeEstimé=" + duréeEstimé +
                ", idMoyen=" + idMoyen +
                ", idEmploye=" + idEmploye +
                ", status=" + status +
                ", typeMoyen=" + getTypeMoyen() +  // ✅ Affichage du type de moyen
                '}';
    }
}

