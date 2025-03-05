package tn.esprit.Controllers;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ListUsersController {

    @FXML
    private TableView<Utilisateur> employeeTable;
    @FXML
    private TableColumn<Utilisateur, ImageView> colProfile;
    @FXML
    private TableColumn<Utilisateur, String> colFirstName;
    @FXML
    private TableColumn<Utilisateur, String> colLastName;
    @FXML
    private TableColumn<Utilisateur, String> colEmail;
    @FXML
    private TableColumn<Utilisateur, String> colBirthday;
    @FXML
    private TableColumn<Utilisateur, String> colJoiningDate;
    @FXML
    private TableColumn<Utilisateur, String> colRole;
    @FXML
    private TableColumn<Utilisateur, String> colNumTel;
    @FXML
    private TableColumn<Utilisateur, Integer> colCongeRestant;
    @FXML
    private TableColumn<Utilisateur, Integer> colTtRestants;
    @FXML
    private TableColumn<Utilisateur, Void> colActions;
    @FXML
    private TextField searchField;
    @FXML
    private Pagination pagination; // Ajout de la pagination
    @FXML
    private StackPane stackPane;

    @FXML
    private ComboBox<Integer> yearFilterComboBox;
    private Integer selectedYear = null;


    private ObservableList<Utilisateur> users = FXCollections.observableArrayList();
    private List<Utilisateur> filteredUsers = new ArrayList<>();

    private static final int ROWS_PER_PAGE = 10;

    private static ListUsersController instance;

    public ListUsersController() {
        instance = this;
    }

    public static ListUsersController getInstance() {
        return instance;
    }
    @FXML
    public void initialize() {
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox.setMargin(searchField, new Insets(0, 0, 20, 0)); // 20px de marge en bas

        loadUsers();
        configureTable();
        initializeYearFilter(); // Initialize the year filter

        pagination.setPageCount((int) Math.ceil((double) users.size() / ROWS_PER_PAGE));
        pagination.setCurrentPageIndex(0);
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updateTable(newIndex.intValue()));
        updateTable(0);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());

        employeeTable.setSelectionModel(null);
    }

    private void initializeYearFilter() {
        // Create a fixed set of years to display
        Set<Integer> years = new HashSet<>();
        years.add(2025);
        years.add(2024);
        years.add(2023);
        years.add(2022);

        // Also add any years from existing user data
        for (Utilisateur user : users) {
            if (user.getJoiningDate() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(user.getJoiningDate());
                years.add(calendar.get(Calendar.YEAR));
            }
        }

        // Sort years in descending order (most recent first)
        List<Integer> yearsList = new ArrayList<>(years);
        Collections.sort(yearsList, Collections.reverseOrder());

        // Create the ComboBox items with "Tous" option
        ObservableList<Integer> yearOptions = FXCollections.observableArrayList();
        yearOptions.add(null); // null represents "All years"
        yearOptions.addAll(yearsList);

        yearFilterComboBox.setItems(yearOptions);

        // Set up cell factory to display "Tous" for null value
        yearFilterComboBox.setCellFactory(lv -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Tous");
                } else {
                    setText(item.toString());
                }
            }
        });

        // Set up button cell to display "Tous" for null value
        yearFilterComboBox.setButtonCell(new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Tous");
                } else {
                    setText(item.toString());
                }
            }
        });

        // Set the default selected value to 2025
        //yearFilterComboBox.setValue(tous);
        //selectedYear = 2025;

        // Apply the filter immediately to show only 2025 employees by default
        applyFilters();
    }
    @FXML
    private void handleYearFilter() {
        selectedYear = yearFilterComboBox.getValue();
        applyFilters(); // Apply both year and search filters
    }
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();

        // Apply both filters
        filteredUsers = users.stream()
                .filter(user -> {
                    // Apply year filter if a year is selected
                    boolean matchesYear = selectedYear == null ||
                            (user.getJoiningDate() != null && getYearFromDate(user.getJoiningDate()) == selectedYear);

                    // Apply search filter if search text is not empty
                    boolean matchesSearch = searchText.isEmpty() ||
                            matchesSearchCriteria(user, searchText);

                    return matchesYear && matchesSearch;
                })
                .collect(Collectors.toList());

        // Update pagination
        pagination.setPageCount(Math.max(1, (int) Math.ceil((double) filteredUsers.size() / ROWS_PER_PAGE)));
        pagination.setCurrentPageIndex(0);
        updateTable(0);
    }
    private void loadUsers() {
        try {
            UtilisateurService userService = new UtilisateurService();
            users.clear();
            List<Utilisateur> userList = userService.showAll();
            for (Utilisateur user : userList) {
                String profilePhoto = userService.getProfilePhoto(user.getId_employe());
                user.setProfilePhoto(profilePhoto);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void configureTable() {
        colProfile.setCellValueFactory(new PropertyValueFactory<>("profilePhotoImageView"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colBirthday.setCellValueFactory(new PropertyValueFactory<>("birthdayDate"));
        colJoiningDate.setCellValueFactory(new PropertyValueFactory<>("joiningDate"));
        colRole.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(formatRole(cellData.getValue().getRole())));
        colNumTel.setCellValueFactory(new PropertyValueFactory<>("num_tel"));
        colCongeRestant.setCellValueFactory(new PropertyValueFactory<>("congeRestant"));
        colTtRestants.setCellValueFactory(new PropertyValueFactory<>("ttRestants"));

        addActionsColumn();
    }

    private void updateTable(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredUsers.size());

        System.out.println(filteredUsers.size());
        // Create a sublist for the current page
        List<Utilisateur> pageItems = filteredUsers.isEmpty() ?
                Collections.emptyList() :
                filteredUsers.subList(fromIndex, toIndex);

        employeeTable.setItems(FXCollections.observableArrayList(pageItems));
    }


    private void addActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Utilisateur, Void>() {
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/editIcon.png")));
            private final ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/deleteIcon.png")));
            private final HBox hbox = new HBox(10, editIcon, deleteIcon);

            {
                editIcon.setFitWidth(30);
                editIcon.setFitHeight(30);
                deleteIcon.setFitWidth(30);
                deleteIcon.setFitHeight(30);

                // Appliquer un curseur "pointer" aux icônes
                editIcon.setStyle("-fx-cursor: hand;");
                deleteIcon.setStyle("-fx-cursor: hand;");


                editIcon.setOnMouseClicked(event -> showEditPopup(getTableView().getItems().get(getIndex())));
                deleteIcon.setOnMouseClicked(event -> confirmDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }
    @FXML
    private void showEditPopup(Utilisateur user) {
        try {
            // Get the main stage
            Stage primaryStage = (Stage) stackPane.getScene().getWindow();
            Scene primaryScene = primaryStage.getScene();

            // Create a semi-transparent overlay
            Rectangle overlay = new Rectangle();
            overlay.setWidth(primaryScene.getWidth());
            overlay.setHeight(primaryScene.getHeight());
            overlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.4)); // Dark transparent gray

            // Ensure overlay resizes with window
            primaryScene.widthProperty().addListener((obs, oldVal, newVal) -> overlay.setWidth(newVal.doubleValue()));
            primaryScene.heightProperty().addListener((obs, oldVal, newVal) -> overlay.setHeight(newVal.doubleValue()));

            // Add overlay to the root container of the main stage
            Pane rootPane = (Pane) primaryScene.getRoot();
            rootPane.getChildren().add(overlay);

            // Load popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/editUser.fxml"));
            Parent root = loader.load();
            EditUserController controller = loader.getController();
            controller.setUserData(user);
            controller.setListUsersController(this);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));

            // Remove overlay when popup is closed
            stage.setOnHidden(e -> rootPane.getChildren().remove(overlay));

            stage.showAndWait();

            refreshUserList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void confirmDelete(Utilisateur user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cet utilisateur ?");
        alert.setContentText(user.getFirstname() + " " + user.getLastname());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                new UtilisateurService().delete(user);
                refreshUserList();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void showAddEmployeePopup() {
        try {
            // Get the main stage
            Stage primaryStage = (Stage) stackPane.getScene().getWindow();
            Scene primaryScene = primaryStage.getScene();

            // Create a semi-transparent overlay
            Rectangle overlay = new Rectangle();
            overlay.setWidth(primaryScene.getWidth());
            overlay.setHeight(primaryScene.getHeight());
            overlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.4)); // Dark transparent gray

            // Ensure overlay resizes with window
            primaryScene.widthProperty().addListener((obs, oldVal, newVal) -> overlay.setWidth(newVal.doubleValue()));
            primaryScene.heightProperty().addListener((obs, oldVal, newVal) -> overlay.setHeight(newVal.doubleValue()));

            // Add overlay to the root container of the main stage
            Pane rootPane = (Pane) primaryScene.getRoot();
            rootPane.getChildren().add(overlay);

            // Load popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajouterUser.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));

            // Remove overlay when popup is closed
            stage.setOnHidden(e -> rootPane.getChildren().remove(overlay));

            stage.showAndWait();

            refreshUserList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshUserList() {
        loadUsers();
        pagination.setPageCount((int) Math.ceil((double) users.size() / ROWS_PER_PAGE));
        updateTable(pagination.getCurrentPageIndex());
        addActionsColumn(); // Recharger les icônes d'actions
        employeeTable.refresh();
    }



    private boolean matchesSearchCriteria(Utilisateur user, String searchText) {
        // Customize this method based on your search requirements
        return user.getFirstname().toLowerCase().contains(searchText) ||
                user.getLastname().toLowerCase().contains(searchText) ||
                user.getEmail().toLowerCase().contains(searchText) ||
                user.getRole().toString().toLowerCase().contains(searchText);
    }

    private int getYearFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase();

        if (keyword.isEmpty()) {
            refreshUserList();
            pagination.setCurrentPageIndex(0); // revenir à la liste complète et à la première page de pagination
        } else {
            List<Utilisateur> filteredList = users.stream()
                    .filter(u -> (u.getFirstname() + " " + u.getLastname()).toLowerCase().contains(keyword)
                            || u.getEmail().contains(keyword)
                            || u.getRole().toString().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());

            employeeTable.setItems(FXCollections.observableArrayList(filteredList));
        }

        addActionsColumn(); // Forcer la mise à jour des icônes d'action
        employeeTable.refresh();
    }
    private String formatRole(Role role) {
        if (role == null) return "Non défini";
        switch (role) {
            case RESPONSABLE_RH: return "Responsable RH";
            case CHEF_PROJET: return "Chef de projet";
            case EMPLOYEE: return "Employée";
            case CONDIDAT: return "Candidat";
            default: return "Inconnu";
        }
    }

}
