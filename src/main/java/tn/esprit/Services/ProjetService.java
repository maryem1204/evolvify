package tn.esprit.Services;

import tn.esprit.Entities.Projet;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjetService implements CRUD<Projet> {
    private Connection cnx = MyDataBase.getInstance().getCnx();
    private PreparedStatement pst;

    public int add(Projet projet) throws SQLException {
        String query = "INSERT INTO projet (name, description, status, end_date, starter_at, abbreviation, id_employe, uploaded_files) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        pst = cnx.prepareStatement(query);

        pst.setString(1, projet.getName());
        pst.setString(2, projet.getDescription());
        pst.setString(3, projet.getStatus().toString());

        // Convertir LocalDate en java.sql.Date
        if (projet.getEnd_date() != null) {
            pst.setDate(4, Date.valueOf(projet.getEnd_date()));
        } else {
            pst.setNull(4, Types.DATE);
        }

        if (projet.getStarter_at() != null) {
            pst.setDate(5, Date.valueOf(projet.getStarter_at()));
        } else {
            pst.setNull(5, Types.DATE);
        }

        pst.setString(6, projet.getAbbreviation());
        pst.setInt(7, projet.getId_employe());
        pst.setBytes(8, projet.getUploaded_files());

        return pst.executeUpdate();
    }

    public int update(Projet projet) throws SQLException {
        String query = "UPDATE projet SET name = ?, description = ?, status = ?, end_date = ?, starter_at = ?, abbreviation = ?, id_employe = ?, uploaded_files = COALESCE(?, uploaded_files) WHERE id_projet = ?";
        pst = cnx.prepareStatement(query);

        pst.setString(1, projet.getName());
        pst.setString(2, projet.getDescription());
        pst.setString(3, projet.getStatus().name());

        // Convertir LocalDate en java.sql.Date
        pst.setDate(4, projet.getEnd_date() != null ? Date.valueOf(projet.getEnd_date()) : null);
        pst.setDate(5, projet.getStarter_at() != null ? Date.valueOf(projet.getStarter_at()) : null);

        pst.setString(6, projet.getAbbreviation());
        pst.setInt(7, projet.getId_employe());

        // Ne mettre à jour uploaded_files que si un fichier est fourni
        if (projet.getUploaded_files() != null) {
            pst.setBytes(8, projet.getUploaded_files());
        } else {
            pst.setNull(8, Types.BLOB);  // COALESCE conservera l'ancien fichier
        }

        pst.setInt(9, projet.getId_projet());

        return pst.executeUpdate();
    }


    @Override
    public int delete(Projet projet) throws SQLException {
        String query = "DELETE FROM projet WHERE id_projet = ?";
        pst = cnx.prepareStatement(query);
        pst.setInt(1, projet.getId_projet());
        return pst.executeUpdate();
    }

    public List<Projet> showAll() throws SQLException {
        String query = "SELECT * FROM projet";
        pst = cnx.prepareStatement(query);
        ResultSet rs = pst.executeQuery();
        List<Projet> projets = new ArrayList<>();

        while (rs.next()) {
            Projet projet = new Projet(
                    rs.getInt("id_projet"),
                    rs.getString("name"),
                    rs.getString("description"),
                    Projet.Status.valueOf(rs.getString("status")),
                    rs.getDate("end_date").toLocalDate(),  // Convertir java.sql.Date en LocalDate
                    rs.getDate("starter_at").toLocalDate(),
                    rs.getString("abbreviation"),
                    rs.getInt("id_employe"),
                    rs.getBytes("uploaded_files")
            );
            projets.add(projet);
        }
        return projets;
    }

    public Projet getProjetById(int id) throws SQLException {
        String query = "SELECT * FROM projet WHERE id_projet = ?";
        pst = cnx.prepareStatement(query);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return new Projet(
                    rs.getInt("id_projet"),
                    rs.getString("name"),
                    rs.getString("description"),
                    Projet.Status.valueOf(rs.getString("status")),
                    rs.getDate("end_date").toLocalDate(),  // Convertir java.sql.Date en LocalDate
                    rs.getDate("starter_at").toLocalDate(),
                    rs.getString("abbreviation"),
                    rs.getInt("id_employe"),
                    rs.getBytes("uploaded_files")
            );
        } else {
            return null;
        }
    }

    public List<Projet> getProjectsByEmployee(int employeId) throws SQLException {
        List<Projet> projets = new ArrayList<>();
        String req = "SELECT * FROM Projet WHERE id_employe = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, employeId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            projets.add(new Projet(
                    rs.getInt("id_projet"),
                    rs.getString("name"),
                    rs.getString("description"),
                    Projet.Status.valueOf(rs.getString("status")),
                    rs.getDate("end_date").toLocalDate(),  // Convertir java.sql.Date en LocalDate
                    rs.getDate("starter_at").toLocalDate(),
                    rs.getString("abbreviation"),
                    rs.getInt("id_employe"),
                    rs.getBytes("uploaded_files")
            ));
        }
        return projets;
    }


}
