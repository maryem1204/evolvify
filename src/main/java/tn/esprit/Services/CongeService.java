package tn.esprit.Services;
import tn.esprit.Entities.Conge;
import tn.esprit.Entities.Reason;
import tn.esprit.Entities.Statut;
import tn.esprit.Entities.Type;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CongeService implements CRUD<Conge>{

    private Connection cnx = MyDataBase.getInstance().getCnx();
    private Statement st;
    private PreparedStatement ps;

    @Override
    public int add(Conge conge) throws SQLException {
        String req = "INSERT INTO congé (leave_start, leave_end, number_of_days, status, id_employe, type, reason, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        ps = cnx.prepareStatement(req);

        ps.setDate(1, new Date(conge.getLeave_start().getTime()));
        ps.setDate(2, new Date(conge.getLeave_end().getTime()));
        ps.setInt(3, conge.getNumber_of_days());
        ps.setString(4, conge.getStatus().name());
        ps.setInt(5, conge.getId_employe());
        ps.setString(6, conge.getType().name()); // Added missing type field
        ps.setString(7, conge.getReason().name());
        ps.setString(8, conge.getDescription());

        return ps.executeUpdate();
    }

    @Override
    public int update(Conge conge) throws SQLException {
        String req = "UPDATE congé SET leave_start = ?, leave_end = ?, number_of_days = ?, status = ?, id_employe = ?, type = ?, reason = ?, description = ? WHERE id_conge = ?";

        ps = cnx.prepareStatement(req);

        ps.setDate(1, new Date(conge.getLeave_start().getTime()));
        ps.setDate(2, new Date(conge.getLeave_end().getTime()));
        ps.setInt(3, conge.getNumber_of_days());
        ps.setString(4, conge.getStatus().name());
        ps.setInt(5, conge.getId_employe());
        ps.setString(6, conge.getType().name()); // Added missing type field
        ps.setString(7, conge.getReason().name());
        ps.setString(8, conge.getDescription());
        ps.setInt(9, conge.getId_Conge());

        int rowsUpdated = ps.executeUpdate();

        if (rowsUpdated > 0) {
            System.out.println("Mise à jour réussie pour le congé ID: " + conge.getId_Conge());
        } else {
            System.out.println("Aucune mise à jour effectuée. Vérifiez l'ID du congé.");
        }

        return rowsUpdated;
    }

    @Override
    public int delete(Conge conge) throws SQLException {
        String req = "DELETE FROM congé WHERE id_conge = ?";

        ps = cnx.prepareStatement(req);
        ps.setInt(1, conge.getId_Conge());

        int rowsDeleted = ps.executeUpdate();

        if (rowsDeleted > 0) {
            System.out.println("Congé supprimé avec succès. ID: " + conge.getId_Conge());
        } else {
            System.out.println("Échec de la suppression. Vérifiez l'ID du congé.");
        }

        return rowsDeleted;
    }

    @Override
    public List<Conge> showAll() throws SQLException {
        List<Conge> conges = new ArrayList<>();

        String req = "SELECT c.*, u.firstname, u.lastname FROM congé c JOIN Users u ON c.id_employe = u.id_employe";

        st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Reason reason;
            try {
                reason = Reason.valueOf(rs.getString("reason"));
            } catch (IllegalArgumentException e) {
                System.err.println("Unexpected reason value: " + rs.getString("reason"));
                reason = Reason.AUTRES;
            }

            Type type;
            try {
                type = Type.valueOf(rs.getString("type"));
            } catch (IllegalArgumentException e) {
                System.err.println("Unexpected type value: " + rs.getString("type"));
                // Assuming there's a default Type value. Replace with appropriate default if available
                type = null;
            }

            Conge conge = new Conge(
                    rs.getInt("id_conge"),
                    rs.getDate("leave_start"),
                    rs.getDate("leave_end"),
                    rs.getInt("number_of_days"),
                    Statut.valueOf(rs.getString("status")),
                    rs.getInt("id_employe"),
                    type,
                    reason,
                    rs.getString("description")
            );

            // Set remaining days (if these columns exist in the database)
            try {
                conge.setNum_tt_restant(rs.getInt("num_tt_restant"));
                conge.setNum_conge_restant(rs.getInt("num_conge_restant"));
            } catch (SQLException e) {
                // These fields might not be in the database yet, so catching the exception
                System.err.println("Note: num_tt_restant or num_conge_restant columns may not exist in the database");
            }

            // Récupérer le prénom et nom de l'employé
            String employeName = rs.getString("firstname") + " " + rs.getString("lastname");

            // Affichage (ou utilisation) sans modifier la classe Conge
            System.out.println("Congé ID: " + conge.getId_Conge() + " | Employé: " + employeName);

            conges.add(conge);
        }

        return conges;
    }

    public String getEmployeNameById(int idEmploye) throws SQLException {
        String query = "SELECT firstname, lastname FROM Users WHERE id_employe = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, idEmploye);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getString("firstname") + " " + rs.getString("lastname");
            }
        }

        return "Inconnu";
    }

    // Method to get a single Conge by ID
    public Conge getById(int idConge) throws SQLException {
        String req = "SELECT * FROM congé WHERE id_conge = ?";

        ps = cnx.prepareStatement(req);
        ps.setInt(1, idConge);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Reason reason;
            try {
                reason = Reason.valueOf(rs.getString("reason"));
            } catch (IllegalArgumentException e) {
                System.err.println("Unexpected reason value: " + rs.getString("reason"));
                reason = Reason.AUTRES;
            }

            Type type;
            try {
                type = Type.valueOf(rs.getString("type"));
            } catch (IllegalArgumentException e) {
                System.err.println("Unexpected type value: " + rs.getString("type"));
                type = null;
            }

            Conge conge = new Conge(
                    rs.getInt("id_conge"),
                    rs.getDate("leave_start"),
                    rs.getDate("leave_end"),
                    rs.getInt("number_of_days"),
                    Statut.valueOf(rs.getString("status")),
                    rs.getInt("id_employe"),
                    type,
                    reason,
                    rs.getString("description")
            );

            // Set remaining days (if these columns exist in the database)
            try {
                conge.setNum_tt_restant(rs.getInt("num_tt_restant"));
                conge.setNum_conge_restant(rs.getInt("num_conge_restant"));
            } catch (SQLException e) {
                // These fields might not be in the database yet
            }

            return conge;
        }

        return null;
    }

    // Method to get leaves by employee ID
    public List<Conge> getByEmployeeId(int idEmploye) throws SQLException {
        List<Conge> conges = new ArrayList<>();

        String req = "SELECT * FROM congé WHERE id_employe = ?";

        ps = cnx.prepareStatement(req);
        ps.setInt(1, idEmploye);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Reason reason;
            try {
                reason = Reason.valueOf(rs.getString("reason"));
            } catch (IllegalArgumentException e) {
                System.err.println("Unexpected reason value: " + rs.getString("reason"));
                reason = Reason.AUTRES;
            }

            Type type;
            try {
                type = Type.valueOf(rs.getString("type"));
            } catch (IllegalArgumentException e) {
                System.err.println("Unexpected type value: " + rs.getString("type"));
                type = null;
            }

            Conge conge = new Conge(
                    rs.getInt("id_conge"),
                    rs.getDate("leave_start"),
                    rs.getDate("leave_end"),
                    rs.getInt("number_of_days"),
                    Statut.valueOf(rs.getString("status")),
                    rs.getInt("id_employe"),
                    type,
                    reason,
                    rs.getString("description")
            );

            try {
                conge.setNum_tt_restant(rs.getInt("num_tt_restant"));
                conge.setNum_conge_restant(rs.getInt("num_conge_restant"));
            } catch (SQLException e) {
                // These fields might not be in the database yet
            }

            conges.add(conge);
        }

        return conges;
    }
}