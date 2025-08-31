package krisapps.tripplanner;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import krisapps.tripplanner.data.PopupManager;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.listview.expense_linker.ExpenseLinkerCellFactory;
import krisapps.tripplanner.data.listview.itinerary.ItineraryCellFactory;
import krisapps.tripplanner.data.listview.upcoming_trips.UpcomingTripsCellFactory;
import krisapps.tripplanner.data.prompts.LoadingDialog;
import krisapps.tripplanner.data.trip.ExpenseCategory;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

public class TripPlanner {

    @FXML
    private VBox root;

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

    @FXML
    private Label selectedItineraryEntryLabel;
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

    @FXML
    private Button deleteExpenseButton;

    @FXML
    private Label selectedExpenseLabel;
    //</editor-fold>

    //<editor-fold desc="Upcoming trips">

    @FXML
    private ListView<Trip> upcomingTripsList;

    //</editor-fold>

    //<editor-fold desc="Notification area">
    @FXML
    private HBox readOnlyNotification;

    @FXML
    private HBox unsavedChangesNotification;
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

    /*
    * TODO: Figure out why opening a new trip after saving another trip before marks the newly opened trip as modified.
    * This is probably due to the way UI is initialized with trip data, so likely a better way is required.
    * */

    @FXML
    public void initialize() {
        TripManager.log("Initializing...");
        long startTime = System.currentTimeMillis();
        trips.init();

        if (trips.getTrips().isEmpty()) {
            changeProgramState(ProgramState.CREATE_NEW_TRIP);
        } else {
            changeProgramState(ProgramState.SHOW_DASHBOARD);
        }

        initUI();
        launchRefreshTask();
        launchCountdownRefresh();
        refreshUpcomingTrips();
        instance = this;

        TripManager.log("Initialization completed in " + (System.currentTimeMillis() - startTime) + "ms");
    }

    public void updateUIForCurrentPlan() {
        SpinnerValueFactory<Integer> expenseDayValueFactory;
        SpinnerValueFactory<Integer> itineraryDayValueFactory;
        if (currentPlan != null) {
            expenseDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, (int) currentPlan.getTripDuration().toDays(), -1);
            itineraryDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, (int) currentPlan.getTripDuration().toDays(), -1);

