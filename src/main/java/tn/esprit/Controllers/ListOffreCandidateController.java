package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import tn.esprit.Entities.ListOffre;
import tn.esprit.Services.ListOffreService;
import tn.esprit.Utils.MyDataBase;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class ListOffreCandidateController {

    @FXML
    private TableColumn<ListOffre, String> colTitre;

    @FXML
    private TableColumn<ListOffre, String> colnom;

    @FXML
    private TableColumn<ListOffre, String> colprenom;

    @FXML
    private TableColumn<ListOffre, Date> datepost;
    @FXML
    private TableColumn<ListOffre, String> status;

    @FXML
    private Button gestcong;

    @FXML
    private Button gestpro;

    @FXML
    private Button gestran;

    @FXML
    private Button gestrec;

    @FXML
    private AnchorPane gests;

    @FXML
    private Button gestutiliser;

    @FXML
    private AnchorPane logo;

    @FXML
    private Button logout;

    @FXML
    private Label logoutlabel;



    @FXML
    private AnchorPane tableblanche;

    @FXML
    private TableView<ListOffre> tabledeliste;

    @FXML
    private AnchorPane tableofthings;
    private Connection cnx = MyDataBase.getInstance().getCnx();
    private final ListOffreService ListOffreService=new ListOffreService();
    @FXML
    public void initialize() {
        // Liaison des colonnes aux propriétés de l'objet ListOffre
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titreOffre"));
        colnom.setCellValueFactory(new PropertyValueFactory<>("nomCandidat"));
        colprenom.setCellValueFactory(new PropertyValueFactory<>("prenomCandidat"));

        status.setCellValueFactory(new PropertyValueFactory<>("status"));
        datepost.setCellValueFactory(new PropertyValueFactory<>("datePostulation"));

        // Récupérer les données et les afficher dans le TableView
        try {
            List<ListOffre> data = ListOffreService.showAll();  // Appeler la méthode pour récupérer les données
            tabledeliste.setItems(FXCollections.observableArrayList(data));  // Afficher dans le TableView
        } catch (SQLException e) {
            e.printStackTrace();
        }
        addActionsColumn();
    }
    @FXML
    private void addActionsColumn() {
        TableColumn<ListOffre, Void> actionColumn = new TableColumn<>("Actions");

        actionColumn.setCellFactory(param -> new TableCell<ListOffre, Void>() {
            private final ImageView okIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/Accepte.png")));
            private final ImageView notOkIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/Refuse.png")));
            private final HBox hbox = new HBox(10, okIcon, notOkIcon);

            {
                // Définition des tailles des icônes
                okIcon.setFitWidth(25);
                okIcon.setFitHeight(25);
                notOkIcon.setFitWidth(25);
                notOkIcon.setFitHeight(25);

                // Appliquer un curseur "main" pour l'interaction
                okIcon.setStyle("-fx-cursor: hand;");
                notOkIcon.setStyle("-fx-cursor: hand;");

                // Action du bouton "OK" -> Change le statut en "accepté"
                okIcon.setOnMouseClicked(event -> {
                    ListOffre offre = getTableRow().getItem();
                    if (offre != null) {
                        updateStatus(offre, "accepte");
                    }
                });

                // Action du bouton "Not OK" -> Change le statut en "refusé"
                notOkIcon.setOnMouseClicked(event -> {
                    ListOffre offre = getTableRow().getItem();
                    if (offre != null) {
                        updateStatus(offre, "refuse");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });

        // Ajouter la colonne Action à la TableView
        tabledeliste.getColumns().add(actionColumn);
    }


    private void updateStatus(ListOffre offre, String newStatus) {
        // Convertir la chaîne de caractères en un statut de l'énumération
        ListOffre.Status status = ListOffre.Status.valueOf(newStatus.toLowerCase());

        String query = "UPDATE liste_offres SET status = ? WHERE id_condidat = ? AND id_offre = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            // Utilise l'énumération dans la requête
            stmt.setString(1, status.name());  // status.name() renvoie "ACCEPTÉ" ou "REFUSÉ"
            stmt.setInt(2, offre.getIdCondidate());
            stmt.setInt(3, offre.getIdOffre());

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                afficherAlerte("Succès", "Le statut de l'offre a été mis à jour en " + status);
                offre.setStatus(status);  // Mise à jour du statut localement
                tabledeliste.refresh();  // Rafraîchir la TableView

                // Récupérer l'email du candidat et envoyer l'email
                String email = getEmailById(offre.getIdCondidate());
                if (email != null) {
                    sendEmail(email, newStatus);
                }
            } else {
                afficherAlerte("Erreur", "La mise à jour a échoué !");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Problème de connexion avec la base de données.");
        }
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Récupérer l'email du candidat (fonction non modifiée)
    private String getEmailById(int idCandidat) {
        String query = "SELECT email FROM users WHERE id_employe = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, idCandidat);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Fonction pour envoyer l'email (modifiée pour Jakarta Mail)
    private void sendEmail(String recipientEmail, String status) {
        final String fromEmail = "cherif.sarra@esprit.tn"; // Remplacez par votre email
        final String password = "ihdr zwon zlnj ngpp"; // Remplacez par votre mot de passe

        // Propriétés de la connexion SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Créer une session d'authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // Créer un message MIME
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));  // Définir l'expéditeur
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));  // Définir le destinataire
            message.setSubject("Mise à jour de votre candidature");

            // Créer le corps du message avec HTML
            String messageText = "<html><body>";
            messageText += "<p>Bonjour,</p>";
            if (status.equalsIgnoreCase("accepte")) {
                messageText += "<p><b>Félicitations !</b> Votre candidature a été <span style='color:green;'>acceptée</span>.</p>";
            } else {
                messageText += "<p>Nous sommes désolés, mais votre candidature a été <span style='color:red;'>refusée</span>.</p>";
            }
            messageText += "<p>Cordialement,<br>L'équipe RH</p>";
            messageText += "</body></html>";

            // Définir le contenu du message en HTML
            message.setContent(messageText, "text/html");

            // Envoyer l'email
            Transport.send(message);

            // Afficher une alerte pour confirmer l'envoi
            afficherAlerte("Succès", "L'email a été envoyé avec succès à " + recipientEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "L'envoi de l'email a échoué !");
        }
    }




}

