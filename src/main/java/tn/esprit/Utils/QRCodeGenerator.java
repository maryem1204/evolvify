package tn.esprit.Utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    /**
     * Génère un QR code sous forme d'image JavaFX à partir d'un texte
     * @param text Le texte à encoder dans le QR code
     * @param width Largeur de l'image
     * @param height Hauteur de l'image
     * @return Une Image JavaFX contenant le QR code
     */
    public static Image generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        return new Image(inputStream);
    }

    /**
     * Génère un QR code pour un abonnement avec toutes les informations pertinentes
     */
    public static Image generateAbonnementQRCode(int id, String type, String dateDebut,
                                                 String dateExp, double prix, int idEmploye,
                                                 String status, int width, int height) throws WriterException, IOException {
        String abonnementInfo = "ID: " + id +
                "\nType: " + type +
                "\nDate début: " + dateDebut +
                "\nDate expiration: " + dateExp +
                "\nPrix: " + prix +
                "\nID Employé: " + idEmploye +
                "\nStatus: " + status;

        return generateQRCodeImage(abonnementInfo, width, height);
    }

    /**
     * Sauvegarde directement le QR code dans un fichier, sans passer par JavaFX Image
     */
    public static boolean saveQRCodeToFile(String text, int width, int height, File file)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", fos);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sauvegarde le QR code d'un abonnement dans un fichier choisi par l'utilisateur
     */
    public static boolean saveAbonnementQRCodeToFile(int id, String type, String dateDebut,
                                                     String dateExp, double prix, int idEmploye,
                                                     String status, int width, int height, Stage stage) {
        String abonnementInfo = "ID: " + id +
                "\nType: " + type +
                "\nDate début: " + dateDebut +
                "\nDate expiration: " + dateExp +
                "\nPrix: " + prix +
                "\nID Employé: " + idEmploye +
                "\nStatus: " + status;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le QR Code");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png")
        );
        fileChooser.setInitialFileName("abonnement_" + id + "_qrcode.png");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                return saveQRCodeToFile(abonnementInfo, width, height, file);
            } catch (WriterException | IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}