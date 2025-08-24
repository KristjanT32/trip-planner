package krisapps.tripplanner;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import krisapps.tripplanner.data.PopupManager;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.listview.expense_linker.ExpenseLinkerCellFactory;
import krisapps.tripplanner.data.listview.itinerary.ItineraryCellFactory;
import krisapps.tripplanner.data.listview.upcoming_trips.UpcomingTripsCellFactory;
import krisapps.tripplanner.data.trip.ExpenseCategory;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import static java.lang.Thread.sleep;

public class TripPlanner {

    //<editor-fold desc="Menu panels">
    @FXML
    private TabPane tripWizard;

    @FXML
    private VBox upcomingTripsPanel;

    @FXML
    private VBox tripSetupPanel;

    @FXML
    private VBox loadingPanel;
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

    @FXML
    private ListView<Itinerary.ItineraryItem> itineraryListView;
    //</editor-fold>

    //<editor-fold desc="Trip overview">
    @FXML
    private Label tripDatesLabel;

    @FXML
    private Label peopleInvolvedLabel;
    //</editor-fold>

    //<editor-fold desc="Expense planner">
    @FXML
    private ChoiceBox<String> expenseTypeSelector;

    @FXML
    private TextField expenseAmountBox;

    @FXML
    private TextField expenseNameBox;

    @FXML
    private Spinner<Integer> expenseDayBox;

    @FXML
    private ListView<PlannedExpense> expenseList;
    //</editor-fold>

    //<editor-fold desc="Upcoming trips">

    @FXML
    private ListView<Trip> upcomingTripsList;

    //</editor-fold>

    private final TripManager trips = TripManager.getInstance();
    private static Trip currentPlan = null;
    private boolean launchedInReadOnly = false;
    private final UnaryOperator<TextFormatter.Change> numbersOnlyFormatter = (change) -> {
        if (change.getControlNewText().isEmpty()) {
            return change;
        }

        try {
            Double.parseDouble(change.getControlNewText());
            return change;
        } catch (NumberFormatException ignored) {}

        return null;
    };
    public static final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(2);

    private enum ProgramState {
        CREATE_NEW_TRIP,
        PLAN_TRIP,
        SHOW_DASHBOARD,
        LOADING_TRIP
    }

    private static TripPlanner instance;
    public static TripPlanner getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        TripManager.log("Initializing...");
        trips.init();

        if (trips.getTrips().isEmpty()) {
            changeProgramState(ProgramState.CREATE_NEW_TRIP);
        } else {
            changeProgramState(ProgramState.SHOW_DASHBOARD);
        }

