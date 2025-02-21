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
    private Statement st ;
    private PreparedStatement ps ;
    @Override
    public int add(Conge conge) throws SQLException {
        String req = "INSERT INTO `congé`(`leave_start`, `leave_end`, `number_of_days`, `status`, `id_employe`, `reason`, `description`)  VALUES (?, ?, ?, ?, ?, ?, ?)";

        ps = cnx.prepareStatement(req);

        ps.setDate(1, new Date(conge.getLeave_start().getTime()));
        ps.setDate(2, new Date(conge.getLeave_end().getTime()));
        ps.setInt(3, conge.getNumber_of_days());
        ps.setString(4, conge.getStatus().name()); // Conversion ENUM -> String
        ps.setInt(5, conge.getId_employe());
        ps.setString(6, conge.getReason().name()); // Conversion ENUM -> String
        ps.setString(7, conge.getDescription());

        return ps.executeUpdate();
    }

    @Override
    public int update(Conge conge) throws SQLException {
        String req = "UPDATE `congé` SET `leave_start` = ?, `leave_end` = ?, `number_of_days` = ?, `status` = ?, `id_employe` = ?, `reason` = ?, `description` = ? WHERE `id_conge` = ?";

        ps = cnx.prepareStatement(req);

        ps.setDate(1, new Date(conge.getLeave_start().getTime()));
        ps.setDate(2, new Date(conge.getLeave_end().getTime()));
        ps.setInt(3, conge.getNumber_of_days());
        ps.setString(4, conge.getStatus().name()); // ENUM -> String
        ps.setInt(5, conge.getId_employe());
        ps.setString(6, conge.getReason().name()); // ENUM -> String (correction ici)
        ps.setString(7, conge.getDescription());   // Correction : maintenant à la bonne position
        ps.setInt(8, conge.getId_Conge());         // Ajout de l'ID pour WHERE

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
        String req = "DELETE FROM `congé` WHERE `id_conge` = ?";

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

        // 🔥 Requête avec une jointure pour récupérer les informations de l'employé
        String req = "SELECT c.*, u.firstname, u.lastname FROM `congé` c JOIN `Users` u ON c.id_employe = u.id_employe";

        st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            // Récupération des informations du congé
            Conge conge = new Conge(
                    rs.getInt("id_conge"),
                    rs.getDate("leave_start"),
                    rs.getDate("leave_end"),
                    rs.getInt("number_of_days"),
                    Statut.valueOf(rs.getString("status")),
                    rs.getInt("id_employe"),
                    Reason.valueOf(rs.getString("reason")),
                    rs.getString("description")
            );

            // ✅ Récupérer le prénom et nom de l'employé
            String employeName = rs.getString("firstname") + " " + rs.getString("lastname");

            // ✅ Affichage (ou utilisation) sans modifier la classe `Conge`
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

}
