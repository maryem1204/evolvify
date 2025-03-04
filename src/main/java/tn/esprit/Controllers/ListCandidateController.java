package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.CandidateService;
import tn.esprit.Utils.MyDataBase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListCandidateController {

    @FXML
    private TableColumn<Utilisateur, String> prenom;
    @FXML
    private TableColumn<Utilisateur, String> nom;
    @FXML
    private TableColumn<Utilisateur, String> email;
    @FXML
    private TableColumn<Utilisateur, String> DatedeNaissance;
    @FXML
    private TableColumn<Utilisateur, String> DatedePostulation;
    @FXML
    private TableColumn<Utilisateur, String> numdetele;
    @FXML
    private TableColumn<Utilisateur, String> gender;
    @FXML
    private TableColumn<Utilisateur, Integer> idColumn;
    @FXML
    private TableColumn<Utilisateur, Void> CVcolum;

    @FXML
    private ToggleButton facondetrie;
    @FXML
    private TextField recherche;
    @FXML
    private AnchorPane tableblanche;
    @FXML
    private TableView<Utilisateur> tabledaffichage;
    @FXML
    private AnchorPane tableofthings;

    private static final Logger logger = Logger.getLogger(Dashboard.class.getName());
    private final CandidateService candidateService = new CandidateService();
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiMjI2ZDk2ZTItNTE5Yy00NGNiLTk5MDktODk4ZDFhNmI4YjVkIiwidHlwZSI6ImFwaV90b2tlbiJ9.LF_cOmX4DsGe4JNRibkmARi92rf_-c4CJRWqH6uhpDs"; // Utilisez une variable d'environnement pour sécuriser la clé API

    @FXML
    public void initialize() {
        prenom.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        nom.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        DatedeNaissance.setCellValueFactory(new PropertyValueFactory<>("birthdayDate"));
        DatedePostulation.setCellValueFactory(new PropertyValueFactory<>("joiningDate"));
        numdetele.setCellValueFactory(new PropertyValueFactory<>("num_tel"));
        gender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_employe"));

        addDownloadButtonToTable();
        loadOffres();
    }

    @FXML
    private void loadOffres() {
        try {
            List<Utilisateur> listCondidate = candidateService.showAll(); // Récupération des candidats
            ObservableList<Utilisateur> listCondidateObse = FXCollections.observableArrayList(listCondidate);
            tabledaffichage.setItems(listCondidateObse);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des offres", e);
            afficherAlerte("Erreur", "Impossible de charger les offres.");
        }
    }

    @FXML
    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addDownloadButtonToTable() {
        CVcolum.setCellFactory(param -> new TableCell<Utilisateur, Void>() {
            private final Button downloadButton = new Button("Télécharger");

            {
                downloadButton.setOnAction(event -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());

                    if (utilisateur == null) {
                        afficherAlerte("Alerte", "Aucun candidat sélectionné !");
                        return;
                    }

                    int userId = utilisateur.getId_employe();
                    try (Connection cnx = MyDataBase.getInstance().getCnx()) {
                        File cvFile = candidateService.retrieveCVFromDatabase(userId, cnx);
                        if (cvFile != null) {
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle("Enregistrer le CV");
                            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
                            File fileToSave = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());

                            if (fileToSave != null) {
                                Files.copy(cvFile.toPath(), fileToSave.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                // Analyser le CV avec EdenAI après téléchargement
                                analyzeCVWithEdenAI(cvFile);
                            }
                            cvFile.delete(); // Supprimer le fichier temporaire après utilisation
                        }
                    } catch (SQLException | IOException e) {
                        afficherAlerte("Erreur", "Erreur lors de la récupération du CV.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : downloadButton);
            }
        });
    }

    // Analyse du CV avec EdenAI
    private void analyzeCVWithEdenAI(File cvFile) {
        try {
            String cvText = extractTextFromPDF(cvFile);

            if (cvText == null || cvText.isEmpty()) {
                afficherAlerte("Erreur d'analyse", "Le texte extrait du CV est vide ou invalide.");
                return;
            }

            // Remplacez ici par le chemin du fichier sur le bureau
            String filePath = "C:/Users/HP/Desktop/cv.pdf"; // Remplacez par votre chemin

            File file = new File(filePath);
            if (!file.exists()) {
                afficherAlerte("Erreur", "Le fichier n'existe pas.");
                return;
            }

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                String url = "https://app.edenai.run/bricks/ocr/resume-parser"; // URL de l'API EdenAI
                HttpPost postRequest = new HttpPost(url);
                postRequest.setHeader("Authorization", "Bearer " + API_KEY);
                postRequest.setHeader("Content-Type", "application/json");

                // Préparation des paramètres JSON
                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("file", encodeFileToBase64(file)); // Encodage du fichier en base64
                jsonPayload.put("language", "fr"); // Langue du CV (français ici)

                StringEntity params = new StringEntity(jsonPayload.toString());
                postRequest.setEntity(params);
                int statusCode = client.execute(postRequest).getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    afficherAlerte("Erreur", "Erreur de communication avec l'API : " + statusCode);
                    return;
                }

                // Exécuter la requête et obtenir la réponse
                String response = EntityUtils.toString(client.execute(postRequest).getEntity());

                afficherResultatsIA(response); // Afficher les résultats

            } catch (Exception e) {
                afficherAlerte("Erreur d'analyse", "Erreur lors de l'analyse avec EdenAI : " + e.getMessage());
                logger.log(Level.SEVERE, "Erreur lors de l'analyse avec EdenAI", e);
            }
        } catch (IOException e) {
            afficherAlerte("Erreur d'extraction", "Erreur lors de l'extraction du texte du CV : " + e.getMessage());
            logger.log(Level.SEVERE, "Erreur lors de l'extraction du texte du CV", e);
        }
    }

    // Méthode pour encoder un fichier en base64
    private String encodeFileToBase64(File file) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        return java.util.Base64.getEncoder().encodeToString(fileBytes);
    }

    // Extraire le texte du fichier PDF
    private String extractTextFromPDF(File cvFile) throws IOException {
        try (PDDocument document = PDDocument.load(cvFile)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                throw new IOException("Le texte extrait du CV est vide.");
            }

            return text;
        }
    }

    // Afficher les résultats de l'IA
    private void afficherResultatsIA(String response) {
        try {
            if (response.startsWith("{")) {
                JSONObject jsonResponse = new JSONObject(response);
                // Extraire les données pertinentes de la réponse JSON
                String extractedText = jsonResponse.getString("extracted_text");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Résultats de l'Analyse IA");
                alert.setHeaderText(null);
                alert.setContentText("Texte extrait du CV :\n" + extractedText);
                alert.showAndWait();
                // Traitement normal
            } else {
                afficherAlerte("Erreur de l'API", "Réponse invalide de l'API : " + response);
            }

        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de l'affichage des résultats.");
            logger.log(Level.SEVERE, "Erreur lors du traitement de la réponse de l'API EdenAI", e);
        }
    }
}
