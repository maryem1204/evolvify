package tn.esprit.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private final String USER = "root";
    private final String PWD = "";
    private final String URL = "jdbc:mysql://localhost:3306/evolvify";

    //1st step
    public static MyDataBase instance;

    private Connection cnx;

    //2nd step (constructor private = no instance can be created)
    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Connected to database !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    //3rd step (returns the instance, to have only one instance of the DB)
    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
