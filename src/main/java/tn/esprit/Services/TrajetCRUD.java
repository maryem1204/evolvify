package tn.esprit.Services;

import tn.esprit.Entities.StatusTrajet;
import tn.esprit.Entities.Trajet;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrajetCRUD implements CRUD<Trajet> {
    private final Connection cnx;

    public TrajetCRUD() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    private boolean verifierAbonnementActif(int employeId) {
        String query = "SELECT status FROM abonnement WHERE id_employe = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, employeId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && "ACTIF".equalsIgnoreCase(rs.getString("status"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de l'abonnement : " + e.getMessage());
            return false;
        }
    }

    private boolean verifierMoyenTransportDisponible(int moyenId) {
        String query = "SELECT status FROM moyentransport WHERE id_moyen = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, moyenId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && "DISPONIBLE".equalsIgnoreCase(rs.getString("status"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification du moyen de transport : " + e.getMessage());
            return false;
        }
    }

    @Override
    public int add(Trajet t) throws SQLException {
        if (!verifierAbonnementActif(t.getIdEmploye())) {
            System.out.println("❌ L'employé n'a pas d'abonnement actif.");
            return 0;
        }

        if (!verifierMoyenTransportDisponible(t.getIdMoyen())) {
            System.out.println("❌ Le moyen de transport sélectionné n'est pas disponible.");
            return 0;
        }

        String query = "INSERT INTO trajet (point_dep, point_arr, distance, durée_estimé, id_moyen, id_employe, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, t.getPointDep());
            pst.setString(2, t.getPointArr());
            pst.setDouble(3, t.getDistance());
            pst.setTime(4, t.getDuréeEstimé());
            pst.setInt(5, t.getIdMoyen());
            pst.setInt(6, t.getIdEmploye());
            pst.setString(7, t.getStatus().name());

            int rowsInserted = pst.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'insertion du trajet : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Trajet> showAll() throws SQLException {
        List<Trajet> trajets = new ArrayList<>();
        String query = "SELECT * FROM trajet";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                try {
                    StatusTrajet status = StatusTrajet.valueOf(rs.getString("status").toUpperCase());
                    Trajet t = new Trajet(
                            rs.getInt("id_T"),
                            rs.getString("point_dep"),
                            rs.getString("point_arr"),
                            rs.getDouble("distance"),
                            rs.getTime("durée_estimé"),
                            rs.getInt("id_moyen"),
                            rs.getInt("id_employe"),
                            status
                    );
                    trajets.add(t);
                } catch (IllegalArgumentException e) {
                    System.err.println("⚠️ Statut inconnu trouvé en base de données : " + rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage des trajets : " + e.getMessage());
        }
        return trajets;
    }

    @Override
    public int update(Trajet t) throws SQLException {
        if (!verifierAbonnementActif(t.getIdEmploye())) {
            System.out.println("❌ L'employé n'a pas d'abonnement actif.");
            return 0;
        }

        if (!verifierMoyenTransportDisponible(t.getIdMoyen())) {
            System.out.println("❌ Le moyen de transport sélectionné n'est pas disponible.");
            return 0;
        }

        String query = "UPDATE trajet SET point_dep=?, point_arr=?, distance=?, durée_estimé=?, id_moyen=?, id_employe=?, status=? WHERE id_T=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, t.getPointDep());
            pst.setString(2, t.getPointArr());
            pst.setDouble(3, t.getDistance());
            pst.setTime(4, t.getDuréeEstimé());
            pst.setInt(5, t.getIdMoyen());
            pst.setInt(6, t.getIdEmploye());
            pst.setString(7, t.getStatus().name());
            pst.setInt(8, t.getIdT());

            return pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du trajet : " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(Trajet trajet) throws SQLException {
        String checkStatusQuery = "SELECT status FROM trajet WHERE id_T=?";
        try (PreparedStatement checkStmt = cnx.prepareStatement(checkStatusQuery)) {
            checkStmt.setInt(1, trajet.getIdT());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && "EN_COURS".equalsIgnoreCase(rs.getString("status"))) {
                    System.out.println("❌ Impossible de supprimer un trajet en cours.");
                    return 0;
                }
            }
        }

        String deleteQuery = "DELETE FROM trajet WHERE id_T=?";
        try (PreparedStatement pst = cnx.prepareStatement(deleteQuery)) {
            pst.setInt(1, trajet.getIdT());
            return pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du trajet : " + e.getMessage());
            throw e;
        }
    }

    public List<Trajet> findMostEfficient(int limit) throws SQLException {
        List<Trajet> trajets = new ArrayList<>();
        String query = "SELECT * FROM trajet ORDER BY durée_estimé DESC LIMIT ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                StatusTrajet status = StatusTrajet.valueOf(rs.getString("status").toUpperCase());
                Trajet trajet = new Trajet(
                        rs.getInt("id_T"),
                        rs.getString("point_dep"),
                        rs.getString("point_arr"),
                        rs.getDouble("distance"),
                        rs.getTime("durée_estimé"),
                        rs.getInt("id_moyen"),
                        rs.getInt("id_employe"),
                        status
                );
                trajets.add(trajet);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des trajets les plus efficaces : " + e.getMessage());
            throw e;
        }
        return trajets;
    }

}
