package tn.esprit.Services;
import javafx.application.Application;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class DeadLineNotification extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Vérifie immédiatement les deadlines
        checkProjectDeadlines();
    }

    public static void checkProjectDeadlines() {
        String url = "jdbc:mysql://localhost:3306/evolvify"; // Remplace avec ta base de données
        String user = "root"; // Ton utilisateur MySQL
        String password = ""; // Ton mot de passe MySQL

        String query = "SELECT name FROM projet WHERE end_date = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            LocalDate tomorrow = LocalDate.now().plusDays(1); // Vérifie les projets qui se terminent demain
            stmt.setDate(1, java.sql.Date.valueOf(tomorrow));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String projectName = rs.getString("name");
                showNotification("📢 Deadline Approche", "Le projet '" + projectName + "' doit être terminé demain !");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showNotification(String title, String message) {
        Notifications.create()
                .title(title)
                .text(message)
                .graphic(null)
                .darkStyle()
                .showInformation();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
