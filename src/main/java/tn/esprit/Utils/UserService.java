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

    public String getUserPhoneNumber(int id_employe) {
        String phone = null;
        // Correction: utilisation de id_employe au lieu de id
        String query = "SELECT num_tel FROM users WHERE id_employe = ?";

        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id_employe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    phone = rs.getString("num_tel");
                    System.out.println("🔹 Numéro récupéré pour l'ID " + id_employe + " : " + phone);
                } else {
                    System.out.println("❌ Aucun utilisateur trouvé avec l'ID : " + id_employe);
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur SQL : " + ex.getMessage());
            ex.printStackTrace();
        }
        return phone;
    }

    public String getUserName(int id_employe) {
        String name = null;
        // Correction: utilisation de id_employe au lieu de id et ajustement des noms de colonnes si nécessaire
        String query = "SELECT CONCAT(firstname, ' ', lastname) as fullname FROM users WHERE id_employe = ?";

        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id_employe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("fullname");
                    System.out.println("🔹 Nom récupéré pour l'ID " + id_employe + " : " + name);
                } else {
                    System.out.println("❌ Aucun utilisateur trouvé avec l'ID : " + id_employe);
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur SQL : " + ex.getMessage());
            ex.printStackTrace();
        }
        return name;
    }
}