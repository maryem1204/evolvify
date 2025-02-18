package tn.esprit.Services;

import tn.esprit.Entities.Offre;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OffreService implements CRUD<Offre> {

    private Connection cnx = MyDataBase.getInstance().getCnx();//cnx : Objet Connection utilisé pour interagir avec la base de données. Il est obtenu via la classe MyDatabase.

    private PreparedStatement ps ;// Objet PreparedStatement utilisé pour exécuter des requêtes SQL paramétrées.
    @Override
    public int add(Offre offre) throws SQLException {
        // Requête SQL pour insérer une offre
        String req = "INSERT INTO `offre`(`titre`, `description`, `date_publication`, `date_expiration`) " +
                "VALUES (?, ?, ?, ?)";

        // Utilisation de try-with-resources pour assurer la fermeture de PreparedStatement
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            // Remplir les paramètres de la requête
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setDate(3, new java.sql.Date(offre.getDatePublication().getTime()));  // Conversion de Date en SQL Date
            ps.setDate(4, new java.sql.Date(offre.getDateExpiration().getTime()));

            // Affichage de la requête SQL pour débogage
            System.out.println("SQL Query: " + req);

            // Exécuter la mise à jour et retourner le nombre de lignes insérées
            int rowsAffected = ps.executeUpdate();

            return rowsAffected;
        } catch (SQLException e) {
            // En cas d'exception, afficher l'erreur et la relancer
            System.err.println("Erreur lors de l'insertion de l'offre : " + e.getMessage());
            throw e;
        }
    }
    @Override
    public List<Offre> showAll() throws SQLException{
        List<Offre> offres = new ArrayList<>();
        String req = "SELECT titre, description, date_publication, date_expiration, status FROM Offre";

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Offre offre = new Offre();
                offre.setTitre(rs.getString("titre"));
                offre.setDescription(rs.getString("description"));
                offre.setDatePublication(rs.getDate("date_publication"));
                offre.setDateExpiration(rs.getDate("date_expiration"));
                String statusFromDb = rs.getString("status");
                System.out.println("Status from database: " + statusFromDb);
                offre.setStatus(Offre.Status.valueOf(statusFromDb));
                offres.add(offre);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'exécution de la requête SQL : " + e.getMessage());
        }
        return offres;
    }
    @Override
    public int update(Offre offre) throws SQLException {
        String req = "UPDATE offres SET titre = ?, description = ?, date_publication = ?, date_expiration = ?, status = ? WHERE id = ?";

        // Convertir java.util.Date en java.sql.Date
        java.sql.Date sqlDatePublication = new java.sql.Date(offre.getDatePublication().getTime());
        java.sql.Date sqlDateExpiration = new java.sql.Date(offre.getDateExpiration().getTime());

        // Déclare une variable pour le nombre de lignes affectées
        int rowsAffected = 0;

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setDate(3, sqlDatePublication);
            ps.setDate(4, sqlDateExpiration);
            ps.setString(5, offre.getStatus().name());  // Convertir l'enum en chaîne de caractères


            // Exécuter la mise à jour et récupérer le nombre de lignes affectées
            rowsAffected = ps.executeUpdate();

            System.out.println("Offre modifiée avec succès");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de l'offre : " + e.getMessage());
            throw e;  // Propager l'exception
        }

        // Retourner le nombre de lignes affectées
        return rowsAffected;
    }


    @Override
    public int delete(Offre offre) throws SQLException {
        if (offre == null || offre.getIdOffre() <= 0) {
            throw new IllegalArgumentException("L'offre à supprimer est invalide.");
        }

        String req = "DELETE FROM `offre` WHERE id_offre = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, offre.getIdOffre());
            int rowsDeleted = pst.executeUpdate();
            System.out.println(rowsDeleted + " ligne(s) supprimée(s).");
            return rowsDeleted;
        }
    }







}


