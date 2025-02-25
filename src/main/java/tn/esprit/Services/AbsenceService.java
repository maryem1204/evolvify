package tn.esprit.Services;

import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Utils.MyDataBase;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbsenceService implements CRUD<Absence> {

    private Connection cnx = MyDataBase.getInstance().getCnx();
    private Statement st ;
    private PreparedStatement ps ;

    @Override
    public int add(Absence absence) throws SQLException {
        String req = "INSERT INTO `absence`(`status`, `date`, `id_employe`) VALUES (?, ?, ?)";

        ps = cnx.prepareStatement(req);
        ps.setString(1, absence.getStatus().name());
        ps.setDate(2, new Date(absence.getDate().getTime()));
        ps.setInt(3, absence.getIdEmployee());

        return ps.executeUpdate();
    }

    @Override
    public int update(Absence absence) throws SQLException {
        String req ="UPDATE absence SET status = ?, date = ?, id_employe = ? WHERE id_absence = ?";
        ps = cnx.prepareStatement(req);
        ps.setString(1, absence.getStatus().name());
        ps.setDate(2, new Date(absence.getDate().getTime()));
        ps.setInt(3, absence.getIdEmployee());
        ps.setInt(4, absence.getIdAbsence());

        return ps.executeUpdate();
    }

    @Override
    public int delete(Absence absence) throws SQLException {
        String req ="DELETE FROM absence WHERE id_absence = ?";
        ps = cnx.prepareStatement(req);
        ps.setInt(1, absence.getIdAbsence());
        return ps.executeUpdate();
    }

    @Override
    public List<Absence> showAll() throws SQLException {
            List<Absence> absences = new ArrayList<>();
            String req = "SELECT * FROM `absence`";

            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Absence absence = new Absence();
                absence.setIdAbsence(rs.getInt(1));
                absence.setStatus(StatutAbsence.valueOf(rs.getString("status")));
                absence.setDate(rs.getDate(3));
                absence.setIdEmployee(rs.getInt("id_employe"));
                absences.add(absence);
            }

            return absences;
    }
}
