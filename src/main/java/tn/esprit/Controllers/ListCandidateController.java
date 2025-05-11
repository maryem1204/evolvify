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
        CVcolum.setCellFactory(param -> new TableCell<>() {
            private final Button downloadButton = new Button("Télécharger");
            {
                downloadButton.setOnAction(event -> {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    if (utilisateur == null || utilisateur.getUploadedCv() == null || utilisateur.getUploadedCv().isEmpty()) {
                        afficherAlerte("Alerte", "Aucun CV disponible pour ce candidat !");
                        return;
                    }

                    File cvFile = getFileFromPath(utilisateur.getUploadedCv());
                    if (cvFile != null) {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Enregistrer le CV");
                        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
                        File fileToSave = fileChooser.showSaveDialog(downloadButton.getScene().getWindow());

                        if (fileToSave != null) {
                            try {
                                Files.copy(cvFile.toPath(), fileToSave.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                analyzeCVWithEdenAI(cvFile);
                            } catch (IOException e) {
                                afficherAlerte("Erreur", "Erreur lors de l'enregistrement du CV.");
                            }
                        }
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

    private File getFileFromPath(String cvPath) {
        File file = new File(cvPath);
        if (file.exists()) return file;
        afficherAlerte("Erreur", "Le fichier CV n'existe pas : " + cvPath);
        return null;
    }



    // Analyse du CV avec EdenAI
    private void analyzeCVWithEdenAI(File cvFile) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = "https://api.edenai.run/v2/ocr/resume_parser";
            HttpPost postRequest = new HttpPost(url);
            postRequest.setHeader("Authorization", "Bearer " + API_KEY);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", cvFile, ContentType.APPLICATION_OCTET_STREAM, cvFile.getName());
            builder.addTextBody("language", "fr", ContentType.TEXT_PLAIN);
            builder.addTextBody("providers", "affinda", ContentType.TEXT_PLAIN);

            postRequest.setEntity(builder.build());

            CloseableHttpResponse response = client.execute(postRequest);
            String responseString = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() == 200) {
                afficherResultatsIA(responseString);
            } else {
                afficherAlerte("Erreur API", "Réponse : " + responseString);
            }

        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de l'analyse du CV.");
        }
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

                    // Extraction des informations personnelles
                    String firstName = personalInfos.optJSONObject("name").optString("first_name", "Non disponible");
                    String lastName = personalInfos.optJSONObject("name").optString("last_name", "Non disponible");
                    String fullName = firstName + " " + lastName;
                    String email = personalInfos.optJSONArray("mails").optString(0, "Non disponible");
                    String phone = personalInfos.optJSONArray("phones").optString(0, "Non disponible");

                    // Organisation des compétences par catégories
                    StringBuilder languages = new StringBuilder();
                    StringBuilder frameworks = new StringBuilder();
                    StringBuilder databases = new StringBuilder();
                    StringBuilder tools = new StringBuilder();

                    for (int i = 0; i < skillsArray.length(); i++) {
                        String skill = skillsArray.getJSONObject(i).optString("name", "Non disponible");

                        // Catégorisation des compétences (à adapter selon ton besoin)
                        if (skill.matches("(?i).*java.*|.*python.*|.*javascript.*|.*c\\+\\+.*")) {
                            languages.append(skill).append(", ");
                        } else if (skill.matches("(?i).*spring.*|.*angular.*|.*javafx.*|.*django.*")) {
                            frameworks.append(skill).append(", ");
                        } else if (skill.matches("(?i).*mysql.*|.*postgresql.*|.*mongodb.*|.*oracle.*")) {
                            databases.append(skill).append(", ");
                        } else if (skill.matches("(?i).*git.*|.*docker.*|.*kubernetes.*|.*jenkins.*")) {
                            tools.append(skill).append(", ");
                        }
                    }

                    // Suppression de la dernière virgule
                    String langList = languages.length() > 0 ? languages.substring(0, languages.length() - 2) : "Aucune";
                    String frameList = frameworks.length() > 0 ? frameworks.substring(0, frameworks.length() - 2) : "Aucune";
                    String dbList = databases.length() > 0 ? databases.substring(0, databases.length() - 2) : "Aucune";
                    String toolList = tools.length() > 0 ? tools.substring(0, tools.length() - 2) : "Aucune";

                    // Création d'un dialogue personnalisé
                    Dialog<ButtonType> dialog = new Dialog<>();
                    dialog.setTitle("Analyse du CV");
                    dialog.setHeaderText("Résultats de l'Analyse IA");

                    // Contenu de la boîte de dialogue
                    DialogPane dialogPane = dialog.getDialogPane();
                    dialogPane.getButtonTypes().add(ButtonType.OK);

                    // Création du contenu structuré
                    StringBuilder extractedText = new StringBuilder();
                    extractedText.append("📌 **Nom:** ").append(fullName).append("\n");
                    extractedText.append("📧 **Email:** ").append(email).append("\n");
                    extractedText.append("📞 **Téléphone:** ").append(phone).append("\n\n");

                    extractedText.append("💼 **Compétences :**\n");
                    extractedText.append("● **Langages** : ").append(langList).append("\n");
                    extractedText.append("● **Frameworks** : ").append(frameList).append("\n");
                    extractedText.append("● **Bases de données** : ").append(dbList).append("\n");
                    extractedText.append("● **Outils** : ").append(toolList).append("\n");

                    Label label = new Label(extractedText.toString());
                    label.setWrapText(true);

                    dialogPane.setContent(label);
                    dialog.showAndWait();
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
