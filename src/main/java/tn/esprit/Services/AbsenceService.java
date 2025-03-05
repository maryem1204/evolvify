package tn.esprit.Services;

import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AbsenceService implements CRUD<Absence> {

    private Connection cnx = MyDataBase.getInstance().getCnx();
    private PreparedStatement ps;

    @Override
    public int add(Absence absence) throws SQLException {
        String req = "INSERT INTO absence (status, date, id_employe) VALUES (?, ?, ?)";
        ps = cnx.prepareStatement(req);
        ps.setString(1, absence.getStatus().name());
        // Converting java.util.Date to java.sql.Date
        ps.setDate(2, new java.sql.Date(absence.getDate().getTime()));
        ps.setInt(3, absence.getIdEmployee());
        return ps.executeUpdate();
    }

    @Override
    public int update(Absence absence) throws SQLException {
        String req = "UPDATE absence SET status = ?, date = ?, id_employe = ? WHERE id_absence = ?";
        ps = cnx.prepareStatement(req);
        ps.setString(1, absence.getStatus().name());
        if (absence.getDate() != null) {
            ps.setDate(2, new java.sql.Date(absence.getDate().getTime()));
        } else {
            ps.setNull(2, java.sql.Types.DATE);
        }
        ps.setInt(3, absence.getIdEmployee());
        ps.setInt(4, absence.getIdAbsence());
        return ps.executeUpdate();
    }

    @Override
    public int delete(Absence absence) throws SQLException {
        String req = "DELETE FROM absence WHERE id_absence = ?";
        ps = cnx.prepareStatement(req);
        ps.setInt(1, absence.getIdAbsence());
        return ps.executeUpdate();
    }

    @Override
    public List<Absence> showAll() throws SQLException {
        List<Absence> absences = new ArrayList<>();
        String req = "SELECT * FROM absence";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Absence absence = new Absence();
            absence.setIdAbsence(rs.getInt("id_absence"));
            absence.setStatus(StatutAbsence.valueOf(rs.getString("status")));
            absence.setDate(rs.getDate("date"));
            absence.setIdEmployee(rs.getInt("id_employe"));
            absences.add(absence);
        }
        return absences;
    }

    public List<Absence> getAbsencesForMonth(int year, int month) {
        List<Absence> absences = new ArrayList<>();
        String query = "SELECT * FROM absence WHERE YEAR(date) = ? AND MONTH(date) = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int idAbsence = rs.getInt("id_absence");
                String statusStr = rs.getString("status");
                Date date = rs.getDate("date");
                int idEmployee = rs.getInt("id_employe");
                StatutAbsence status = StatutAbsence.valueOf(statusStr.toUpperCase());
                absences.add(new Absence(idAbsence, status, date, idEmployee));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return absences;
    }

    public List<Absence> getAbsencesByDate(Date date) throws SQLException {
        List<Absence> absences = new ArrayList<>();
        String req = "SELECT * FROM absence WHERE date = ?";
        ps = cnx.prepareStatement(req);
        ps.setDate(1, date);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Absence absence = new Absence();
            absence.setIdAbsence(rs.getInt("id_absence"));
            absence.setStatus(StatutAbsence.valueOf(rs.getString("status")));
            absence.setDate(rs.getDate("date"));
            absence.setIdEmployee(rs.getInt("id_employe"));
            absences.add(absence);
        }
        return absences;
    }

    public List<Absence> getAbsencesForEmployee(int employeeId) throws SQLException {
        List<Absence> absences = new ArrayList<>();
        String query = "SELECT * FROM absence WHERE id_employe = ? ORDER BY date DESC";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Absence absence = new Absence();
                    absence.setIdAbsence(resultSet.getInt("id_absence"));
                    String statusStr = resultSet.getString("status");
                    StatutAbsence status = null;
                    if (statusStr != null) {
                        try {
                            status = StatutAbsence.valueOf(statusStr);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Unknown status: " + statusStr);
                        }
                    }
                    absence.setStatus(status);
                    absence.setDate(resultSet.getDate("date"));
                    absence.setIdEmployee(resultSet.getInt("id_employe"));
                    absences.add(absence);
                }
            }
        }
        return absences;
    }

    // New method: get a single absence record for an employee on a specific date.
    public Absence getAbsenceForEmployeeByDate(int employeeId, Date date) {
        String query = "SELECT * FROM absence WHERE id_employe = ? AND date = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            stmt.setDate(2, date);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Absence absence = new Absence();
                absence.setIdAbsence(rs.getInt("id_absence"));
                absence.setStatus(StatutAbsence.valueOf(rs.getString("status")));
                absence.setDate(rs.getDate("date"));
                absence.setIdEmployee(rs.getInt("id_employe"));
                return absence;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
