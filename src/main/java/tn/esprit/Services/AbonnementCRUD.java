package tn.esprit.Services;

import tn.esprit.Entities.Abonnement;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbonnementCRUD implements CRUD<Abonnement> {

    private final Connection cnx;

    public AbonnementCRUD() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public int add(Abonnement abonnement) throws SQLException {
        String query = "INSERT INTO abonnement (type_Ab, date_debut, date_exp, prix, id_employe, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, abonnement.getType_Ab());
            preparedStatement.setDate(2, new java.sql.Date(abonnement.getDate_debut().getTime()));
            preparedStatement.setDate(3, new java.sql.Date(abonnement.getDate_exp().getTime()));
            preparedStatement.setDouble(4, abonnement.getPrix());
            preparedStatement.setInt(5, abonnement.getId_employe());
            preparedStatement.setString(6, abonnement.getStatus());

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        abonnement.setId_Ab(generatedKeys.getInt(1));
                        return abonnement.getId_Ab();
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public List<Abonnement> showAll() throws SQLException {
        List<Abonnement> abonnements = new ArrayList<>();
        String query = "SELECT * FROM abonnement";
        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Abonnement abonnement = new Abonnement(
                        resultSet.getInt("id_Ab"),
                        resultSet.getString("type_Ab"),
                        resultSet.getDate("date_debut"),
                        resultSet.getDate("date_exp"),
                        resultSet.getDouble("prix"),
                        resultSet.getInt("id_employe"),
                        resultSet.getString("status")
                );
                abonnements.add(abonnement);
            }
        }
        return abonnements;
    }

    public Optional<Abonnement> getById(int id_Ab) throws SQLException {
        String query = "SELECT * FROM abonnement WHERE id_Ab = ?";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            preparedStatement.setInt(1, id_Ab);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new Abonnement(
                            resultSet.getInt("id_Ab"),
                            resultSet.getString("type_Ab"),
                            resultSet.getDate("date_debut"),
                            resultSet.getDate("date_exp"),
                            resultSet.getDouble("prix"),
                            resultSet.getInt("id_employe"),
                            resultSet.getString("status")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public int update(Abonnement abonnement) throws SQLException {
        if (abonnement.getDate_debut().after(abonnement.getDate_exp())) {
            throw new SQLException("❌ Erreur : La date de début doit être antérieure à la date d'expiration.");
        }

        String query = "UPDATE abonnement SET type_Ab=?, date_debut=?, date_exp=?, prix=?, id_employe=?, status=? WHERE id_Ab=?";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            preparedStatement.setString(1, abonnement.getType_Ab());
            preparedStatement.setDate(2, new java.sql.Date(abonnement.getDate_debut().getTime()));
            preparedStatement.setDate(3, new java.sql.Date(abonnement.getDate_exp().getTime()));
            preparedStatement.setDouble(4, abonnement.getPrix());
            preparedStatement.setInt(5, abonnement.getId_employe());
            preparedStatement.setString(6, abonnement.getStatus());
            preparedStatement.setInt(7, abonnement.getId_Ab());

            return preparedStatement.executeUpdate();
        }
    }

    @Override
    public int delete(Abonnement abonnement) throws SQLException {
        String query = "DELETE FROM abonnement WHERE id_Ab=?";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(query)) {
            preparedStatement.setInt(1, abonnement.getId_Ab());
            return preparedStatement.executeUpdate();
        }
    }

}
