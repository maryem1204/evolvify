package tn.esprit.Tests;

import tn.esprit.Entities.MoyenTransport;
import tn.esprit.Entities.StatusTransport;
import tn.esprit.Services.MoyenTransportCRUD;
import tn.esprit.Utils.MyDataBase;

import java.util.List;
import java.sql.SQLException;

public class MainTestM {
    public static void main(String[] args) throws SQLException {
        // Initialisation de la base de données et du service
        MyDataBase db1 = MyDataBase.getInstance();
        MoyenTransportCRUD moyenTransportCRUD = new MoyenTransportCRUD();

        // Ajouter un moyen de transport
        MoyenTransport mt1 = new MoyenTransport(0, "Bus", 50, 123456, StatusTransport.DISPONIBLE);
        moyenTransportCRUD.add(mt1);

        // Afficher tous les moyens de transport
        System.out.println("\n📌 Liste des moyens de transport après ajout :");
        List<MoyenTransport> moyens = moyenTransportCRUD.showAll();
        for (MoyenTransport mt : moyens) {
            System.out.println(mt);
        }

        // Modifier un moyen de transport existant (exemple avec le premier élément)
        if (!moyens.isEmpty()) {
            MoyenTransport mtToUpdate = moyens.get(0);
            mtToUpdate.setCapacité(60);
            mtToUpdate.setStatus(StatusTransport.EN_MAINTENANCE);
            moyenTransportCRUD.update(mtToUpdate);
        }

        // Afficher la liste après modification
        System.out.println("\n📌 Liste après modification :");
        moyens = moyenTransportCRUD.showAll();
        for (MoyenTransport mt : moyens) {
            System.out.println(mt);
        }

        // Supprimer un moyen de transport (exemple avec le premier élément)
        if (!moyens.isEmpty()) {
            MoyenTransport mtToDelete = moyens.get(0);
            moyenTransportCRUD.delete(mtToDelete);
        }

        // Afficher la liste après suppression
        System.out.println("\n📌 Liste après suppression :");
        moyens = moyenTransportCRUD.showAll();
        for (MoyenTransport mt : moyens) {
            System.out.println(mt);
        }
    }
}
