package tn.esprit.Controllers;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.BaseColor;
import javafx.scene.shape.Rectangle;
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
    @FXML
    private TableView<Projet> projectTable;
    @FXML
    private TableColumn<Projet, String> colName;
    @FXML
    private TableColumn<Projet, String> colAbbreviation;
    @FXML
    private TableColumn<Projet, Projet.Status> colStatus;
    @FXML
    private TableColumn<Projet, Date> colEndDate;
    @FXML
    private TableColumn<Projet, Date> colStarterDate;
    @FXML
    private TableColumn<Projet, List<Integer>> colEmployeId;
    @FXML
    private TableColumn<Projet, Void> colActions;
    @FXML
    private TextField recherche;
    @FXML
    private Pagination pagination;
    @FXML
    private BorderPane rootPane;

    private ObservableList<Projet> projets = FXCollections.observableArrayList();
    private FilteredList<Projet> filteredProjet;
    private ProjetService projectService = new ProjetService();
    private Map<Integer, Utilisateur> userMap = new HashMap<>();

    // Reference to main dashboard controller for content switching
    private DashboardController dashboardController;

    // Pagination control
    private static final int ROWS_PER_PAGE = 8;

    public void initialize() throws SQLException {
        loadUsers();
        projectTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupTableColumns();
        loadProjects();

        // Initialize filteredProjet BEFORE setupPagination
        filteredProjet = new FilteredList<>(projets, p -> true);

        setupPagination();
        setupSearch();
        addActionsColumn();
        projectTable.setFixedCellSize(-1);

        // Explicitly set preferred width for the Assigné à column
        colEmployeId.setPrefWidth(280);
    }

    // Set the dashboard controller reference
    public void setDashboardController(DashboardController controller) {
        this.dashboardController = controller;
    }

    // Other methods remain the same...

    private void setupTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAbbreviation.setCellValueFactory(new PropertyValueFactory<>("abbreviation"));

        // Configuration of status column with visual badge
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

                Label statusLabel = new Label(status.toString().replace("_", " "));
                statusLabel.getStyleClass().addAll("status-label");

                // Apply specific status styles
                switch (status) {
                    case IN_PROGRESS:
                        statusLabel.getStyleClass().add("status-inprogress");
                        break;
                    case COMPLETED:
                        statusLabel.getStyleClass().add("status-completed");
                        break;
                    default:
                        statusLabel.getStyleClass().add("status-default");
                }

                setGraphic(statusLabel);
                setText(null);
            }
        });

        // Date column configurations
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("end_date"));
        colStarterDate.setCellValueFactory(new PropertyValueFactory<>("starter_at"));

        // Enhanced configuration for employee column to show users more clearly
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

                // Create a container that expands horizontally
                VBox userContainer = new VBox();
                userContainer.getStyleClass().add("users-cell");
                userContainer.setSpacing(8);
                userContainer.setMinWidth(250); // Ensure container uses available width

                // Create a flow pane for better layout when multiple users
                FlowPane usersPane = new FlowPane();
                usersPane.setHgap(10);
                usersPane.setVgap(8);
                usersPane.setPrefWrapLength(250); // Prefer wrapping after this width

                for (Integer employeId : employes) {
                    Utilisateur user = userMap.get(employeId);
                    if (user != null) {
                        // Create a more compact layout for each user
                        HBox userItem = new HBox();
                        userItem.getStyleClass().add("user-item");
                        userItem.setSpacing(5);
                        userItem.setAlignment(Pos.CENTER_LEFT);
                        userItem.setPrefHeight(20); // Fixed height for consistency

                        // Avatar with initials
                        Circle avatar = new Circle(12);
                        avatar.getStyleClass().add("user-avatar");
                        avatar.setFill(getColorForId(employeId));

                        String initials = getInitials(user.getLastname(), user.getFirstname());
                        Text initialsText = new Text(initials);
                        initialsText.setFill(Color.WHITE);

                        StackPane avatarPane = new StackPane(avatar, initialsText);
                        avatarPane.setAlignment(Pos.CENTER);

                        // User name with some styling
                        Label userName = new Label(user.getFirstname() + " " + user.getLastname());
                        userName.getStyleClass().add("user-name");

                        userItem.getChildren().addAll(avatarPane, userName);
                        usersPane.getChildren().add(userItem);
                    }
                }

                userContainer.getChildren().add(usersPane);
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

        // Make sure the column takes proper space
        colEmployeId.setMinWidth(250);
    }

    private void setupPagination() {
        // Initialize pagination
        int pageCount = (projets.size() / ROWS_PER_PAGE) + ((projets.size() % ROWS_PER_PAGE > 0) ? 1 : 0);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(5);

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            updateTableData();
        });

        updateTableData();
    }

    private void updateTableData() {
        int pageIndex = pagination.getCurrentPageIndex();
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredProjet.size());

        // Create a sublist view for the current page
        ObservableList<Projet> pageItems = FXCollections.observableArrayList();
        if (fromIndex < toIndex) {
            pageItems.addAll(filteredProjet.subList(fromIndex, toIndex));
        }

        projectTable.setItems(pageItems);
        projectTable.refresh(); // Assure que la hauteur des lignes est recalculée
    }

    private void setupSearch() {
        // Initialize filtered list
        filteredProjet = new FilteredList<>(projets, p -> true);

        // Add listener for search text changes
        recherche.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredProjet.setPredicate(projet -> {
                // If filter text is empty, display all projects
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String searchKeyword = newValue.toLowerCase();

                // Compare fields we want to search through
                String name = projet.getName() != null ? projet.getName().toLowerCase() : "";
                String abbreviation = projet.getAbbreviation() != null ? projet.getAbbreviation().toLowerCase() : "";
                String status = projet.getStatus() != null ? projet.getStatus().toString().toLowerCase() : "";

                return name.contains(searchKeyword) ||
                        abbreviation.contains(searchKeyword) ||
                        status.contains(searchKeyword);
            });

            // After filtering, update pagination
            int pageCount = (filteredProjet.size() / ROWS_PER_PAGE) + ((filteredProjet.size() % ROWS_PER_PAGE > 0) ? 1 : 0);
            pagination.setPageCount(Math.max(pageCount, 1));
            pagination.setCurrentPageIndex(0);

            // Update table data
            updateTableData();
        });
    }

    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Projet, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                // Create a container for action icons
                HBox actionsContainer = new HBox(15);
                actionsContainer.setAlignment(Pos.CENTER);

                // Create action icons with enhanced styling
                ImageView editIcon = createSafeImageView("/images/editIcon.png");
                ImageView deleteIcon = createSafeImageView("/images/deleteIconn.png");
                ImageView tasksIcon = createSafeImageView("/images/tasks.png");

                // Configure action icons with proper styling
                if (editIcon != null) {
                    configureActionIcon(editIcon, "action-icon action-icon-edit");
                    editIcon.setOnMouseClicked(event -> {
                        Projet projet = getTableView().getItems().get(getIndex());
                        showEditPopup(projet);
                    });

                    Tooltip editTooltip = new Tooltip("Modifier");
                    Tooltip.install(editIcon, editTooltip);
                }

                if (deleteIcon != null) {
                    configureActionIcon(deleteIcon, "action-icon action-icon-delete");
                    deleteIcon.setOnMouseClicked(event -> {
                        Projet projet = getTableView().getItems().get(getIndex());
                        deleteProjet(projet);
                    });

                    Tooltip deleteTooltip = new Tooltip("Supprimer");
                    Tooltip.install(deleteIcon, deleteTooltip);
                }

                if (tasksIcon != null) {
                    configureActionIcon(tasksIcon, "action-icon action-icon-tasks");
                    tasksIcon.setOnMouseClicked(event -> {
                        Projet projet = getTableView().getItems().get(getIndex());
                        showTasksPopup(projet);  // Added method call to show tasks popup
                    });

                    Tooltip tasksTooltip = new Tooltip("Gérer les tâches");
                    Tooltip.install(tasksIcon, tasksTooltip);
                } else {
                    // Fallback button if image can't be loaded
                    Button tasksBtn = new Button("Tâches");
                    tasksBtn.getStyleClass().addAll("gradient-button", "small-button");
                    tasksBtn.setOnAction(event -> {
                        Projet projet = getTableView().getItems().get(getIndex());
                        showTasksPopup(projet);  // Added method call to show tasks popup
                    });
                    actionsContainer.getChildren().add(tasksBtn);
                }

                actionsContainer.getChildren().addAll(
                        editIcon != null ? editIcon : new Label(),
                        deleteIcon != null ? deleteIcon : new Label(),
                        tasksIcon != null && actionsContainer.getChildren().isEmpty() ? tasksIcon : new Label()
                );

                setGraphic(actionsContainer);
            }

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

            private void configureActionIcon(ImageView icon, String styleClass) {
                icon.setFitWidth(24);
                icon.setFitHeight(24);
                icon.getStyleClass().addAll(styleClass.split(" "));
            }
        });
    }

    // New method to show tasks popup
    private void showTasksPopup(Projet projet) {
        try {
            // Get main stage
            Stage primaryStage = (Stage) projectTable.getScene().getWindow();
            Scene primaryScene = primaryStage.getScene();

            // Create semi-transparent overlay
            Rectangle overlay = new Rectangle();
            overlay.setWidth(primaryScene.getWidth());
            overlay.setHeight(primaryScene.getHeight());
            overlay.setFill(Color.rgb(0, 0, 0, 0.4));

            // Ensure overlay resizes with window
            primaryScene.widthProperty().addListener((obs, oldVal, newVal) -> overlay.setWidth(newVal.doubleValue()));
            primaryScene.heightProperty().addListener((obs, oldVal, newVal) -> overlay.setHeight(newVal.doubleValue()));

            // Add overlay to the root container
            Pane rootContainer = (Pane) primaryScene.getRoot();
            rootContainer.getChildren().add(overlay);

            // Load ListeTacheRH.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListTacheRH.fxml"));
            Parent root = loader.load();

            // If there's a controller for ListeTacheRH.fxml that needs the project data
            try {
                tacheListController controller = loader.getController();
                controller.setProjet(projet);
            } catch (Exception e) {
                System.err.println("Warning: Could not set project data on ListeTacheRHController: " + e.getMessage());
                // Continue without setting project data if controller doesn't have the method
            }

            // Create undecorated stage for popup
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Styles/styles.css").toExternalForm());
            stage.setScene(scene);

            // Remove overlay when popup is closed
            stage.setOnHidden(e -> rootContainer.getChildren().remove(overlay));

            stage.showAndWait();

            // Optionally refresh data after task management
            // refreshProjetList();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre de gestion des tâches: " + e.getMessage());
        }
    }

    private void deleteProjet(Projet projet) {
        try {
            // Load FXML for delete confirmation
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DeleteProjet.fxml"));
            Parent root = loader.load();

            // Get controller and pass project data
            DeleteProjetController controller = loader.getController();
            controller.setProjet(projet);
            controller.setProjectListController(this);

            // Create modal dialog
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Confirmation de suppression");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Styles/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
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

        // Apply dialog styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Styles/styles.css").toExternalForm());

        alert.showAndWait();
    }

    private void showEditPopup(Projet projet) {
        try {
            // Get main stage
            Stage primaryStage = (Stage) projectTable.getScene().getWindow();
            Scene primaryScene = primaryStage.getScene();

            // Create semi-transparent overlay
            Rectangle overlay = new Rectangle();
            overlay.setWidth(primaryScene.getWidth());
            overlay.setHeight(primaryScene.getHeight());
            overlay.setFill(Color.rgb(0, 0, 0, 0.4));

            // Ensure overlay resizes with window
            primaryScene.widthProperty().addListener((obs, oldVal, newVal) -> overlay.setWidth(newVal.doubleValue()));
            primaryScene.heightProperty().addListener((obs, oldVal, newVal) -> overlay.setHeight(newVal.doubleValue()));

            // Add overlay to the root container
            Pane rootContainer = (Pane) primaryScene.getRoot();
            rootContainer.getChildren().add(overlay);

            // Load popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierProjet.fxml"));
            Parent root = loader.load();
            ModifierProjetController controller = loader.getController();
            controller.setUserData(projet);

            // Create undecorated stage for popup
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Styles/styles.css").toExternalForm());
            stage.setScene(scene);

            // Remove overlay when popup is closed
            stage.setOnHidden(e -> rootContainer.getChildren().remove(overlay));

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
            // Get main stage
            Stage primaryStage = (Stage) projectTable.getScene().getWindow();
            Scene primaryScene = primaryStage.getScene();

            // Create semi-transparent overlay
            Rectangle overlay = new Rectangle();
            overlay.setWidth(primaryScene.getWidth());
            overlay.setHeight(primaryScene.getHeight());
            overlay.setFill(Color.rgb(0, 0, 0, 0.4));

            // Ensure overlay resizes with window
            primaryScene.widthProperty().addListener((obs, oldVal, newVal) -> overlay.setWidth(newVal.doubleValue()));
            primaryScene.heightProperty().addListener((obs, oldVal, newVal) -> overlay.setHeight(newVal.doubleValue()));

            // Add overlay to the root container
            Pane rootContainer = (Pane) primaryScene.getRoot();
            rootContainer.getChildren().add(overlay);

            // Load popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjoutProjet.fxml"));
            Parent root = loader.load();

            // Create undecorated modal stage
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.setTitle("Ajouter un projet");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/Styles/styles.css").toExternalForm());
            popupStage.setScene(scene);

            // Remove overlay when popup is closed
            popupStage.setOnHidden(e -> rootContainer.getChildren().remove(overlay));

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
            // Load all projects
            projets.setAll(projectService.showAll());

            // Update filtered list
            if (filteredProjet != null) {
                // This will cause the predicate to be re-evaluated
                filteredProjet.setPredicate(filteredProjet.getPredicate());

                // Update pagination
                int pageCount = (filteredProjet.size() / ROWS_PER_PAGE) + ((filteredProjet.size() % ROWS_PER_PAGE > 0) ? 1 : 0);
                pagination.setPageCount(Math.max(pageCount, 1));

                // Update table data for current page
                updateTableData();
            } else {
                // First time setup
                filteredProjet = new FilteredList<>(projets, p -> true);
                setupPagination();
            }
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