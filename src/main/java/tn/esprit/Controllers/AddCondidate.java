package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import tn.esprit.Entities.ListOffre;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Utils.MyDataBase;
import tn.esprit.Services.CandidateService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;


public class AddCondidate {


    @FXML
    private Button buttonphoto;

    @FXML
    private TextField cvimporté;

    @FXML
    private DatePicker datedepotTextfield;

    @FXML
    private DatePicker datenaissancetextfield;

    @FXML
    private TextField emailTextfield;

    @FXML
    private TextField nomTextfield;

    @FXML
    private TextField numtextfield;

    @FXML
    private ImageView photobutton;

    @FXML
    private TextField prenomTextfield;

    @FXML
    private Button uploadbutton;

    private File selectedImageFile;
    private File selectedCvFile;

    private String cvPath;
    @FXML
    private Button confirme;


    @FXML
    private void importerCV() {
        // Crée un sélecteur de fichier pour choisir un document (CV)
        FileChooser fileChooser = new FileChooser();

        // Définit le titre de la boîte de dialogue
        fileChooser.setTitle("Choisir un CV (PDF ou Word)");

        // Ajoute un filtre pour afficher uniquement les fichiers PDF et Word (.pdf, .doc, .docx)
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx"));

        // Ouvre la boîte de dialogue et récupère le fichier sélectionné par l'utilisateur
        selectedCvFile = fileChooser.showOpenDialog(null);

        // Vérifie si un fichier a été sélectionné
        if (selectedCvFile != null) {
            // Stocke le chemin absolu du fichier au lieu du contenu en byte[]
            cvPath = selectedCvFile.getAbsolutePath();

            // Affiche le nom du fichier CV dans un TextField appelé cvImporte
            cvimporté.setText(selectedCvFile.getName());
        }
    }

    private Connection cnx = MyDataBase.getInstance().getCnx();
    private int idOffre;  // Variable pour stocker l'ID de l'offre

    // Méthode pour définir l'ID de l'offre
    public void setIdOffre(int idOffre) {
        System.out.println("setIdOffre() appelé, valeur : "+ idOffre);
        this.idOffre = idOffre;
    }
    public int getIdOffre() {
        System.out.println("getIdOffre() appelé, valeur : " + idOffre);
        return idOffre;
    }
    @FXML
    void add(ActionEvent event) throws IOException, SQLException {
        if (nomTextfield.getText().isEmpty() || prenomTextfield.getText().isEmpty() || emailTextfield.getText().isEmpty() || numtextfield.getText().isEmpty()
                || datenaissancetextfield.getValue() == null || datedepotTextfield.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur");
            alert.setContentText("Tous les champs doivent être remplis.");
            alert.show();
            return;
        }

        // Contrôle de saisie pour l'email
        String email = emailTextfield.getText();
        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!email.matches(emailPattern)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur de saisie");
            alert.setContentText("L'email n'est pas valide.");
            alert.show();
            return;
        }

        // Vérification que le CV n'est pas vide
        if (cvPath == null || cvPath.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur de CV");
            alert.setContentText("Le CV ne peut pas être vide.");
            alert.show();
            return;
        }

        // Récupérer les dates de naissance et de dépôt depuis les DatePicker
        LocalDate localDateNaissance = datenaissancetextfield.getValue();
        LocalDate localDateDepot = datedepotTextfield.getValue();

        // Convertir LocalDate en java.sql.Date
        Date dateNaissance = Date.valueOf(localDateNaissance);
        Date dateDepot = Date.valueOf(localDateDepot);

        // Création de l'objet Utilisateur avec cvPath au lieu de byte[]
        Utilisateur condidate = new Utilisateur(
                nomTextfield.getText(),
                prenomTextfield.getText(),
                email,
                dateNaissance,
                dateDepot,
                cvPath,  // Correction ici : on stocke le chemin au lieu de byte[]
                numtextfield.getText()
        );

        CandidateService ps = new CandidateService();

        try {
            int rowsAffected = ps.add(condidate);

            if (rowsAffected > 0) {
                int idOffre = getIdOffre();
                ListOffre lo = new ListOffre(condidate.getId_employe(), idOffre, condidate.getJoiningDate());

                addToListOffre(lo);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Échec de l'ajout du candidat.");
                alert.show();
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de base de données");
            alert.setContentText("Erreur lors de l'ajout du candidat : " + e.getMessage());
            alert.show();
        }
    }


    private void addToListOffre(ListOffre lo)
    {
        System.out.println("ID de l'offre utilisé : " + lo.getIdOffre());
        System.out.println("ID du candidat utilisé : " + lo.getIdCondidate());

        // Ajouter l'ID du candidat et de l'offre dans la table `listeoffre`
        String queryInsertListOffre = "INSERT INTO `liste_offres`( `id_condidat`, `id_offre`, `date_postulation`) VALUES (?, ?,  ?)";
        try (PreparedStatement stmt = cnx.prepareStatement(queryInsertListOffre)) {
            stmt.setInt(1, lo.getIdCondidate());
            stmt.setInt(2, lo.getIdOffre());

            stmt.setDate(3,  new Date(lo.getDatePostulation().getTime())); // La date de postulation est la même que la date de dépôt
            try {
                stmt.executeUpdate();
                System.out.println("Insertion réussie !");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Erreur SQL : " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Autre erreur : " + e.getMessage());
            }
            System.out.println("ID Candidat: " + lo.getIdCondidate());
            System.out.println("ID Offre: " + lo.getIdOffre());
            // Afficher un message de succès
            Alert alertSuccess = new Alert(Alert.AlertType.INFORMATION);
            alertSuccess.setTitle("Succès");
            alertSuccess.setContentText("Candidat ajouté et candidature enregistrée avec succès !");
            alertSuccess.show();


        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur SQL");
            alert.setContentText("Erreur lors de l'ajout du candidat dans listeoffre : " + e.getMessage());
            alert.show();
        }
    }

}


