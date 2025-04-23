package tn.esprit.Services;

import tn.esprit.Entities.Gender;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Utils.MyDataBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.sql.*;

public class CandidateService implements CRUD<Utilisateur> {

    private Connection cnx = MyDataBase.getInstance().getCnx();

    private PreparedStatement ps ;
    @Override
    public int add(Utilisateur user) throws SQLException {
        String req = "INSERT INTO `users`(`firstname`, `lastname`, `email`, `password`, `profilePhoto`, `birthdayDate`, `joiningDate`,  `tt_restants`, `conge_restant`, `uploaded_cv`, `num_tel`, `role`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?,?)";

        ps = cnx.prepareStatement(req, PreparedStatement.RETURN_GENERATED_KEYS);

        ps.setString(1, user.getFirstname());
        ps.setString(2, user.getLastname());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        //ps.setBytes(5, user.getProfilePhoto());
        ps.setDate(6, new Date(user.getBirthdayDate().getTime()));  // Conversion de Date en SQL Date
        ps.setDate(7, new Date(user.getJoiningDate().getTime()));
        ps.setInt(8, 0);
        ps.setInt(9, 0);
        ps.setBytes(10, user.getUploadedCv());
        ps.setString(11, user.getNum_tel());
        ps.setString(12, "CONDIDAT");
        // Afficher la requête pour débogage
        System.out.println("SQL Query: " + req);

        // Exécuter la mise à jour et retourner le nombre de lignes insérées
        int rowsAffected = ps.executeUpdate();
        System.out.println("rowsAffected : " + rowsAffected);
        // Récupérer l'ID généré
        if (rowsAffected > 0) {
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);  // Récupérer l'ID généré
                    user.setId_employe(generatedId);  // Mettre à jour l'objet Utilisateur avec l'ID généré
                    System.out.println("ID du candidat généré : " + generatedId);
                }
            }
        }
        return rowsAffected;
    }
    public int getLastInsertedId() throws SQLException {
        String query = "SELECT LAST_INSERT_ID()";
        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1); // Récupérer le dernier ID inséré
            }
            return -1; // Retourner -1 si aucun ID n'est trouvé
        }
    }


    @Override
    public int update(Utilisateur user) throws SQLException {
        return 0;
    }

    @Override
    public int delete(Utilisateur user) throws SQLException {
        return 0;
    }

    @Override
    public List<Utilisateur> showAll() throws SQLException {
        List<Utilisateur> condidates = new ArrayList<>();
        System.out.println("hellos");
        String req = "SELECT id_employe,firstname, lastname, email, password, birthdayDate, joiningDate, uploaded_cv, num_tel ,gender FROM users WHERE role='CONDIDAT'";
        System.out.println("Exécution de la requête : " + req);

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId_employe(rs.getInt("id_employe"));
                utilisateur.setFirstname(rs.getString("firstname"));
                utilisateur.setLastname(rs.getString("lastname"));
                utilisateur.setEmail(rs.getString("email"));
                utilisateur.setPassword(rs.getString("password"));
                utilisateur.setUploadedCv(rs.getBytes("uploaded_cv"));
                utilisateur.setBirthdayDate(rs.getDate("birthdayDate"));
                utilisateur.setJoiningDate(rs.getDate("joiningDate"));
                utilisateur.setNum_tel(rs.getString("num_tel"));
                utilisateur.setGender(Gender.valueOf(rs.getString("gender")));


                condidates.add(utilisateur);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'exécution de la requête SQL : " + e.getMessage());
            throw e;
        }
        return condidates;
    }

    public File retrieveCVFromDatabase(int candidateId, Connection conn) throws SQLException, IOException {
        String query = "SELECT uploaded_cv FROM users WHERE id_employe = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    InputStream inputStream = rs.getBinaryStream("uploaded_cv"); // Vérification du bon nom de colonne
                    File tempFile = File.createTempFile("cv_", ".pdf"); // Génère un fichier temporaire unique
                    try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    return tempFile;
                } else {
                    throw new SQLException("CV non trouvé pour cet utilisateur.");
                }
            }
        }
    }

}
