package krisapps.tripplanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import krisapps.tripplanner.data.TripUtility;
import krisapps.tripplanner.data.trip.Trip;

public class Controller {

    //<editor-fold desc="Menu panels">
    @FXML
    private TabPane tripWizard;

    @FXML
    private VBox upcomingTripsPanel;

    @FXML
    private VBox tripSetupPanel;
    //</editor-fold>

    @FXML
    private TextField tripNameBox;

    @FXML
    private TextField tripDestinationBox;

    @FXML
    private TextField tripBudgetBox;

    TripUtility triputil = new TripUtility();
    Trip currentPlan = null;

    @FXML
    public void initialize() {
        TripUtility.log("Initializing...");
        triputil.init();

        if (triputil.getTrips().isEmpty()) {
            tripWizard.setVisible(false);
            tripSetupPanel.setVisible(true);
            upcomingTripsPanel.setVisible(false);
        } else {
            tripWizard.setVisible(false);
            tripSetupPanel.setVisible(false);
            upcomingTripsPanel.setVisible(true);
        }
    }

    public void startWizard() {
        currentPlan = new Trip(
                tripNameBox.getText(),
                tripDestinationBox.getText()
        );
        if (!tripBudgetBox.getText().isEmpty()) {
            currentPlan.setBudget(Double.parseDouble(tripBudgetBox.getText()));
        } else {
            currentPlan.setBudget(0);
        }

        refreshWindowTitle("KrisApps Trip Planner - planning " + currentPlan.getTripName());
        tripSetupPanel.setVisible(false);
        tripWizard.setVisible(true);
    }

    public void refreshWindowTitle(String title) {
        if (Application.window != null) {
            Application.window.setTitle(title);
        } else {
            Platform.runLater(() -> {
                if (Application.window != null) {
                    Application.window.setTitle(title);
                }
            });
        }
    }

}