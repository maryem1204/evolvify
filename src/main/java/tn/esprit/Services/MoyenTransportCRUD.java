package tn.esprit.Services;

import tn.esprit.Entities.MoyenTransport;
import tn.esprit.Utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MoyenTransportCRUD implements CRUD<MoyenTransport> {

    private final Connection cnx;

    public MoyenTransportCRUD() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public int add(MoyenTransport mt) throws SQLException {
        String query = "INSERT INTO MoyenTransport (type_moyen, capacité, immatriculation, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, mt.getTypeMoyen());
            pst.setInt(2, mt.getCapacité());
            pst.setInt(3, mt.getImmatriculation());
            pst.setString(4, mt.getStatus());

            int rowsInserted = pst.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        mt.setIdMoyen(generatedKeys.getInt(1));
                        return mt.getIdMoyen();
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public List<MoyenTransport> showAll() throws SQLException {
        List<MoyenTransport> list = new ArrayList<>();
        String query = "SELECT * FROM MoyenTransport";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                MoyenTransport mt = new MoyenTransport(
                        rs.getInt("id_moyen"),
                        rs.getString("type_moyen"),
                        rs.getInt("capacité"),
                        rs.getInt("immatriculation"),
                        rs.getString("status")
                );
                list.add(mt);
            }
        }
        return list;
    }

    public Optional<MoyenTransport> getById(int id) throws SQLException {
        String query = "SELECT * FROM MoyenTransport WHERE id_moyen = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new MoyenTransport(
                            rs.getInt("id_moyen"),
                            rs.getString("type_moyen"),
                            rs.getInt("capacité"),
                            rs.getInt("immatriculation"),
                            rs.getString("status")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public int update(MoyenTransport mt) throws SQLException {
        String query = "UPDATE MoyenTransport SET type_moyen=?, capacité=?, immatriculation=?, status=? WHERE id_moyen=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, mt.getTypeMoyen());
            pst.setInt(2, mt.getCapacité());
            pst.setInt(3, mt.getImmatriculation());
            pst.setString(4, mt.getStatus());
            pst.setInt(5, mt.getIdMoyen());

            return pst.executeUpdate();
        }
    }

    @Override
    public int delete(MoyenTransport mt) throws SQLException {
        String query = "DELETE FROM MoyenTransport WHERE id_moyen=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, mt.getIdMoyen());
            return pst.executeUpdate();
        }
    }
    public MoyenTransport findById(int id) throws SQLException {
        MoyenTransport moyenTransport = null;
        String query = "SELECT * FROM MoyenTransport WHERE idMoyen = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                moyenTransport = new MoyenTransport(rs.getInt("idMoyen"), rs.getString("typeMoyen"));
            }
        }
        return moyenTransport;
    }


}
