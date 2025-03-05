package tn.esprit.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private final String USER = "root";
    private final String PWD = "";
    private final String URL = "jdbc:mysql://localhost:3306/evolvify?autoReconnect=true&useSSL=false";

    private static MyDataBase instance;
    private Connection cnx;

    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Connected to database !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion initiale : " + e.getMessage());
        }
    }

    public static synchronized MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public synchronized Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed() || !cnx.isValid(2)) {
                //System.out.println("Connexion perdue, tentative de reconnexion...");
                cnx = DriverManager.getConnection(URL, USER, PWD);
                //System.out.println("Reconnexion réussie !");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la reconnexion à la base de données : " + e.getMessage());
        }
        return cnx;
    }
}
