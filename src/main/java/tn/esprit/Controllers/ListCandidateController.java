package tn.esprit.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
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
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiY2EzYmIwZGUtNmNlZi00YjA3LThlZTUtMjdhYWY1NWRhZDUyIiwidHlwZSI6ImFwaV90b2tlbiJ9.-vcKMaUNglqDquZx36OuK3il5B9aNCO1KWnJ8lbFwd8"; // Utilisez une variable d'environnement pour sécuriser la clé API

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
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = "https://api.edenai.run/v2/ocr/resume_parser";
            HttpPost postRequest = new HttpPost(url);
            postRequest.setHeader("Authorization", "Bearer " + API_KEY);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", cvFile, ContentType.APPLICATION_OCTET_STREAM, cvFile.getName());
            builder.addTextBody("language", "fr", ContentType.TEXT_PLAIN); // Langue du CV
            builder.addTextBody("providers", "affinda", ContentType.TEXT_PLAIN);// Fournisseur d'analyse

            HttpEntity multipart = builder.build();
            postRequest.setEntity(multipart);

            CloseableHttpResponse response = client.execute(postRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseString = EntityUtils.toString(response.getEntity());

            if (statusCode == 200) {
                afficherResultatsIA(responseString);
            } else {
                afficherAlerte("Erreur API", "Erreur de l'API EdenAI : " + statusCode + "\nRéponse : " + responseString);
            }

        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de l'analyse avec EdenAI : " + e.getMessage());
            logger.log(Level.SEVERE, "Erreur lors de l'analyse avec EdenAI", e);
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
            System.out.println("Réponse complète de l'API EdenAI : " + response);

            JSONObject jsonResponse = new JSONObject(response);

            // Vérification des résultats
            if (jsonResponse.has("affinda")) {
                JSONObject affinda = jsonResponse.getJSONObject("affinda");

                // Vérification si des données extraites existent
                if (affinda.has("extracted_data")) {
                    JSONObject extractedData = affinda.getJSONObject("extracted_data");
                    JSONObject personalInfos = extractedData.getJSONObject("personal_infos");
                    JSONArray skillsArray = extractedData.getJSONArray("skills");

                    StringBuilder extractedText = new StringBuilder();
                    // Extraction des informations personnelles
                    String firstName = personalInfos.optJSONObject("name").optString("first_name", "Non disponible");
                    String lastName = personalInfos.optJSONObject("name").optString("last_name", "Non disponible");
                    String fullName = firstName + " " + lastName;
                    String email = personalInfos.optJSONArray("mails").optString(0, "Non disponible");
                    String phone = personalInfos.optJSONArray("phones").optString(0, "Non disponible");

                    extractedText.append("Nom: ").append(fullName).append("\n");
                    extractedText.append("Email: ").append(email).append("\n");
                    extractedText.append("Téléphone: ").append(phone).append("\n");

                    // Extraction des compétences
                    extractedText.append("Compétences: \n");
                    for (int i = 0; i < skillsArray.length(); i++) {
                        extractedText.append("- ").append(skillsArray.getJSONObject(i).optString("name", "Non disponible")).append("\n");
                    }

                    // Affichage des résultats dans un pop-up
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Résultats de l'Analyse IA");
                    alert.setHeaderText(null);
                    alert.setContentText(extractedText.toString());
                    alert.showAndWait();
                    return;
                }
            }

            // Si aucun résultat valide n'est trouvé
            afficherAlerte("Erreur de l'API", "Aucune donnée trouvée dans la réponse.");

        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de l'affichage des résultats.");
            logger.log(Level.SEVERE, "Erreur lors du traitement de la réponse de l'API EdenAI", e);
        }
    }

}
