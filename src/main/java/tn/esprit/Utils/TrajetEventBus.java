package tn.esprit.Utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import tn.esprit.Entities.Trajet;

public class TrajetEventBus {
    private static final ObjectProperty<Trajet> trajetEvent = new SimpleObjectProperty<>();

    public static void fireTrajetEvent(Trajet trajet) {
        trajetEvent.set(trajet);
    }

    public static ObjectProperty<Trajet> trajetEventProperty() {
        return trajetEvent;
    }
}
