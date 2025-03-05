package tn.esprit.Utils;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A utility class to handle absence status updates between controllers.
 * Implements an observer pattern to notify the HR interface when an employee
 * updates their presence status.
 */
public class AbsenceUpdateSystem {

    // Singleton instance
    private static AbsenceUpdateSystem instance;

    // List of observers (callbacks) to notify when an absence status changes
    private final List<Consumer<Void>> observers = new ArrayList<>();

    // Private constructor for singleton pattern
    private AbsenceUpdateSystem() {}

    /**
     * Get the singleton instance of AbsenceUpdateSystem
     * @return the instance
     */
    public static synchronized AbsenceUpdateSystem getInstance() {
        if (instance == null) {
            instance = new AbsenceUpdateSystem();
        }
        return instance;
    }

    /**
     * Register an observer to be notified when an absence status changes
     * @param observer the callback to invoke when an update occurs
     */
    public void addObserver(Consumer<Void> observer) {
        observers.add(observer);
    }

    /**
     * Remove an observer from the notification list
     * @param observer the callback to remove
     */
    public void removeObserver(Consumer<Void> observer) {
        observers.remove(observer);
    }

    /**
     * Notify all observers about an absence status change
     * This will run on the JavaFX application thread
     */
    public void notifyAbsenceUpdate() {
        Platform.runLater(() -> {
            for (Consumer<Void> observer : observers) {
                observer.accept(null);
            }
        });
    }
}