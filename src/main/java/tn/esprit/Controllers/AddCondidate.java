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

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import javafx.stage.FileChooser;
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

                if (rowsAffected > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setContentText("Candidat ajouté avec succès !");
                    alert.show();
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

}


