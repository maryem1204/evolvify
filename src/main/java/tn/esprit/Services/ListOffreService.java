package tn.esprit.Services;

import tn.esprit.Entities.ListOffre;
import tn.esprit.Entities.Offre;
import tn.esprit.Utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListOffreService implements CRUD<ListOffre> {
    @Override
    public int add(ListOffre listOffre) throws SQLException {
        return 0;
    }

    @Override
    public int update(ListOffre listOffre) throws SQLException {
        return 0;
    }

    @Override
    public int delete(ListOffre listOffre) throws SQLException {
        return 0;
    }

    private Connection cnx = MyDataBase.getInstance().getCnx();
    @Override
    public List<ListOffre> showAll() throws SQLException {
        List<ListOffre> LO = new ArrayList<>();
        String req = "SELECT \n" +
                "    u.firstname AS nom_candidat, \n" +
                "    u.lastname AS prenom_candidat, \n" +
                "    o.titre AS titre_offre, \n" +
                "    lo.status, \n" +
                "    lo.date_postulation, \n" +
                "    lo.id_condidat, \n" +
                "    lo.id_offre\n" +
                "FROM liste_offres lo \n" +
                "JOIN users u ON lo.id_condidat = u.id_employe \n" +
                "JOIN offre o ON lo.id_offre = o.id_offre;\n";

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                // Créer une nouvelle instance de ListOffre à chaque itération
                ListOffre listOf = new ListOffre();

                // Récupérer les informations nécessaires pour ListOffre
                int idCondidat = rs.getInt("id_condidat");
                int idOffre = rs.getInt("id_offre");
                String nomCandidat = rs.getString("nom_candidat");
                String prenomCandidat = rs.getString("prenom_candidat");
                String titreOffre = rs.getString("titre_offre");
                String statusFromDb = rs.getString("status");
                Date datePostulation = rs.getDate("date_postulation");

                // Remplir l'objet ListOffre avec les données extraites
                listOf.setIdCondidate(idCondidat);
                listOf.setIdOffre(idOffre);
                listOf.setNomCandidat(nomCandidat);   // Assurez-vous que le setter existe
                listOf.setPrenomCandidat(prenomCandidat); // Assurez-vous que le setter existe
                listOf.setTitreOffre(titreOffre);      // Assurez-vous que le setter existe
                listOf.setStatus(ListOffre.Status.valueOf(statusFromDb));  // Assurez-vous que "Offre.Status" est bien défini dans votre classe
                listOf.setDatePostulation(datePostulation);

                // Ajouter l'objet à la liste
                LO.add(listOf);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'exécution de la requête SQL : " + e.getMessage());
            throw e;  // Rejeter l'exception pour être géré dans le contrôleur
        }

        return LO;  // Retourner la liste des offres
    }


}
