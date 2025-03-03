package tn.esprit.Controllers;

import com.google.zxing.WriterException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.Entities.Abonnement;
import tn.esprit.Entities.StatusAbonnement;
import tn.esprit.Services.AbonnementCRUD;
import tn.esprit.Utils.QRCodeGenerator;
import tn.esprit.Utils.QRCodeUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class AddAbonnementController {

    @FXML
    private Button ajouter;
    @FXML
    private DatePicker date_deb;
    @FXML
    private TextField id_employe;
    @FXML
    private DatePicker date_exp;
    @FXML
    private TextField prix;
    @FXML
    private ComboBox<String> status;
    @FXML
    private ComboBox<String> type_ab;
    @FXML
    private ImageView qrCodeImageView; // Ajout du ImageView pour afficher le QR code

    private final AbonnementCRUD abonnementCRUD = new AbonnementCRUD();

    @FXML
    public void initialize() {
        // Remplit automatiquement la date du jour
        date_deb.setValue(LocalDate.now());

        // Initialisation des valeurs des ComboBox
        type_ab.getItems().addAll("Mensuel", "Trimestriel", "Annuel");
        status.getItems().addAll("ACTIF", "INACTIF");

        // Sélection par défaut
        type_ab.setValue("Mensuel");
        status.setValue("ACTIF");
    }

    @FXML
    void ajouterAbonnement() {
        try {
            // Vérification des champs vides
            if (id_employe.getText().isEmpty() || prix.getText().isEmpty() ||
                    date_deb.getValue() == null || date_exp.getValue() == null ||
                    type_ab.getValue() == null || status.getValue() == null) {

                showAlert("Erreur", "Veuillez remplir tous les champs !");
                return;
            }

            // Vérification que id_employé est un nombre valide
            if (!isInteger(id_employe.getText())) {
                showAlert("Erreur", "L'ID de l'employé doit être un nombre entier valide !");
                return;
            }
            int idEmployeValue = Integer.parseInt(id_employe.getText());

            // Conversion des valeurs
            String typeValue = type_ab.getValue();
            double prixValue = Double.parseDouble(prix.getText());
            String statusValue = status.getValue();

            // Conversion de LocalDate en Date
            LocalDate localDateDebut = date_deb.getValue();
            LocalDate localDateExp = date_exp.getValue();
            Date dateDebutValue = Date.from(localDateDebut.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dateExpValue = Date.from(localDateExp.atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Vérification des dates
            if (dateDebutValue.after(dateExpValue)) {
                showAlert("Erreur", "La date de début doit être antérieure à la date d'expiration !");
                return;
            }

            // Conversion du statut en Enum
            StatusAbonnement statusEnum = StatusAbonnement.valueOf(statusValue.toUpperCase());

            // Création d'un objet Abonnement
            Abonnement abonnement = new Abonnement(0, typeValue, dateDebutValue, dateExpValue, prixValue, idEmployeValue, statusEnum);

            // Ajout dans la base de données
            int id = abonnementCRUD.add(abonnement);
            if (id != -1) {
                showAlert("Succès", "Abonnement ajouté avec succès !");

                // Génération et affichage du QR code
                generateAndDisplayQRCode(id, typeValue, localDateDebut, localDateExp, prixValue, idEmployeValue, statusValue);

                // Ne pas effacer les champs immédiatement pour permettre de voir le QR code
                // clearFields(); // Commenté pour voir le QR code avec les données
            } else {
                showAlert("Erreur", "L'ajout a échoué.");
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix doit être un nombre valide !");
        } catch (IllegalArgumentException e) {
            showAlert("Erreur", "Le statut sélectionné est invalide !");
        } catch (SQLException e) {
            showAlert("Erreur SQL", e.getMessage());
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur inconnue s'est produite: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Génère et affiche le QR code dans l'ImageView
     */
    private void generateAndDisplayQRCode(int id, String type, LocalDate dateDebut,
                                          LocalDate dateExp, double prix, int idEmploye,
                                          String status) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String dateDebutStr = dateDebut.format(formatter);
            String dateExpStr = dateExp.format(formatter);

            Image qrCodeImage = QRCodeGenerator.generateAbonnementQRCode(
                    id, type, dateDebutStr, dateExpStr, prix, idEmploye, status, 200, 200);

            qrCodeImageView.setImage(qrCodeImage);

            // Afficher une fenêtre de visualisation du QR code si nécessaire
            showQRCodeDialog(qrCodeImage, id);

        } catch (WriterException | IOException e) {
            showAlert("Erreur QR Code", "Impossible de générer le QR code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showQRCodeDialog(Image qrCodeImage, int abonnementId) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("QR Code de l'abonnement #" + abonnementId);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, closeButtonType);

        ImageView imageView = new ImageView(qrCodeImage);
        dialog.getDialogPane().setContent(imageView);

        // Récupérer les valeurs actuelles pour la sauvegarde
        String typeValue = type_ab.getValue();
        LocalDate localDateDebut = date_deb.getValue();
        LocalDate localDateExp = date_exp.getValue();
        double prixValue = Double.parseDouble(prix.getText());
        int idEmployeValue = Integer.parseInt(id_employe.getText());
        String statusValue = status.getValue();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String dateDebutStr = localDateDebut.format(formatter);
        String dateExpStr = localDateExp.format(formatter);

        // Créer les informations d'abonnement
        String abonnementInfo = "ID: " + abonnementId +
                "\nType: " + typeValue +
                "\nDate début: " + dateDebutStr +
                "\nDate expiration: " + dateExpStr +
                "\nPrix: " + prixValue +
                "\nID Employé: " + idEmployeValue +
                "\nStatus: " + statusValue;

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                boolean saved = QRCodeUtils.saveQRCodeToFile(
                        qrCodeImage, abonnementId, abonnementInfo, 200, 200, stage);

                if (saved) {
                    showAlert("Succès", "QR Code enregistré avec succès");
                } else {
                    showAlert("Erreur", "Échec de l'enregistrement du QR Code");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
    // Vérifie si une chaîne est un entier valide
    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        id_employe.clear();
        type_ab.setValue("Mensuel");
        prix.clear();
        date_deb.setValue(LocalDate.now());
        date_exp.setValue(null);
        status.setValue("ACTIF");
        qrCodeImageView.setImage(null); // Effacer l'image du QR code
    }
}