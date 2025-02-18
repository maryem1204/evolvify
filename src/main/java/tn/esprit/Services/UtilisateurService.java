package tn.esprit.Services;

import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService implements CRUD<Utilisateur>, CRUD_User<Utilisateur> {
    private Connection cnx = MyDataBase.getInstance().getCnx();
    private PreparedStatement pst;

    /******* ❌Add user into database *********/
    @Override
    public int add(Utilisateur user) throws SQLException {
        String req = "INSERT INTO Users (firstname, lastname, email, role) VALUES (?, ?, ?, ?)";
        pst = cnx.prepareStatement(req);
        pst.setString(1, user.getFirstname());
        pst.setString(2, user.getLastname());
        pst.setString(3, user.getEmail());
        pst.setString(4, user.getRole().toString());
        return pst.executeUpdate();
    }

    /******* ❌Display list Users *********/
    @Override
    public List<Utilisateur> showAll() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String req = "SELECT * FROM Users";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            utilisateurs.add(new Utilisateur(
                    rs.getInt("id_employe"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getBytes("profilePhoto"),
                    rs.getDate("birthdayDate") != null ? rs.getDate("birthdayDate") : Date.valueOf("2000-01-01"),
                    rs.getDate("joiningDate") != null ? rs.getDate("joiningDate") : Date.valueOf("2000-01-01"),
                    Role.valueOf(rs.getString("role")), // Correction ici
                    rs.getInt("tt_restants"),  // Ajout de la virgule ici
                    rs.getInt("conge_restant"),
                    rs.getBytes("uploaded_cv"),
                    rs.getString("num_tel")
            ));
        }


        return utilisateurs;
    }

    /******* ❌Delete User by its ID *********/
    @Override
    public int delete(Utilisateur utilisateur) throws SQLException {
        if (utilisateur.getId_employe() == 0) {
            System.out.println("Erreur : ID invalide pour la suppression !");
            return 0;
        }

        String req = "DELETE FROM Users WHERE id_employe = ?";
        pst = cnx.prepareStatement(req);
        pst.setInt(1, utilisateur.getId_employe());
        return pst.executeUpdate();
    }

    /******* ❌Update User in the database (for HR only) *********/
    @Override
    public int update(Utilisateur user) throws SQLException {
        if (user.getId_employe() == 0) {
            System.out.println(" Erreur : ID invalide pour la mise à jour !");
            return 0;
        }

        String req = "UPDATE Users SET firstname = ?, lastname = ?, email = ?, role = ? WHERE id_employe = ?";
        pst = cnx.prepareStatement(req);

        pst.setString(1, user.getFirstname());
        pst.setString(2, user.getLastname());
        pst.setString(3, user.getEmail());
        pst.setString(4, user.getRole().toString());
        pst.setInt(5, user.getId_employe());

        return pst.executeUpdate();
    }

    /******* ❌Update User Profile in the database (for employee or HR or project manager) *********/
    public int updateProfile(Utilisateur user) throws SQLException {
        if (user.getId_employe() == 0) {
            System.out.println("❌ Erreur : ID invalide pour la mise à jour !");
            return 0;
        }

        String req = "UPDATE Users SET profilePhoto = ?, birthdayDate = ?, tt_restants = ?, conge_restant = ?, uploaded_cv = ?, num_tel = ? WHERE id_employe = ?";
        pst = cnx.prepareStatement(req);

        pst.setBytes(1, user.getProfilePhoto());
        pst.setDate(2, user.getBirthdayDate() != null ? user.getBirthdayDate() : Date.valueOf("2000-01-01"));
        pst.setInt(3, user.getTt_restants());
        pst.setInt(4, user.getConge_restant());
        pst.setBytes(5, user.getUploadedCv());
        pst.setString(6, user.getNum_tel());
        pst.setInt(7, user.getId_employe());

        return pst.executeUpdate();
    }



    /******* ❌Get User profile photo to display in Profile *********/
    @Override
    public byte[] getProfilePhoto(int userId) throws SQLException {
        String query = "SELECT profilePhoto FROM Users WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getBytes("profilePhoto");
        }
        return null;
    }

    /******* ❌Update User in the database (for HR only) *********/
    @Override
    public int updateProfilePhoto(int userId, byte[] photo) throws SQLException {
        String query = "UPDATE Users SET profilePhoto = ? WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setBytes(1, photo);
        pst.setInt(2, userId);
        return pst.executeUpdate();
    }

    /******* ❌Get User by its ID *********/

    public Utilisateur getUserById(int id) throws SQLException {
        String req = "SELECT * FROM Users WHERE id_employe = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_employe"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBytes("profilePhoto"),
                        rs.getDate("birthdayDate") != null ? rs.getDate("birthdayDate") : Date.valueOf("2000-01-01"),
                        rs.getDate("joiningDate") != null ? rs.getDate("joiningDate") : Date.valueOf("2000-01-01"),
                        Role.valueOf(rs.getString("role")), // ✅ Correction ici
                        rs.getInt("tt_restants"),
                        rs.getInt("conge_restant"),
                        rs.getBytes("uploaded_cv"),
                        rs.getString("num_tel")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }

        return null;
    }

    /******* ❌Get User by its email *********/
    public Utilisateur getUserByEmail(String email) throws SQLException {
        String req = "SELECT * FROM Users WHERE email = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_employe"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBytes("profilePhoto"),
                        rs.getDate("birthdayDate") != null ? rs.getDate("birthdayDate") : Date.valueOf("2000-01-01"),
                        rs.getDate("joiningDate") != null ? rs.getDate("joiningDate") : Date.valueOf("2000-01-01"),
                        Role.valueOf(rs.getString("role")), // ✅ Correction ici
                        rs.getInt("tt_restants"),
                        rs.getInt("conge_restant"),
                        rs.getBytes("uploaded_cv"),
                        rs.getString("num_tel")
                );
            }
        }
        return null;
    }

}
