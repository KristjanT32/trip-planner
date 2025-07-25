package krisapps.tripplanner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import krisapps.tripplanner.data.TripUtility;
import krisapps.tripplanner.data.trip.Trip;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Controller {

    //<editor-fold desc="Menu panels">
    @FXML
    private TabPane tripWizard;

    @FXML
    private VBox upcomingTripsPanel;

    @FXML
    private VBox tripSetupPanel;
    //</editor-fold>

    //<editor-fold desc="New trip setup">
    @FXML
    private TextField tripNameBox;

    @FXML
    private TextField tripDestinationBox;

    @FXML
    private TextField tripBudgetBox;
    //</editor-fold>

    //<editor-fold desc="Trip details menu">
    @FXML
    private DatePicker tripStartBox;

    @FXML
    private DatePicker tripEndBox;

    @FXML
    private Spinner<Integer> tripPartySizeBox;
    //</editor-fold>

    //<editor-fold desc="Itinerary">
    @FXML
    private Spinner<Integer> activityDayBox;

    @FXML
    private TextField activityDescriptionBox;
    //</editor-fold>

    //<editor-fold desc="Trip overview">
    @FXML
    private Label tripDatesLabel;

    @FXML
    private Label peopleInvolvedLabel;
    //</editor-fold>

    public static TripUtility trips = new TripUtility();
    Trip currentPlan = null;
    public static boolean launchedInReadOnly = false;

    @FXML
    public void initialize() {
        TripUtility.log("Initializing...");
        trips.init();

        if (trips.getTrips().isEmpty()) {
            tripWizard.setVisible(false);
            tripSetupPanel.setVisible(true);
            upcomingTripsPanel.setVisible(false);
        } else {
            tripWizard.setVisible(false);
            tripSetupPanel.setVisible(false);
            upcomingTripsPanel.setVisible(true);
        }
        registerListeners();
        setupSpinners();
    }

    public void setupSpinners() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Short.MAX_VALUE, 1);
        tripPartySizeBox.setValueFactory(valueFactory);
    }

    public void registerListeners() {
        // Setup menu
        tripStartBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (currentPlan == null) return;
            if (launchedInReadOnly) return;
            currentPlan.setTripStartDate(newValue.atStartOfDay());
        });
        tripEndBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (currentPlan == null) return;
            if (launchedInReadOnly) return;
            currentPlan.setTripEndDate(newValue.atStartOfDay());
        });
        tripPartySizeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (currentPlan == null) return;
            if (launchedInReadOnly) return;
            currentPlan.setPartySize(newValue.shortValue());
        });

        refreshItinerary();
    }

    public void refreshItinerary() {
        if (currentPlan == null) return;
        if (currentPlan.tripDatesSupplied()) {
            long tripDurationInDays = Duration.between(currentPlan.getTripStartDate(), currentPlan.getTripEndDate()).toDays();
            activityDayBox.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, (int) tripDurationInDays, 1));
        } else {
            activityDayBox.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Short.MAX_VALUE, 1));
        }
    }

    public void refreshExpensePlanner() {

    }

    public void refreshTripOverview() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        long tripDuration = Duration.between(currentPlan.getTripStartDate(), currentPlan.getTripEndDate()).toDays();
        tripDatesLabel.setText(
                formatter.format(currentPlan.getTripStartDate().toLocalDate()) + " - " + formatter.format(currentPlan.getTripEndDate().toLocalDate())
                + " (duration: " + tripDuration + (tripDuration == 1 ? " day" : " days") + ")"
        );
        peopleInvolvedLabel.setText(String.valueOf(currentPlan.getPartySize()));
    }

    public void showTripSetup() {
        if (currentPlan != null) {
            Alert prompt = new Alert(Alert.AlertType.CONFIRMATION);
            prompt.setHeaderText(null);
            prompt.setTitle("Trip planning in progress!");
            prompt.setContentText("A trip is currently open in Trip Planner. Do you want to discard this open plan, and start a new one?");
            prompt.getButtonTypes().clear();
            prompt.getButtonTypes().addAll(new ButtonType("Yes, discard current plan", ButtonBar.ButtonData.APPLY), ButtonType.CANCEL);
            Optional<ButtonType> response = prompt.showAndWait();
            if (response.isPresent()) {
                if (response.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                    TripUtility.log("Discarded current trip plan.");
                    resetPlanner();
                    showTripSetup();
                }
            }

        } else {
            tripWizard.setVisible(false);
            tripSetupPanel.setVisible(true);
            upcomingTripsPanel.setVisible(false);
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

    public void resetPlanner() {
        currentPlan = null;
        refreshWindowTitle("KrisApps Trip Planner");
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