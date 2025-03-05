package tn.esprit.Controllers;

import com.jfoenix.controls.JFXDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class DialogController {
    private JFXDialog dialog;

    @FXML
    private Button closeButton;  // Ensure this matches the fx:id in FXML

    public void setDialog(JFXDialog dialog) {
        this.dialog = dialog;
    }

    @FXML
    private void closeDialog() {
        if (dialog != null) {
            dialog.close();
        } else if (closeButton != null && closeButton.getScene() != null) {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        }
    }
}
