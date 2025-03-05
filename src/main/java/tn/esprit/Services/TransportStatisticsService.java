package tn.esprit.Services;

import tn.esprit.Entities.MoyenTransport;
import tn.esprit.Entities.StatusTransport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransportStatisticsService {

    private final MoyenTransportCRUD transportCRUD;
    private static final Logger logger = Logger.getLogger(TransportStatisticsService.class.getName());

    public TransportStatisticsService() {
        this.transportCRUD = new MoyenTransportCRUD();
    }

    /**
     * Calcule la capacité totale de tous les moyens de transport
     */
    public int calculateTotalCapacity() throws SQLException {
        List<MoyenTransport> allTransports = transportCRUD.showAll();
        return allTransports.stream()
                .mapToInt(MoyenTransport::getCapacité)
                .sum();
    }

    /**
     * Calcule la capacité moyenne des moyens de transport
     */
    public double calculateAverageCapacity() throws SQLException {
        List<MoyenTransport> allTransports = transportCRUD.showAll();
        if (allTransports.isEmpty()) {
            return 0;
        }

        int totalCapacity = allTransports.stream()
                .mapToInt(MoyenTransport::getCapacité)
                .sum();

        return (double) totalCapacity / allTransports.size();
    }

    /**
     * Obtient la capacité totale par type de moyen de transport
     */
    public Map<String, Integer> getCapacityByType() throws SQLException {
        List<MoyenTransport> allTransports = transportCRUD.showAll();
        Map<String, Integer> capacityByType = new HashMap<>();

        for (MoyenTransport transport : allTransports) {
            String type = transport.getTypeMoyen();
            int capacity = transport.getCapacité();

            capacityByType.put(type, capacityByType.getOrDefault(type, 0) + capacity);
        }

        return capacityByType;
    }

    /**
     * Obtient la capacité totale par statut de transport
     */
    public Map<StatusTransport, Integer> getCapacityByStatus() throws SQLException {
        List<MoyenTransport> allTransports = transportCRUD.showAll();
        Map<StatusTransport, Integer> capacityByStatus = new HashMap<>();

        for (MoyenTransport transport : allTransports) {
            StatusTransport status = transport.getStatus();
            int capacity = transport.getCapacité();

            capacityByStatus.put(status, capacityByStatus.getOrDefault(status, 0) + capacity);
        }

        return capacityByStatus;
    }

    /**
     * Trouve le moyen de transport avec la plus grande capacité
     */
    public MoyenTransport getTransportWithHighestCapacity() throws SQLException {
        List<MoyenTransport> allTransports = transportCRUD.showAll();

        return allTransports.stream()
                .max((t1, t2) -> Integer.compare(t1.getCapacité(), t2.getCapacité()))
                .orElse(null);
    }

    /**
     * Trouve le moyen de transport avec la plus petite capacité
     */
    public MoyenTransport getTransportWithLowestCapacity() throws SQLException {
        List<MoyenTransport> allTransports = transportCRUD.showAll();

        return allTransports.stream()
                .min((t1, t2) -> Integer.compare(t1.getCapacité(), t2.getCapacité()))
                .orElse(null);
    }
}