        initUI();
        launchDataChecker();
        launchCountdownRefresh();
        instance = this;
    }

    public void initUI() {
        setupSpinners();
        setupListViews();
        setupDropdowns();
        registerListeners();

        expenseAmountBox.setTextFormatter(new TextFormatter<>(numbersOnlyFormatter));
    }

    public void setupSpinners() {
        SpinnerValueFactory<Integer> partySizeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Short.MAX_VALUE, 1);
        tripPartySizeBox.setValueFactory(partySizeValueFactory);

        SpinnerValueFactory<Integer> expenseDayValueFactory;
        try {
            expenseDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, (int) currentPlan.getTripDuration().toDays(), -1);
        } catch (IllegalStateException | NullPointerException e) {
            expenseDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE, -1);
        }
        expenseDayBox.setValueFactory(expenseDayValueFactory);
    }

    public void setupDropdowns() {
        ObservableList<String> items = expenseTypeSelector.getItems();
        items.addAll(Arrays.stream(ExpenseCategory.values()).map(ExpenseCategory::name).toList());
        expenseTypeSelector.getSelectionModel().select(ExpenseCategory.UNCATEGORIZED.name());
    }

    public void setupListViews() {
        itineraryListView.setCellFactory(new ItineraryCellFactory());

        final Itinerary.ItineraryItem[] testItems = {
          new Itinerary.ItineraryItem("test-1", 1),
          new Itinerary.ItineraryItem("test-2", -1)
        };

        for (Itinerary.ItineraryItem item : testItems) {
            itineraryListView.getItems().add(item);
        }

        expenseList.setCellFactory(new ExpenseLinkerCellFactory());
        upcomingTripsList.setCellFactory(new UpcomingTripsCellFactory());
        refreshUpcomingTrips();
        refreshExpensePlanner();
    }

    public void registerListeners() {
        // Setup menu
        tripStartBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (currentPlan == null) return;
            if (launchedInReadOnly) return;
            if (newValue == null) {
                currentPlan.setTripStartDate(null);
            } else {
                currentPlan.setTripStartDate(newValue.atStartOfDay());
            }
        });
        tripEndBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (currentPlan == null) return;
            if (launchedInReadOnly) return;
            if (newValue == null) {
                currentPlan.setTripEndDate(null);
            } else {
                currentPlan.setTripEndDate(newValue.atStartOfDay());
            }
        });
        tripPartySizeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (currentPlan == null) return;
            if (launchedInReadOnly) return;
            currentPlan.setPartySize(newValue.shortValue());
        });

        refreshItinerary();
    }

    public void launchDataChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            if (currentPlan == null) return;
            if (!currentPlan.tripDatesSupplied()) {
                tripWizard.getTabs().get(4).setDisable(true);
                return;
            }
            tripWizard.getTabs().get(4).setDisable(false);
        }, 0L, 200L, TimeUnit.MILLISECONDS);
    }

    public void launchCountdownRefresh() {
        scheduler.scheduleAtFixedRate(() -> {
            if (upcomingTripsPanel.isVisible()) {
                upcomingTripsList.refresh();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void changeProgramState(ProgramState changeTo) {
        tripWizard.setVisible(false);
        upcomingTripsPanel.setVisible(false);
        tripSetupPanel.setVisible(false);
        loadingPanel.setVisible(false);

        switch (changeTo) {
            case CREATE_NEW_TRIP -> tripSetupPanel.setVisible(true);
            case PLAN_TRIP -> tripWizard.setVisible(true);
            case SHOW_DASHBOARD -> upcomingTripsPanel.setVisible(true);
            case LOADING_TRIP -> loadingPanel.setVisible(true);
        }
    }

    private CompletableFuture<Void> loadTrip(Trip t) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        refreshWindowTitle("KrisApps Trip Planner - loading " + t.getTripName());
        changeProgramState(ProgramState.LOADING_TRIP);

        currentPlan = t;
        setupSpinners();
        setupDropdowns();
        setupListViews();
        refreshItinerary();
        refreshExpensePlanner();

        Platform.runLater(() -> {
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        refreshWindowTitle("KrisApps Trip Planner - planning " + t.getTripName());
        future.complete(null);

        return future;
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
        // Reinitialize the spinners to apply limit changes for current trip plan (e.g. limit day picker to trip duration)
        setupSpinners();

        expenseList.getItems().clear();
        if (currentPlan != null) {
            for (PlannedExpense exp: currentPlan.getExpenses().plannedExpenses) {
                expenseList.getItems().add(exp);
            }
        }
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

    public void refreshUpcomingTrips() {
        upcomingTripsList.getItems().clear();
        for (Trip t: trips.getTrips()) {
            upcomingTripsList.getItems().add(t);
        }
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
                    TripManager.log("Discarded current trip plan.");
                    resetPlanner();
                    showTripSetup();
                }
            }
        } else {
            changeProgramState(ProgramState.CREATE_NEW_TRIP);
        }
    }

    public void showUpcomingTrips() {
        refreshUpcomingTrips();
        tripWizard.setVisible(false);
        tripSetupPanel.setVisible(false);
        upcomingTripsPanel.setVisible(true);
    }

    public void openExistingTrip(Trip tripToOpen) {
        loadTrip(tripToOpen).thenRun(() -> {
           Platform.runLater(() -> {
               changeProgramState(ProgramState.PLAN_TRIP);
           });
        });
    }

    public void createNewTrip() {
        loadTrip(new Trip(
                tripNameBox.getText(),
                tripDestinationBox.getText()
        ));
        if (!tripBudgetBox.getText().isEmpty()) {
            currentPlan.setBudget(Double.parseDouble(tripBudgetBox.getText()));
        } else {
            currentPlan.setBudget(0);
        }

        // Reinitialize the spinners to apply limit changes for current trip plan (e.g. limit day picker to trip duration)
        setupSpinners();
        changeProgramState(ProgramState.PLAN_TRIP);
    }

    public void resetPlanner() {
        currentPlan = null;
        refreshWindowTitle("KrisApps Trip Planner");
    }


    public void addItineraryEntry() {
        if (activityDescriptionBox.getText().isEmpty()) {
            return;
        }
        String activityDesc =  activityDescriptionBox.getText();
        int activityDay = activityDayBox.getValue() != null ? activityDayBox.getValue() : -1;
        currentPlan.getItinerary().addItem(new Itinerary.ItineraryItem(activityDesc, activityDay));
        refreshItinerary();
    }

    public void addExpenseEntry() {
        if (expenseNameBox.getText().isEmpty()) {
            PopupManager.showPredefinedPopup(PopupManager.PopupType.EXPENSE_NAME_MISSING);
            return;
        }
        if (expenseAmountBox.getText().isEmpty()) {
            PopupManager.showPredefinedPopup(PopupManager.PopupType.EXPENSE_AMOUNT_MISSING);
            return;
        }
        PlannedExpense expense = new PlannedExpense(Double.parseDouble(expenseAmountBox.getText()), expenseNameBox.getText(), ExpenseCategory.valueOf(expenseTypeSelector.getValue()));
        expense.setDay(expenseDayBox.getValue());
        trips.addExpense(currentPlan, expense);

        expenseAmountBox.setText("");
        expenseNameBox.setText("");
        expenseTypeSelector.setValue(ExpenseCategory.UNCATEGORIZED.name());
        expenseDayBox.getValueFactory().setValue(-1);
        refreshExpensePlanner();
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