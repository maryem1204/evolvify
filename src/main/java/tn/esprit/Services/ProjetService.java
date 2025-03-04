package tn.esprit.Services;

import tn.esprit.Entities.Projet;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjetService implements CRUD<Projet> {
    private Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public int add(Projet projet) throws SQLException {
        String query = "INSERT INTO projet (name, description, status, end_date, starter_at, abbreviation, id_employe, uploaded_files) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, projet.getName());
            pst.setString(2, projet.getDescription());
            pst.setString(3, projet.getStatus().toString());
            pst.setDate(4, projet.getEnd_date() != null ? Date.valueOf(projet.getEnd_date()) : null);
            pst.setDate(5, projet.getStarter_at() != null ? Date.valueOf(projet.getStarter_at()) : null);
            pst.setString(6, projet.getAbbreviation());
            pst.setInt(7, projet.getId_employe());
            pst.setBytes(8, projet.getUploaded_files());

            return pst.executeUpdate();
        }
    }

    @Override
    public int update(Projet projet) throws SQLException {
        String query = "UPDATE projet SET name = ?, description = ?, status = ?, end_date = ?, starter_at = ?, abbreviation = ?, id_employe = ?, uploaded_files = COALESCE(?, uploaded_files) WHERE id_projet = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, projet.getName());
            pst.setString(2, projet.getDescription());
            pst.setString(3, projet.getStatus().name());
            pst.setDate(4, projet.getEnd_date() != null ? Date.valueOf(projet.getEnd_date()) : null);
            pst.setDate(5, projet.getStarter_at() != null ? Date.valueOf(projet.getStarter_at()) : null);
            pst.setString(6, projet.getAbbreviation());
            pst.setInt(7, projet.getId_employe());
            pst.setBytes(8, projet.getUploaded_files() != null ? projet.getUploaded_files() : null);
            pst.setInt(9, projet.getId_projet());

            return pst.executeUpdate();
        }
    }

    @Override
    public int delete(Projet projet) throws SQLException {
        String query = "DELETE FROM projet WHERE id_projet = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, projet.getId_projet());
            return pst.executeUpdate();
        }
    }

    @Override
    public List<Projet> showAll() throws SQLException {
        String query = "SELECT * FROM projet";
        List<Projet> projets = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                projets.add(mapProjet(rs));
            }
        }
        return projets;
    }

    public Projet getProjetById(int id) throws SQLException {
        String query = "SELECT * FROM projet WHERE id_projet = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapProjet(rs);
                }
            }
        }
        return null;
    }

    public List<Projet> getProjectsByEmployee(int employeId) throws SQLException {
        String query = "SELECT * FROM projet WHERE id_employe = ?";
        List<Projet> projets = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
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
        return new Projet(
                rs.getInt("id_projet"),
                rs.getString("name"),
                rs.getString("description"),
                Projet.Status.valueOf(rs.getString("status")),
                rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                rs.getDate("starter_at") != null ? rs.getDate("starter_at").toLocalDate() : null,
                rs.getString("abbreviation"),
                rs.getInt("id_employe"),
                rs.getBytes("uploaded_files")
        );
    }
}
