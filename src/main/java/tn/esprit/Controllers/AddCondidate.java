package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.CandidateService;
import tn.esprit.Entities.ListOffre;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.stage.FileChooser;
import tn.esprit.Utils.MyDataBase;

import java.nio.file.Files;
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
    private byte[] profilePhotoBytes;
    private byte[] cvBytes;
    @FXML
    private Button confirme;
    @FXML
    private void importerImage() {
        // Crée un sélecteur de fichier (boîte de dialogue pour choisir un fichier)
        FileChooser fileChooser = new FileChooser();

        // Définit le titre de la boîte de dialogue
        fileChooser.setTitle("Choisir une image");

        // Ajoute un filtre pour n'afficher que les fichiers d'image (PNG, JPG, JPEG)
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));

        // Ouvre la boîte de dialogue et récupère le fichier sélectionné par l'utilisateur
        selectedImageFile = fileChooser.showOpenDialog(null);

        // Vérifie si un fichier a bien été sélectionné (si l'utilisateur n'a pas annulé)
        if (selectedImageFile != null) {
            try {
                // Convertit le fichier image en un tableau de bytes (pour un stockage en base de données)
                profilePhotoBytes = Files.readAllBytes(selectedImageFile.toPath());

                // Charge l'image depuis le fichier sélectionné
                Image image = new Image(selectedImageFile.toURI().toString());

                // Affiche l'image dans un composant ImageView
                photobutton.setImage(image);

            } catch (IOException e) {
                // Affiche une erreur en cas de problème de lecture du fichier
                e.printStackTrace();
            }
        }
    }

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

        // Vérifie si un fichier a été sélectionné (si l'utilisateur n'a pas annulé)
        if (selectedCvFile != null) {
            try {
                // Convertit le fichier CV en un tableau de bytes (pour stockage en base de données)
                cvBytes = Files.readAllBytes(selectedCvFile.toPath());

                // Affiche le nom du fichier CV dans un TextField appelé cvImporte
                cvimporté.setText(selectedCvFile.getName());

            } catch (IOException e) {
                // Affiche une erreur en cas de problème de lecture du fichier
                e.printStackTrace();
            }
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
            alert.setTitle("Error");
            alert.show();
        } else {

            byte[] profilePhotoBytes = this.profilePhotoBytes;  // Cette variable contient l'image sous forme de byte[]
            byte[] cvBytes = this.cvBytes; // Cette variable contient le CV sous forme de byte[]
            // Récupérer les dates de naissance et de dépôt depuis les DatePicker
            LocalDate localDateNaissance = datenaissancetextfield.getValue();
            LocalDate localDateDepot = datedepotTextfield.getValue();

            // Convertir LocalDate en java.sql.Date
            Date dateNaissance = Date.valueOf(localDateNaissance);
            Date dateDepot = Date.valueOf(localDateDepot);


            Utilisateur condidate = new Utilisateur(nomTextfield.getText(), prenomTextfield.getText(), emailTextfield.getText(),profilePhotoBytes, dateNaissance, dateDepot, cvBytes,  numtextfield.getText());
            CandidateService ps = new CandidateService();

            try {
                int rowsAffected = ps.add(condidate);

                int idOffre = getIdOffre();  // Récupération de l'ID de l'offre
                System.out.println("********Valeur récupérée de getIdOffre() avant instanciation : " + idOffre);
                System.out.println("*********ID employé utilisé : " + condidate.getId_employe());
                System.out.println("********Date d'inscription utilisée : " + condidate.getJoiningDate());

                ListOffre lo = new ListOffre(condidate.getId_employe(), idOffre, condidate.getJoiningDate());
                System.out.println("Objet ListOffre créé : " + lo);
                System.out.println("********Valeur récupérée de getIdOffre() aapres instanciation : " + lo.getIdOffre());
                System.out.println("*********ID employé utilisé ******** : " + lo.getIdCondidate());
                System.out.println("********Date d'inscription utilisée ******: " + lo.getDatePostulation());


                if (rowsAffected > 0) {
                    if (lo == null) {
                        System.out.println("Objet ListOffre (lo) est null !");
                    } else {
                        System.out.println("ID de l'offre à insérer : " + lo.getIdOffre());
                        System.out.println("ID de condidate à insérer : " + lo.getIdCondidate());
                        System.out.println("date  à insérer : " + lo.getDatePostulation());
                        addToListOffre(lo);
                    }



                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Échec de l'ajout du candidat.");
                    alert.show();
                }
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setContentText("Erreur lors de l'ajout du candidat : " + e.getMessage());
                alert.show();
            }




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


