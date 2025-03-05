package tn.esprit.Services;

import tn.esprit.Entities.Tache;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TacheService implements CRUD<Tache> {

    private Connection cnx = MyDataBase.getInstance().getCnx();
    private PreparedStatement pst;

    // Ajouter une tâche
    @Override
    public int add(Tache tache) throws SQLException {
        String query = "INSERT INTO tache (description, status, created_at, id_employe, id_projet, priority, location) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        PreparedStatement pst = null;

        try {
            // Obtenir une nouvelle connexion à chaque appel
            connection = MyDataBase.getInstance().getCnx();

            // Désactiver l'auto-commit pour un meilleur contrôle des transactions
            connection.setAutoCommit(false);

            pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, tache.getDescription());
            pst.setString(2, tache.getStatus().name());  // Conversion de l'enum en String
            pst.setDate(3, Date.valueOf(tache.getCreated_at()));
            pst.setInt(4, tache.getId_employe());
            pst.setInt(5, tache.getId_projet());
            pst.setString(6, tache.getPriority().name());  // Conversion de l'enum en String
            pst.setString(7, tache.getLocation());

            int rowsAffected = pst.executeUpdate();

            // Valider la transaction
            connection.commit();

            // Récupérer l'ID généré si nécessaire
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        System.out.println("Tâche ajoutée avec l'ID : " + generatedId);
                    }
                }
            }

            return rowsAffected;

        } catch (SQLException e) {
            // En cas d'erreur, annuler la transaction
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }

            // Journalisation détaillée de l'erreur
            System.err.println("Erreur lors de l'ajout de la tâche : " + e.getMessage());
            e.printStackTrace();

            throw e;  // Re-lancer l'exception pour une gestion au niveau supérieur

        } finally {
            // Toujours fermer les ressources
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException e) {
                    System.err.println("Erreur de fermeture PreparedStatement : " + e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    // Réactiver l'auto-commit
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Erreur de fermeture de connexion : " + e.getMessage());
                }
            }
        }
    }

    // Modifier une tâche
    @Override
    public int update(Tache tache) throws SQLException {
        String query = "UPDATE tache SET description = ?, status = ?, created_at = ?, id_employe = ?, id_projet = ?, priority = ?, location = ? WHERE id_tache = ?";

        Connection connection = null;
        PreparedStatement pst = null;

        try {
            // Obtenir une nouvelle connexion à chaque appel
            connection = MyDataBase.getInstance().getCnx();

            // Désactiver l'auto-commit pour un meilleur contrôle des transactions
            connection.setAutoCommit(false);

            pst = connection.prepareStatement(query);
            pst.setString(1, tache.getDescription());
            pst.setString(2, tache.getStatus().name());  // Conversion de l'enum en String
            pst.setDate(3, Date.valueOf(tache.getCreated_at()));  // Conversion de java.util.Date en java.sql.Date
            pst.setInt(4, tache.getId_employe());
            pst.setInt(5, tache.getId_projet());
            pst.setString(6, tache.getPriority().name());
            pst.setString(7, tache.getLocation());
            pst.setInt(8, tache.getId_tache());

            int rowsAffected = pst.executeUpdate();

            // Valider la transaction
            connection.commit();

            return rowsAffected;

        } catch (SQLException e) {
            // En cas d'erreur, annuler la transaction
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }

            // Journalisation détaillée de l'erreur
            System.err.println("Erreur lors de la mise à jour de la tâche : " + e.getMessage());
            e.printStackTrace();

            throw e;  // Re-lancer l'exception pour une gestion au niveau supérieur

        } finally {
            // Toujours fermer les ressources
            if (pst != null) {
                try {
                    pst.close();
                } catch (SQLException e) {
                    System.err.println("Erreur de fermeture PreparedStatement : " + e.getMessage());
                }
            }
            if (connection != null) {
                try {
                    // Réactiver l'auto-commit
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Erreur de fermeture de connexion : " + e.getMessage());
                }
            }
        }
    }

    // Supprimer une tâche
    @Override
    public int delete(Tache tache) throws SQLException {
        String query = "DELETE FROM tache WHERE id_tache = ?";

        pst = cnx.prepareStatement(query);
        pst.setInt(1, tache.getId_tache());

        return pst.executeUpdate();
    }

    // Afficher toutes les tâches
    @Override
    public List<Tache> showAll() throws SQLException {
        String query = "SELECT * FROM tache";
        List<Tache> taches = new ArrayList<>();

        pst = cnx.prepareStatement(query);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Tache tache = new Tache(
                    rs.getInt("id_tache"),
                    rs.getString("description"),
                    Tache.Status.valueOf(rs.getString("status")),  // Conversion directe en Status enum
                    rs.getDate("created_at").toLocalDate(),
                    rs.getInt("id_employe"),
                    rs.getInt("id_projet"),
                    Tache.Priority.valueOf(rs.getString("priority")),  // Conversion directe en Priority enum
                    rs.getString("location")
            );
            taches.add(tache);
        }

        return taches;
    }
    public Tache getTacheById(int id_tache) throws SQLException {
        String query = "SELECT * FROM tache WHERE id_tache = ?";
        Connection connection = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            connection = MyDataBase.getInstance().getCnx();
            pst = connection.prepareStatement(query);
            pst.setInt(1, id_tache);
            rs = pst.executeQuery();

            if (rs.next()) {
                return new Tache(
                        rs.getInt("id_tache"),
                        rs.getString("description"),
                        Tache.Status.valueOf(rs.getString("status")),
                        rs.getDate("created_at").toLocalDate(),
                        rs.getInt("id_employe"),
                        rs.getInt("id_projet"),
                        Tache.Priority.valueOf(rs.getString("priority")),
                        rs.getString("location")
                );
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            throw e;
        } finally {
            // Close resources to prevent connection leaks
            if (rs != null) rs.close();
            if (pst != null) pst.close();
            if (connection != null) connection.close();
        }
    }

    public List<Tache> getTachesByProjetId(int projetId) throws SQLException {
        List<Tache> taches = new ArrayList<>();
        Connection connection = MyDataBase.getInstance().getCnx();
        String query = "SELECT * FROM Tache WHERE id_projet = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, projetId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Tache tache = new Tache();
                tache.setId_tache(rs.getInt("id_tache"));
                tache.setDescription(rs.getString("description"));
                tache.setStatus(Tache.Status.valueOf(rs.getString("status")));
                tache.setCreated_at(rs.getDate("created_at").toLocalDate());
                tache.setId_employe(rs.getInt("id_employe"));
                tache.setId_projet(rs.getInt("id_projet"));
                tache.setPriority(Tache.Priority.valueOf(rs.getString("priority")));
                tache.setLocation(rs.getString("location"));

                taches.add(tache);
            }
        }

        return taches;
    }



}