            for (Itinerary.ItineraryItem item : currentPlan.getItinerary().getItems().values()) {
                itineraryListView.getItems().add(item);
            }


        } else {
            expenseDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE, -1);
            itineraryDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE, -1);
        }

        expenseDayBox.setValueFactory(expenseDayValueFactory);
        activityDayBox.setValueFactory(itineraryDayValueFactory);

        tripStartBox.setValue(currentPlan.getTripStartDate().toLocalDate());
        tripEndBox.setValue(currentPlan.getTripEndDate().toLocalDate());

        tripPartySizeBox.getValueFactory().setValue((int) currentPlan.getPartySize());

        refreshUpcomingTrips();
        refreshExpensePlanner();
    }

    public void initUI() {
        setupSpinners();
        setupListViews();
        setupDropdowns();
        registerListeners();

        root.sceneProperty().addListener((event, oldVal, newVal) -> {
            if (newVal != null) {
                newVal.addEventHandler(KeyEvent.KEY_PRESSED, (ev) -> {
                    if (ev.isControlDown()) {
                        if (ev.getCode() == KeyCode.S) {
                            if (launchedInReadOnly) {
                                Toolkit.getDefaultToolkit().beep();
                                return;
                            }
                            saveChanges(false);
                        }
                    }
                });
            }
        });

        expenseAmountBox.setTextFormatter(new TextFormatter<>(numbersOnlyFormatter));
        tripBudgetBox.setTextFormatter(new TextFormatter<>(numbersOnlyFormatter));

        readOnlyNotification.setVisible(false);
        readOnlyNotification.setManaged(false);
        unsavedChangesNotification.setVisible(false);
        unsavedChangesNotification.setManaged(false);
    }

    public void setupSpinners() {
        SpinnerValueFactory<Integer> partySizeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Short.MAX_VALUE, 1);
        tripPartySizeBox.setValueFactory(partySizeValueFactory);
    }

    public void setupDropdowns() {
        ObservableList<String> items = expenseTypeSelector.getItems();
        items.clear();
        items.addAll(Arrays.stream(ExpenseCategory.values()).map(ExpenseCategory::name).toList());
        expenseTypeSelector.getSelectionModel().select(ExpenseCategory.UNCATEGORIZED.name());
    }

    public void setupListViews() {
        itineraryListView.setCellFactory(new ItineraryCellFactory());
        expenseList.setCellFactory(new ExpenseLinkerCellFactory(true));
        upcomingTripsList.setCellFactory(new UpcomingTripsCellFactory());
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

        expenseList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
           if (newValue != null) {
               selectedExpenseLabel.setText(newValue.getExpenseSource() + " - " + TripManager.Formatting.formatMoney(newValue.getAmount(), "€", false) + (newValue.getDay() != -1 ? " (Day #" + newValue.getDay() + ")" : ""));
           }
        });

        itineraryListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
           if (newValue != null) {
               selectedItineraryEntryLabel.setText(newValue.getItemDescription() + (newValue.getAssociatedDay() != -1 ? " (Day #" + newValue.getAssociatedDay() + ")" : ""));
           }
        });

        refreshItinerary();
    }

    public void launchRefreshTask() {
        scheduler.scheduleAtFixedRate(() -> {
            Optional<PlannedExpense> selectedExpense = Optional.ofNullable(expenseList.getSelectionModel().getSelectedItem());
            deleteExpenseButton.setDisable(selectedExpense.isEmpty());

            if (currentPlan != null) {
                if (launchedInReadOnly) {
                    readOnlyNotification.setVisible(true);
                    readOnlyNotification.setManaged(true);
                } else {
                    readOnlyNotification.setVisible(false);
                    readOnlyNotification.setManaged(false);

                    if (currentPlan.hasBeenModified()) {
                        unsavedChangesNotification.setVisible(true);
                        unsavedChangesNotification.setManaged(true);
                    } else {
                        unsavedChangesNotification.setVisible(false);
                        unsavedChangesNotification.setManaged(false);
                    }
                }

                if (!currentPlan.tripDatesSupplied()) {
                    tripWizard.getTabs().get(4).setDisable(true);
                    return;
                }
                tripWizard.getTabs().get(4).setDisable(false);
            } else {
                readOnlyNotification.setVisible(false);
                readOnlyNotification.setManaged(false);
                unsavedChangesNotification.setVisible(false);
                unsavedChangesNotification.setManaged(false);
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
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
            case PLAN_TRIP -> {
                tripWizard.setVisible(true);
                if (launchedInReadOnly) {
                    readOnlyNotification.setVisible(true);
                    readOnlyNotification.setManaged(true);
                } else {
                    readOnlyNotification.setVisible(false);
                    readOnlyNotification.setManaged(false);
                }
            }
            case SHOW_DASHBOARD -> upcomingTripsPanel.setVisible(true);
            case LOADING_TRIP -> loadingPanel.setVisible(true);
        }
    }

    private CompletableFuture<Void> loadTrip(Trip t) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        refreshWindowTitle("KrisApps Trip Planner - loading " + t.getTripName());

        currentPlan = t;
        t.resetModifiedFlag();

        future.complete(null);
        return future;
    }


    public void refreshItinerary() {
        if (currentPlan == null) return;

        ObservableList<Itinerary.ItineraryItem> itineraryItems = itineraryListView.getItems();
        itineraryItems.clear();
        itineraryItems.addAll(currentPlan.getItinerary().getItems().values().stream().sorted(Comparator.comparingInt(Itinerary.ItineraryItem::getAssociatedDay)).toList());
    }

    public void refreshExpensePlanner() {
        // Reinitialize the spinners to apply limit changes for current trip plan (e.g. limit day picker to trip duration)
        setupSpinners();
        refreshExpenseList();
    }

    public void refreshExpenseList() {
        expenseList.getItems().clear();
        if (currentPlan != null) {
            for (PlannedExpense exp: currentPlan.getExpenseData().getPlannedExpenses().values()) {
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
        for (Trip t: trips.getTrips().stream().sorted(Comparator.comparingLong(t -> Duration.between(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()), t.getTripStartDate()).toHours())).toList()) {
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

    public void openExistingTrip(Trip tripToOpen, boolean openInReadOnly) {
        LoadingDialog dlg = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_SPINNER);
        dlg.setPrimaryLabel("Opening plan");
        dlg.setSecondaryLabel("Please wait while the trip is opened in the Planner...");
        dlg.show("Loading", () -> {
            Platform.runLater(() -> {
                loadTrip(tripToOpen).join();
                launchedInReadOnly = openInReadOnly;
                changeProgramState(ProgramState.PLAN_TRIP);
                updateUIForCurrentPlan();
                refreshWindowTitle("KrisApps Trip Planner - planning " + tripToOpen.getTripName());
            });
        });
    }

    public void createNewTrip() {
        Trip trip = new Trip(
                tripNameBox.getText(),
                tripDestinationBox.getText()
        );

        loadTrip(trip);
        if (!tripBudgetBox.getText().isEmpty()) {
            currentPlan.setBudget(Double.parseDouble(tripBudgetBox.getText()));
        } else {
            currentPlan.setBudget(0);
        }

        // Reinitialize the spinners to apply limit changes for current trip plan (e.g. limit day picker to trip duration)
        setupSpinners();
        changeProgramState(ProgramState.PLAN_TRIP);
        refreshWindowTitle("KrisApps Trip Planner - planning " + trip.getTripName());
    }

    public void resetPlanner() {
        currentPlan = null;
        refreshWindowTitle("KrisApps Trip Planner");
    }

    public boolean isReadOnlyEnabled() {
        return launchedInReadOnly;
    }

    public void disableReadOnly() {
        Optional<ButtonType> response = PopupManager.showConfirmation(
                "Enable plan editing?",
                "Are you sure you wish to enable plan editing? This is necessary to make changes to an incomplete plan, but can also protect your plan from unintended changes when you only want to view your plan.",
                new ButtonType("Yes, enable", ButtonBar.ButtonData.APPLY),
                new ButtonType("No, keep read-only", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        if (response.isPresent()) {
            if (response.get().getButtonData().equals(ButtonBar.ButtonData.APPLY)) {
                launchedInReadOnly = false;
            }
        }
    }

    public void saveChanges() {
        saveChanges(false);
    }

    public void saveChanges(boolean closePlanner) {
        LoadingDialog loadingDialog = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_PROGRESSBAR);
        loadingDialog.setPrimaryLabel("Just a second!");
        loadingDialog.setSecondaryLabel("Saving changes to '" + currentPlan.getTripName() + "'");

        loadingDialog.show("Saving trip data", () -> {
            currentPlan.resetModifiedFlag();
            trips.updateTrip(currentPlan);
            if (closePlanner) {
                Platform.runLater(() -> {
                    changeProgramState(ProgramState.SHOW_DASHBOARD);
                    refreshWindowTitle("KrisApps Trip Planner");
                });
            }
        });
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

    public void deleteSelectedExpense() {
        Optional<PlannedExpense> selectedExpense = Optional.ofNullable(expenseList.getSelectionModel().getSelectedItem());
        selectedExpense.ifPresent(expense -> currentPlan.getExpenseData().removeExpense(expense.getExpenseID()));
        refreshExpenseList();
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

    public void deleteSelectedItineraryEntry() {
        Optional<Itinerary.ItineraryItem> selectedEntry = Optional.ofNullable(itineraryListView.getSelectionModel().getSelectedItem());
        selectedEntry.ifPresent(itineraryItem -> currentPlan.getItinerary().removeItem(itineraryItem.getItemID()));
        refreshItinerary();
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

    public Trip getOpenPlan() {
        return currentPlan;
    }

}