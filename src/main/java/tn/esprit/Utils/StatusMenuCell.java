package tn.esprit.Utils;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import tn.esprit.Entities.Absence;
import tn.esprit.Entities.StatutAbsence;

public class StatusMenuCell extends TableCell<Absence, StatutAbsence> {
    private final MenuButton menuButton = new MenuButton();

    public StatusMenuCell() {
        for (StatutAbsence s : StatutAbsence.values()) {
            MenuItem item = new MenuItem(getDisplayText(s));
            item.setOnAction(e -> {
                Absence absence = getTableView().getItems().get(getIndex());
                absence.setStatus(s); // Modifier directement le statut
                updateItem(s, false);
            });
            menuButton.getItems().add(item);
        }
    }

    @Override
    protected void updateItem(StatutAbsence status, boolean empty) {
        super.updateItem(status, empty);
        if (empty || status == null) {
            setGraphic(null);
        } else {
            menuButton.setText(getDisplayText(status));
            setGraphic(menuButton);
        }
    }

    private String getDisplayText(StatutAbsence status) {
        switch (status) {
            case PRESENT:
                return "Présent";
            case ABSENT:
                return "Absent";
            case EN_CONGE:
                return "En Congé";
            default:
                return status.toString();
        }
    }
}
