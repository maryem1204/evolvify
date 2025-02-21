package tn.esprit.Services;

import tn.esprit.Entities.Statut;
import tn.esprit.Entities.Tt;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TtService implements CRUD<Tt> {

    private Connection cnx = MyDataBase.getInstance().getCnx();
    private PreparedStatement ps;
    private Statement st;

    @Override
    public int add(Tt tt) throws SQLException {
        String req = "INSERT INTO `tt` (`leave_start`, `leave_end`, `number_of_days`, `status`, `id_employe`) VALUES (?, ?, ?, ?, ?)";
        ps = cnx.prepareStatement(req);

        ps.setDate(1, new Date(tt.getLeave_start().getTime()));
        ps.setDate(2, new Date(tt.getLeave_end().getTime()));
        ps.setInt(3, tt.getNumber_of_days());
        ps.setString(4, tt.getStatus().name()); // ENUM -> String
        ps.setInt(5, tt.getId_employe());

        return ps.executeUpdate();
    }

    @Override
    public int update(Tt tt) throws SQLException {
        String req = "UPDATE `tt` SET `leave_start` = ?, `leave_end` = ?, `number_of_days` = ?, `status` = ?, `id_employe` = ? WHERE `id_tt` = ?";
        ps = cnx.prepareStatement(req);

        ps.setDate(1, new Date(tt.getLeave_start().getTime()));
        ps.setDate(2, new Date(tt.getLeave_end().getTime()));
        ps.setInt(3, tt.getNumber_of_days());
        ps.setString(4, tt.getStatus().name());
        ps.setInt(5, tt.getId_employe());
        ps.setInt(6, tt.getId_tt());

        return ps.executeUpdate();
    }

    @Override
    public int delete(Tt tt) throws SQLException {
        String req = "DELETE FROM `tt` WHERE `id_tt` = ?";
        ps = cnx.prepareStatement(req);
        ps.setInt(1, tt.getId_tt());

        return ps.executeUpdate();
    }

    @Override
    public List<Tt> showAll() throws SQLException {
        List<Tt> tts = new ArrayList<>();
        String req = "SELECT tt.*, u.firstname, u.lastname FROM `tt` JOIN `Users` u ON tt.id_employe = u.id_employe";
        st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Tt tt = new Tt(
                    rs.getInt("id_tt"),
                    rs.getDate("leave_start"),
                    rs.getDate("leave_end"),
                    rs.getInt("number_of_days"),
                    Statut.valueOf(rs.getString("status")),
                    rs.getInt("id_employe")
            );

            String employeName = rs.getString("firstname") + " " + rs.getString("lastname");
            System.out.println("TT ID: " + tt.getId_tt() + " | Employé: " + employeName);

            tts.add(tt);
        }

        return tts;
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
}
