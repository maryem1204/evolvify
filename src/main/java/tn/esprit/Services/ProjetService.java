package tn.esprit.Services;

import tn.esprit.Entities.Projet;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjetService {

    private Connection getConnection() throws SQLException {
        Connection cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null || cnx.isClosed()) {
            throw new SQLException("La connexion à la base de données est fermée.");
        }
        return cnx;
    }

    public int add(Projet projet) throws SQLException {
        String query = "INSERT INTO projet (name, description, status, end_date, starter_at, abbreviation, uploaded_files) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, projet.getName());
            pst.setString(2, projet.getDescription());
            pst.setString(3, projet.getStatus().toString());
            pst.setDate(4, projet.getEnd_date() != null ? Date.valueOf(projet.getEnd_date()) : null);
            pst.setDate(5, projet.getStarter_at() != null ? Date.valueOf(projet.getStarter_at()) : null);
            pst.setString(6, projet.getAbbreviation());
            pst.setBytes(7, projet.getUploaded_files());

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int projetId = generatedKeys.getInt(1);
                        associateProjetWithEmployes(projetId, projet.getEmployes());
                        return projetId;
                    }
                }
            }
        }
        return -1;
    }

    public int update(Projet projet) throws SQLException {
        String query = "UPDATE projet SET name = ?, description = ?, status = ?, end_date = ?, starter_at = ?, abbreviation = ?, uploaded_files = COALESCE(?, uploaded_files) WHERE id_projet = ?";
        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setString(1, projet.getName());
            pst.setString(2, projet.getDescription());
            pst.setString(3, projet.getStatus().name());
            pst.setDate(4, projet.getEnd_date() != null ? Date.valueOf(projet.getEnd_date()) : null);
            pst.setDate(5, projet.getStarter_at() != null ? Date.valueOf(projet.getStarter_at()) : null);
            pst.setString(6, projet.getAbbreviation());
            pst.setBytes(7, projet.getUploaded_files());
            pst.setInt(8, projet.getId_projet());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                removeAllEmployeesFromProjet(projet.getId_projet());
                associateProjetWithEmployes(projet.getId_projet(), projet.getEmployes());
            }
            return rowsUpdated;
        }
    }

    public int delete(Projet projet) throws SQLException {
        removeAllEmployeesFromProjet(projet.getId_projet());
        String query = "DELETE FROM projet WHERE id_projet = ?";
        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setInt(1, projet.getId_projet());
            return pst.executeUpdate();
        }
    }

    public List<Projet> showAll() throws SQLException {
        String query = "SELECT * FROM projet";
        List<Projet> projets = new ArrayList<>();
        Map<Integer, Projet> projetMap = new HashMap<>(); // Pour stocker les projets

        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                int projetId = rs.getInt("id_projet");
                Projet projet = new Projet(
                        projetId,
                        rs.getString("name"),
                        rs.getString("description"),
                        Projet.Status.valueOf(rs.getString("status")),
                        rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                        rs.getDate("starter_at") != null ? rs.getDate("starter_at").toLocalDate() : null,
                        rs.getString("abbreviation"),
                        rs.getBytes("uploaded_files"),
                        new ArrayList<>() // On ajoute les employés après
                );
                projets.add(projet);
                projetMap.put(projetId, projet);
            }
        }

        // Maintenant, on récupère les employés pour chaque projet après la fermeture du ResultSet
        for (Projet projet : projets) {
            projet.setEmployes(getEmployeesByProjetId(projet.getId_projet()));
        }

        return projets;
    }


    public Projet getProjetById(int id) throws SQLException {
        String query = "SELECT * FROM projet WHERE id_projet = ?";
        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    List<Integer> employes = getEmployeesByProjetId(id);
                    return new Projet(
                            rs.getInt("id_projet"),
                            rs.getString("name"),
                            rs.getString("description"),
                            Projet.Status.valueOf(rs.getString("status")),
                            rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                            rs.getDate("starter_at") != null ? rs.getDate("starter_at").toLocalDate() : null,
                            rs.getString("abbreviation"),
                            rs.getBytes("uploaded_files"),
                            employes
                    );
                }
            }
        }
        return null;
    }

    private void associateProjetWithEmployes(int projetId, List<Integer> employeIds) throws SQLException {
        if (employeIds == null || employeIds.isEmpty()) return;

        String sql = "INSERT INTO projet_employe (projet_id, employe_id) VALUES (?, ?)";
        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(sql)) {
            for (int employeId : employeIds) {
                pst.setInt(1, projetId);
                pst.setInt(2, employeId);
                pst.addBatch();
            }
            pst.executeBatch();
        }
    }

    private void removeAllEmployeesFromProjet(int projetId) throws SQLException {
        String query = "DELETE FROM projet_employe WHERE projet_id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setInt(1, projetId);
            pst.executeUpdate();
        }
    }

    private List<Integer> getEmployeesByProjetId(int projetId) throws SQLException {
        List<Integer> employes = new ArrayList<>();
        String query = "SELECT employe_id FROM projet_employe WHERE projet_id = ?";
        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query)) {

            pst.setInt(1, projetId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    employes.add(rs.getInt("employe_id"));
                }
            }
        }
        return employes;
    }


    public List<Projet> getProjectsByEmployee(int employeId) throws SQLException {
        String query = "SELECT p.* FROM projet p " +
                "JOIN projet_employe pe ON p.id_projet = pe.projet_id " +
                "WHERE pe.employe_id = ?";
        List<Projet> projets = new ArrayList<>();

        try (Connection cnx = getConnection();
             PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, employeId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    projets.add(mapProjet(rs));
                }
            }
        }
        return projets;
    }


    private Projet mapProjet(ResultSet rs) throws SQLException {
        int projetId = rs.getInt("id_projet");

        return new Projet(
                projetId,
                rs.getString("name"),
                rs.getString("description"),
                Projet.Status.valueOf(rs.getString("status")),
                rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                rs.getDate("starter_at") != null ? rs.getDate("starter_at").toLocalDate() : null,
                rs.getString("abbreviation"),
                rs.getBytes("uploaded_files"),
                getEmployeesByProjetId(projetId) // Récupérer la liste des employés associés
        );
    }



}
