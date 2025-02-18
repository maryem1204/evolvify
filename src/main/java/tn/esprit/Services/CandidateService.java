package tn.esprit.Services;

import tn.esprit.Entities.Utilisateur;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Utils.MyDataBase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class CandidateService implements CRUD<Utilisateur> {

    private Connection cnx = MyDataBase.getInstance().getCnx();//cnx : Objet Connection utilisé pour interagir avec la base de données. Il est obtenu via la classe MyDatabase.

    private PreparedStatement ps ;// Objet PreparedStatement utilisé pour exécuter des requêtes SQL paramétrées.
    @Override
    public int add(Utilisateur user) throws SQLException {
        String req = "INSERT INTO `users`(`firstname`, `lastname`, `email`, `password`, `profilePhoto`, `birthdayDate`, `joiningDate`,  `tt_restants`, `conge_restant`, `uploaded_cv`, `num_tel`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?)";

        ps = cnx.prepareStatement(req);

        ps.setString(1, user.getFirstname());
        ps.setString(2, user.getLastname());
        ps.setString(3, user.getEmail());
        ps.setString(4, user.getPassword());
        ps.setBytes(5, user.getProfilePhoto());
        ps.setDate(6, new Date(user.getBirthdayDate().getTime()));  // Conversion de Date en SQL Date
        ps.setDate(7, new Date(user.getJoiningDate().getTime()));
        ps.setInt(8, 0);
        ps.setInt(9, 0);
        ps.setBytes(10, user.getUploadedCv());
        ps.setString(11, user.getNum_tel());

        // Afficher la requête pour débogage
        System.out.println("SQL Query: " + req);

        // Exécuter la mise à jour et retourner le nombre de lignes insérées
        int rowsAffected = ps.executeUpdate();

        return rowsAffected;
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
        return List.of();
    }
}
