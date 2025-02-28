package tn.esprit.Tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import tn.esprit.Services.AbonnementCRUD;
import tn.esprit.Utils.MailSender;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainFXDash extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo1.png")));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dash.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Dashboard");

            // Obtenir les dimensions de l'écran SANS cacher la barre des tâches
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Ajustements pour occuper toute la zone visible
            double correctionX = 5;  // Élargir un peu vers la droite
            double correctionY = 5;  // Agrandir vers le bas

            primaryStage.setX(screenBounds.getMinX() - 6); // Déplace légèrement à gauche
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth() + 5);
            primaryStage.setHeight(screenBounds.getHeight() + correctionY);

            // Empêcher le redimensionnement
            primaryStage.setResizable(false);
            primaryStage.setFullScreen(false);

            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de l'interface FXML !");
        }
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        AbonnementCRUD abonnementCRUD = new AbonnementCRUD();

// Calculer le délai avant la prochaine exécution (par exemple, demain à 08:00)
        long initialDelay = TimeUnit.HOURS.toMillis(16) - System.currentTimeMillis() % TimeUnit.DAYS.toMillis(1);
        long period = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("🔔 Vérification des abonnements expirant...");
            abonnementCRUD.sendExpiryNotifications();
        }, initialDelay, period, TimeUnit.MILLISECONDS);
    }
    private void startAbonnementNotifier() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        AbonnementCRUD abonnementCRUD = new AbonnementCRUD();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("🔄 Vérification des abonnements expirant...");
                abonnementCRUD.sendExpiryNotifications();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("❌ Erreur lors de l'envoi des notifications d'abonnement.");
            }
        }, 0, 1, TimeUnit.DAYS);
    }
   /*public static void main(String[] args) {
        int id_employe = 3; // ID de l'employé dans la base de données
        String sujet = "Mise à jour importante";
        String contenu = "Bonjour, voici une information importante pour vous.";

        MailSender.sendEmailToEmployee(id_employe, sujet, contenu);
    }*/
    }

