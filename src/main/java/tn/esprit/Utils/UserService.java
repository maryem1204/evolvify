package tn.esprit.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    // Récupère la connexion à partir de MyDataBase
    private Connection getConnection() {
        return MyDataBase.getInstance().getCnx();
    }

    public String getUserPhoneNumber(int employeeId) {
        String phone = null;
        String query = "SELECT num_tel FROM users WHERE id_employe = ?";
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    phone = rs.getString("num_tel");
                    System.out.println("🔹 Numéro récupéré pour l'ID " + employeeId + " : " + phone);
                } else {
                    System.out.println("❌ Aucun utilisateur trouvé avec l'ID : " + employeeId);
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur SQL : " + ex.getMessage());
            ex.printStackTrace();
        }
        return phone;
    }


    public String getUserName(int employeeId) {
        String name = null;
        String query = "SELECT CONCAT(firstname, ' ', lastname) as fullname FROM users WHERE id = ?";
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("fullname");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return name;
    }
}
