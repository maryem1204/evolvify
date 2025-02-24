package tn.esprit.Controllers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import tn.esprit.Services.ListOffreService;
import tn.esprit.Entities.ListOffre;
import tn.esprit.Utils.MyDataBase;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.Properties;
import static com.mysql.cj.conf.PropertyKey.logger;

public class DashboardListOffre {

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
            private final Button btnOk = new Button("OK");
            private final Button btnNotOk = new Button("Not OK");
            private final HBox hbox = new HBox(10, btnOk, btnNotOk);

            {
                // Style des boutons
                btnOk.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnNotOk.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                // Action du bouton "OK" -> Change le statut en "accepté"
                btnOk.setOnAction(event -> {
                    ListOffre offre = getTableView().getItems().get(getIndex());
                    updateStatus(offre, "accepte");

                });

                // Action du bouton "Not OK" -> Change le statut en "refusé"
                btnNotOk.setOnAction(event -> {
                    ListOffre offre = getTableView().getItems().get(getIndex());
                    updateStatus(offre, "refuse");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });


        // Ajouter la colonne Action à la TableView
        tabledeliste.getColumns().add(actionColumn);
        tabledeliste.refresh();  // Rafraîchit la TableView pour montrer les changements
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

    private void sendEmail(String recipientEmail, String status) {
        final String fromEmail = "cherif.sarra@esprit.tn"; // Remplacez par votre email
        final String password = "ihdr zwon zlnj ngpp"; // Remplacez par votre mot de passe

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Mise à jour de votre candidature");

            String messageText = "<html><body>";
            messageText += "<p>Bonjour,</p>";
            if (status.equalsIgnoreCase("accepte")) {
                messageText += "<p><b>Félicitations !</b> Votre candidature a été <span style='color:green;'>acceptée</span>.</p>";
            } else {
                messageText += "<p>Nous sommes désolés, mais votre candidature a été <span style='color:red;'>refusée</span>.</p>";
            }
            messageText += "<p>Cordialement,<br>L'équipe RH</p>";
            messageText += "</body></html>";

            message.setContent(messageText, "text/html");

            Transport.send(message);

            afficherAlerte("Succès", "L'email a été envoyé avec succès à " + recipientEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "L'envoi de l'email a échoué !");
        }
    }




}

