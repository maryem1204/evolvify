package tn.esprit.Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.Utils.MyDataBase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

public class PageAcceuil {
    @FXML
    private ScrollPane scrollPane; // Référence au ScrollPane
    @FXML private StackPane card1, card2, card3, card4, card5;
    @FXML private StackPane frontPane1, frontPane2, frontPane3, frontPane4, frontPane5;
    @FXML private StackPane backPane1, backPane2, backPane3, backPane4, backPane5;
    @FXML private Button postuler1, postuler2, postuler3, postuler4, postuler5;
    @FXML private StackPane cardAboutUs, cardSolutions;
    @FXML private Button login, nosoffre, nossolution, quisommesnous;
    private boolean[] isFlipped = {false, false, false, false, false};
    private Connection cnx = MyDataBase.getInstance().getCnx();

    @FXML
    private void goToAboutUs() {
        Platform.runLater(() -> {
            // Utiliser un délai pour forcer le redimensionnement avant le défilement
            Platform.runLater(() -> scrollToCard(cardAboutUs));
        });
    }
    // Défilement vers "Nos Solutions"
    @FXML
    private void goToSolutions() {
        scrollToCard(cardSolutions);
    }


    private void scrollToCard(StackPane card) {
        double scrollValue = 1600.0;
        scrollPane.setVvalue(scrollValue);

    }
    @FXML
    private void goToOffres() {
        Platform.runLater(() -> {
            scrollPane.layout(); // Mise à jour du layout avant de calculer les positions
            scrollToCardOffres(card1); // Faire défiler vers la première carte des offres

            // Défilement vers les autres cartes avec des délais
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.seconds(1), e -> scrollToCard(card2)),
                    new KeyFrame(Duration.seconds(2), e -> scrollToCard(card3)),
                    new KeyFrame(Duration.seconds(3), e -> scrollToCard(card4)),
                    new KeyFrame(Duration.seconds(4), e -> scrollToCard(card5))
            );
            timeline.play(); // Lancer la timeline
        });
    }
    private void scrollToCardOffres(StackPane card) {

        Bounds viewportBounds = scrollPane.getViewportBounds();
        Bounds cardBounds = card.localToScene(card.getBoundsInLocal());

        double scrollOffset = cardBounds.getMinY() - viewportBounds.getMinY();
        double contentHeight = scrollPane.getContent().getBoundsInLocal().getHeight();
        double scrollValue = scrollOffset / contentHeight;

        // Vérification pour s'assurer que la valeur est entre 0 et 1
        scrollValue = Math.max(0, Math.min(1, scrollValue));

        scrollPane.setVvalue(scrollValue);

    }





    @FXML
    public void initialize() {
        nossolution.setOnAction(event -> goToAboutUs());
        quisommesnous.setOnAction(event -> goToSolutions());
        // Assigner des actions aux boutons "Postuler"
        List<Button> postulerButtons = Arrays.asList(postuler1, postuler2, postuler3, postuler4, postuler5);
        for (int i = 0; i < postulerButtons.size(); i++) {
            final int index = 9;  // Associer un ID d'offre à chaque bouton
            postulerButtons.get(i).setOnAction(event -> afficherFormulaire(index + 1));
        }

        // Charger les offres dans les cartes
        for (int i = 0; i < 5; i++) {
            chargerOffre(i);  // Charger les offres en fonction de l'index (0, 1, 2, ...)
            postulerButtons.get(i).setVisible(true);


        }


        // Liste des cartes et de leurs faces avant/arrière
        List<StackPane> cards = Arrays.asList(card1, card2, card3, card4, card5);
        List<StackPane> frontPanes = Arrays.asList(frontPane1, frontPane2, frontPane3, frontPane4, frontPane5);
        List<StackPane> backPanes = Arrays.asList(backPane1, backPane2, backPane3, backPane4, backPane5);

        for (int i = 0; i < cards.size(); i++) {
            int index = i;
            StackPane card = cards.get(i);
            StackPane frontPane = frontPanes.get(i);
            StackPane backPane = backPanes.get(i);

            if (card == null || frontPane == null || backPane == null) continue;

            // Transition pour retourner la carte vers l'arrière
            RotateTransition rotateToBack = new RotateTransition(Duration.seconds(0.2), card);
            rotateToBack.setFromAngle(0);
            rotateToBack.setToAngle(180);
            rotateToBack.setAxis(new Point3D(0, 1, 0));
            rotateToBack.setOnFinished(e -> {
                frontPane.setVisible(false);
                backPane.setVisible(true);
                isFlipped[index] = true;

                // Vérification si le bouton est bien ajouté au backPane
                if (!backPane.getChildren().contains(postulerButtons.get(index))) {
                    backPane.getChildren().add(postulerButtons.get(index));
                }

                // Utiliser Platform.runLater pour forcer l'affichage du bouton
                Platform.runLater(() -> {
                    postulerButtons.get(index).setVisible(true);
                    postulerButtons.get(index).toFront();
                });
            });


            // Transition pour retourner la carte vers l'avant
            RotateTransition rotateToFront = new RotateTransition(Duration.seconds(0.5), card);
            rotateToFront.setFromAngle(180);
            rotateToFront.setToAngle(0);
            rotateToFront.setAxis(new Point3D(0, 1, 0));
            rotateToFront.setOnFinished(e -> {
                backPane.setVisible(false);
                frontPane.setVisible(true);
                isFlipped[index] = false;
                postulerButtons.get(index).setVisible(false);
            });

            // Flip au survol de chaque carte indépendamment
            card.setOnMouseEntered(e -> {
                if (!isFlipped[index]) {
                    rotateToBack.play();
                }
            });

            card.setOnMouseExited(e -> {
                if (isFlipped[index]) {
                    rotateToFront.play();
                }
            });
        }
    }

    // Méthode pour récupérer les offres depuis la base de données et les afficher dans les cartes
    private void chargerOffre(int index) {
        try (PreparedStatement stmt = cnx.prepareStatement("SELECT id_offre, description FROM offre LIMIT ?, 1")) {
            stmt.setInt(1, index);  // Charger l'offre par son index
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    String description = rs.getString("description");

                    // Créer un texte pour la description de l'offre
                    Text descriptionText = new Text(description);
                    descriptionText.setStyle("-fx-font-size: 12px; -fx-fill: gray;");

                    // Ajouter le titre et la description au backPane de la carte correspondante
                    StackPane backPane = getBackPaneByIndex(index);
                    backPane.getChildren().clear();
                    backPane.getChildren().addAll(descriptionText);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Retourner le backPane correspondant à l'index de la carte
    private StackPane getBackPaneByIndex(int index) {
        switch (index) {
            case 0: return backPane1;
            case 1: return backPane2;
            case 2: return backPane3;
            case 3: return backPane4;
            case 4: return backPane5;
            default: return null;
        }
    }

    // Méthode pour afficher le formulaire de candidature
    private void afficherFormulaire(int idOffre) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddCondidate.fxml"));
            Parent root = loader.load();
            AddCondidate controller = loader.getController();
            controller.setIdOffre(idOffre);  // Passer l'ID de l'offre au formulaire
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Formulaire de Candidature");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    /**
     * Charge dynamiquement une vue dans le `contentArea`
     */
    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Remplace la vue actuelle (exemple)
            Scene scene = login.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlelogin() {
        loadView("/fxml/loginUser.fxml");
    }
}
