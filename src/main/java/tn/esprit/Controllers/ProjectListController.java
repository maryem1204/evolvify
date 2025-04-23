package tn.esprit.Controllers;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.BaseColor;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.Entities.Projet;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.ProjetService;
import tn.esprit.Services.UtilisateurService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectListController {
    @FXML private TableView<Projet> projectTable;
    @FXML private TableColumn<Projet, String> colName;
    @FXML private TableColumn<Projet, String> colAbbreviation;
    @FXML private TableColumn<Projet, Projet.Status> colStatus;
    @FXML private TableColumn<Projet, Date> colEndDate;
    @FXML private TableColumn<Projet, Date> colStarterDate;
    @FXML private TableColumn<Projet, List<Integer>> colEmployeId;
    @FXML private TableColumn<Projet, Void> colActions;
    @FXML private TextField recherche;

    private ObservableList<Projet> projets = FXCollections.observableArrayList();
    private ProjetService projectService = new ProjetService();
    private Map<Integer, Utilisateur> userMap = new HashMap<>();

    public void initialize() throws SQLException {
        loadUsers();
        projectTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        loadProjects();
        addActionsColumn();
        // Appliquer des styles CSS personnalisés
        applyCustomStyles();
        recherche.textProperty().addListener((observable, oldValue, newValue) -> filterProjets(newValue));
    }

    private void applyCustomStyles() {
        projectTable.getStyleClass().add("table-view");

        // Appliquer des styles aux boutons
        recherche.getStyleClass().add("searchInput");
        projectTable.getStyleClass().add("no-empty-rows");
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAbbreviation.setCellValueFactory(new PropertyValueFactory<>("abbreviation"));

        // Configuration de la colonne de statut avec un badge visuel
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<Projet, Projet.Status>() {
            @Override
            protected void updateItem(Projet.Status status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label statusLabel = new Label(status.toString());
                statusLabel.getStyleClass().addAll("status-label"); // Base status label style

                // Apply specific status styles based on the status
                switch (status) {
                    case IN_PROGRESS:
                        statusLabel.getStyleClass().add("status-inprogress");
                        break;
                    case COMPLETED:
                        statusLabel.getStyleClass().add("status-completed");
                        break;
                    // Add more cases for other statuses if needed
                    default:
                        // Default style if no specific style is defined
                        statusLabel.getStyleClass().add("status-default");
                }

                setGraphic(statusLabel);
                setText(null);
            }
        });

        colEndDate.setCellValueFactory(new PropertyValueFactory<>("end_date"));
        colStarterDate.setCellValueFactory(new PropertyValueFactory<>("starter_at"));

        // Configuration de la colonne des employés pour afficher les utilisateurs l'un sous l'autre
        colEmployeId.setCellValueFactory(new PropertyValueFactory<>("employes"));
        colEmployeId.setCellFactory(col -> new TableCell<Projet, List<Integer>>() {
            @Override
            protected void updateItem(List<Integer> employes, boolean empty) {
                super.updateItem(employes, empty);

                if (empty || employes == null || employes.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                VBox userContainer = new VBox();
                userContainer.getStyleClass().add("users-cell");
                userContainer.setSpacing(5);

                for (Integer employeId : employes) {
                    Utilisateur user = userMap.get(employeId);
                    if (user != null) {
                        HBox userItem = new HBox();
                        userItem.getStyleClass().add("user-item");
                        userItem.setSpacing(5);

                        // Créer l'avatar avec les initiales
                        Circle avatar = new Circle(12.5);
                        avatar.getStyleClass().add("user-avatar");
                        avatar.setFill(getColorForId(employeId));

                        String initials = getInitials(user.getLastname(), user.getFirstname());
                        Text initialsText = new Text(initials);
                        initialsText.setFill(Color.WHITE);
                        //initialsText.setFont(new Font("System", 10.0)); // Sans le FontWeight

                        StackPane avatarPane = new StackPane(avatar, initialsText);
                        avatarPane.setAlignment(Pos.CENTER);

                        // Créer le label pour le nom d'utilisateur
                        Label userName = new Label(user.getFirstname() + " " + user.getLastname());
                        userName.getStyleClass().add("user-name");

                        userItem.getChildren().addAll(avatarPane, userName);
                        userContainer.getChildren().add(userItem);
                    }
                }

                setGraphic(userContainer);
            }

            private Color getColorForId(int id) {
                String[] colors = {
                        "#1E88E5", "#43A047", "#E53935", "#8E24AA",
                        "#FB8C00", "#00ACC1", "#3949AB", "#7CB342"
                };
                return Color.web(colors[Math.abs(id) % colors.length]);
            }

            private String getInitials(String lastName, String firstName) {
                return (firstName.isEmpty() ? "" : firstName.substring(0, 1)) +
                        (lastName.isEmpty() ? "" : lastName.substring(0, 1));
            }
        });
    }

    private ObservableList<Projet> filteredProjet = FXCollections.observableArrayList();

    private void filterProjets(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            projectTable.setItems(projets);
            return;
        }

        String searchKeyword = keyword.toLowerCase();

        List<Projet> filteredList = projets.stream()
                .filter(projet -> {
                    String name = projet.getName() != null ? projet.getName().toLowerCase() : "";
                    String abbreviation = projet.getAbbreviation() != null ? projet.getAbbreviation().toLowerCase() : "";
                    String status = projet.getStatus() != null ? projet.getStatus().toString().toLowerCase() : "";

                    return name.contains(searchKeyword) ||
                            abbreviation.contains(searchKeyword) ||
                            status.contains(searchKeyword);
                })
                .collect(Collectors.toList());

        filteredProjet.setAll(filteredList);
        projectTable.setItems(filteredProjet);
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Projet, Void>() {
            // Create a safer way to load images
            private ImageView createSafeImageView(String path) {
                try {
                    InputStream stream = getClass().getResourceAsStream(path);
                    if (stream != null) {
                        return new ImageView(new Image(stream));
                    } else {
                        System.err.println("Resource not found: " + path);
                        return null;
                    }
                } catch (Exception e) {
                    System.err.println("Error loading " + path + ": " + e.getMessage());
                    return null;
                }
            }

            private final ImageView editIcon = createSafeImageView("/images/editIcon.png");
            private final ImageView deleteIcon = createSafeImageView("/images/deleteIconn.png");
            private final ImageView tasksIcon = createSafeImageView("/images/tasks.png");
            private final HBox hbox = new HBox(10);

            {
                // Configure icons with proper null checks
                configureIcon(editIcon, 20, 20);
                configureIcon(deleteIcon, 20, 20);

                // If tasks image fails to load, create a button as fallback
                if (tasksIcon != null) {
                    configureIcon(tasksIcon, 20, 20);
                    hbox.getChildren().add(tasksIcon);
                } else {
                    Button tasksBtn = new Button("Tasks");
                    tasksBtn.getStyleClass().add("task-button");
                    tasksBtn.setStyle("-fx-background-color: #0052cc; -fx-text-fill: white; -fx-cursor: hand;");
                    hbox.getChildren().add(tasksBtn);
                }

                hbox.setAlignment(Pos.CENTER);
            }

            private void configureIcon(ImageView icon, double width, double height) {
                if (icon != null) {
                    icon.setFitWidth(width);
                    icon.setFitHeight(height);
                    icon.setStyle("-fx-cursor: hand;");
                    hbox.getChildren().add(icon);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Configure click handlers
                    if (editIcon != null) {
                        editIcon.setOnMouseClicked(event -> {
                            Projet projet = getTableView().getItems().get(getIndex());
                            showEditPopup(projet);
                        });
                    }

                    if (deleteIcon != null) {
                        deleteIcon.setOnMouseClicked(event -> {
                            Projet projet = getTableView().getItems().get(getIndex());
                            deleteProjet(projet);
                        });
                    }

                    // Gérer le clic sur l'icône ou le bouton des tâches
                    Node tasksNode = hbox.getChildren().get(hbox.getChildren().size() - 1);
                    if (tasksNode instanceof ImageView) {
                        ((ImageView) tasksNode).setOnMouseClicked(event -> {
                            Projet projetSelectionne = getTableView().getItems().get(getIndex());
                            openTasksList(projetSelectionne);
                        });
                    } else if (tasksNode instanceof Button) {
                        ((Button) tasksNode).setOnAction(event -> {
                            Projet projetSelectionne = getTableView().getItems().get(getIndex());
                            openTasksList(projetSelectionne);
                        });
                    }

                    setGraphic(hbox);
                }
            }
        });
    }

    private void openTasksList(Projet projetSelectionne) {
        try {
            System.out.println("Opening tasks for project: " + projetSelectionne.getName()
                    + " (ID: " + projetSelectionne.getId_projet() + ")");

            // Change this line to use the correct filename
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListTacheRH.fxml"));
            Parent root = loader.load();
            tacheListController controller = loader.getController();

            // Pass the selected project to the task controller
            controller.setProjet(projetSelectionne);

            Stage stage = new Stage();
            stage.setTitle("Liste des tâches pour " + projetSelectionne.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la liste des tâches");
        }
    }


    private void deleteProjet(Projet projet) {
        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DeleteProjet.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et passer le projet sélectionné
            DeleteProjetController controller = loader.getController();
            controller.setProjet(projet);
            controller.setProjectListController(this); // Référence au contrôleur parent

            // Créer une nouvelle fenêtre pour la confirmation
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquer l'interaction avec la fenêtre principale
            stage.setTitle("Confirmation de suppression");
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Attendre la fermeture de la fenêtre avant de continuer
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la boîte de dialogue de suppression.");
        }
    }


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showEditPopup(Projet projet) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierProjet.fxml"));
            Parent root = loader.load();
            ModifierProjetController controller = loader.getController();
            controller.setUserData(projet);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshProjetList();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    private void loadUsers() throws SQLException {
        UtilisateurService utilisateurService = new UtilisateurService();
        List<Utilisateur> users = utilisateurService.showAll();
        userMap.clear();
        for (Utilisateur user : users) {
            userMap.put(user.getId_employe(), user);
        }
    }

    @FXML
    private void openAjoutProjetPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjoutProjet.fxml"));
            Parent root = loader.load();
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setTitle("Ajouter un projet");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
            refreshProjetList();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage());
        }
    }

    public void refreshProjetList() {
        try {
            projets.setAll(projectService.showAll());
            projectTable.setItems(projets);

            // Réappliquer la configuration des colonnes et des actions
            setupTableColumns();
            addActionsColumn();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Erreur lors du chargement des projets: " + e.getMessage());
        }
    }

    @FXML
    public void exportTableViewToPDF() {
        try {
            // FileChooser for saving the PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer en PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Logo
                String logoPath = getClass().getResource("/images/logo.png").getPath();
                com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoPath);
                logo.scaleToFit(100, 100);
                logo.setAlignment(Element.ALIGN_LEFT);
                document.add(logo);

                // Titre du document
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(0, 71, 171));
                Paragraph title = new Paragraph("Liste des Projets", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(Chunk.NEWLINE);

                // Date d'exportation
                Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
                Paragraph dateParagraph = new Paragraph("Exporté le: " + new java.util.Date(), dateFont);
                dateParagraph.setAlignment(Element.ALIGN_RIGHT);
                document.add(dateParagraph);
                document.add(Chunk.NEWLINE);

                // Nombre de colonnes visibles (excluant "Actions")
                long visibleColumnCount = projectTable.getColumns().stream()
                        .filter(column -> !column.getText().equalsIgnoreCase("Actions"))
                        .count();

                // Création du tableau PDF
                PdfPTable pdfTable = new PdfPTable((int) visibleColumnCount);
                pdfTable.setWidthPercentage(100);

                // Style pour les en-têtes
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
                BaseColor headerBackground = BaseColor.LIGHT_GRAY;

                // Ajout des en-têtes
                for (TableColumn<Projet, ?> column : projectTable.getColumns()) {
                    if (!column.getText().equalsIgnoreCase("Actions")) {
                        PdfPCell header = new PdfPCell(new Phrase(column.getText(), headerFont));
                        header.setBackgroundColor(headerBackground);
                        header.setPadding(8);
                        header.setHorizontalAlignment(Element.ALIGN_CENTER);
                        pdfTable.addCell(header);
                    }
                }

                // Style pour les données
                Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);
                BaseColor evenRowColor = BaseColor.CYAN;
                BaseColor oddRowColor = BaseColor.WHITE;

                // Ajout des données
                ObservableList<Projet> items = projectTable.getItems();
                int rowNum = 0;
                for (Projet item : items) {
                    BaseColor rowColor = (rowNum % 2 == 0) ? evenRowColor : oddRowColor;
                    rowNum++;

                    for (TableColumn<Projet, ?> column : projectTable.getColumns()) {
                        if (!column.getText().equalsIgnoreCase("Actions")) {
                            PdfPCell cell = new PdfPCell();

                            // Traitement spécial pour la colonne "assigné à"
                            if (column.getText().equalsIgnoreCase("assigné à")) {
                                List<Integer> employes = item.getEmployes();
                                if (employes != null && !employes.isEmpty()) {
                                    StringBuilder employesList = new StringBuilder();
                                    for (Integer employeId : employes) {
                                        Utilisateur user = userMap.get(employeId);
                                        if (user != null) {
                                            if (employesList.length() > 0) {
                                                employesList.append("\n");
                                            }
                                            employesList.append(user.getFirstname()).append(" ").append(user.getLastname());
                                        }
                                    }
                                    cell = new PdfPCell(new Phrase(employesList.toString(), cellFont));
                                } else {
                                    cell = new PdfPCell(new Phrase("", cellFont));
                                }
                            } else {
                                // Autres colonnes
                                String cellValue = column.getCellObservableValue(item) != null ?
                                        column.getCellObservableValue(item).getValue().toString() : "";
                                cell = new PdfPCell(new Phrase(cellValue, cellFont));
                            }

                            cell.setBackgroundColor(rowColor);
                            cell.setPadding(5);
                            pdfTable.addCell(cell);
                        }
                    }
                }

                document.add(pdfTable);

                // Pied de page
                Paragraph footer = new Paragraph("Document généré automatiquement", new Font(Font.FontFamily.HELVETICA, 8));
                footer.setAlignment(Element.ALIGN_CENTER);
                document.add(Chunk.NEWLINE);
                document.add(footer);

                document.close();

                showAlert(Alert.AlertType.INFORMATION, "PDF Créé",
                        "Le document PDF a été créé avec succès !");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur PDF",
                    "Une erreur est survenue lors de la création du PDF: " + e.getMessage());
        }
    }
    private void loadProjects() throws SQLException {
        // Clear existing projects
        projets.clear();

        // Fetch all projects from the project service
        projets.addAll(projectService.showAll());

        // Set the projects in the table view
        projectTable.setItems(projets);
    }
}