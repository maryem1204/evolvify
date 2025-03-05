package tn.esprit.Utils;

import com.google.zxing.WriterException;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class QRCodeUtils {

    /**
     * Sauvegarde l'image du QR code dans un fichier sélectionné par l'utilisateur
     * @param qrCodeImage L'image à sauvegarder
     * @param abonnementId ID de l'abonnement pour le nom de fichier par défaut
     * @param abonnementInfo Les informations de l'abonnement à encoder dans le QR code
     * @param width Largeur de l'image
     * @param height Hauteur de l'image
     * @param stage Stage parent pour la boîte de dialogue
     * @return true si la sauvegarde a réussi, false sinon
     */
    public static boolean saveQRCodeToFile(Image qrCodeImage, int abonnementId,
                                           String abonnementInfo, int width, int height, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le QR Code");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );
        fileChooser.setInitialFileName("abonnement_" + abonnementId + "_qrcode.png");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                // Utilise la méthode existante pour sauvegarder le QR code
                return QRCodeGenerator.saveQRCodeToFile(abonnementInfo, width, height, file);
            } catch (WriterException | IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}