package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import tn.esprit.Entities.Projet;

public class DetailsProjetController {

    @FXML
    private Text projectName;
    @FXML
    private Text projectDescription;
    @FXML
    private Text projectStatus;

    public void setProjet(Projet projet) {
        projectName.setText(projet.getName());
        projectDescription.setText(projet.getDescription());
        projectStatus.setText(projet.getStatus().toString());
    }
}
