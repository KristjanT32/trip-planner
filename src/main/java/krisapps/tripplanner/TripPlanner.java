package krisapps.tripplanner;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import krisapps.tripplanner.data.DayExpenses;
import krisapps.tripplanner.data.PopupManager;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.listview.cost_list.CategoryExpenseSummary;
import krisapps.tripplanner.data.listview.cost_list.CostListCellFactory;
import krisapps.tripplanner.data.listview.expense_linker.ExpenseLinkerCellFactory;
import krisapps.tripplanner.data.listview.itinerary.ItineraryCellFactory;
import krisapps.tripplanner.data.listview.upcoming_trips.UpcomingTripsCellFactory;
import krisapps.tripplanner.data.prompts.EditTripDetailsDialog;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

public class TripPlanner {

    public static final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(2);
    private static Trip currentPlan = null;
    private static TripPlanner instance;
    private final TripManager trips = TripManager.getInstance();
    private final UnaryOperator<TextFormatter.Change> numbersOnlyFormatter = (change) -> {
        if (change.getControlNewText().isEmpty()) {
            return change;
        }

        try {
            Double.parseDouble(change.getControlNewText());
            return change;
        } catch (NumberFormatException ignored) {
        }

        return null;
    };
    //</editor-fold>
    @FXML
    private VBox root;
    //<editor-fold desc="Menu panels">
    @FXML
    private TabPane tripWizard;
    @FXML
    private VBox upcomingTripsPanel;
    //</editor-fold>
    @FXML
    private VBox tripSetupPanel;
    @FXML
    private VBox loadingPanel;
    //<editor-fold desc="New trip setup">
    @FXML
    private TextField tripNameBox;
    //</editor-fold>
    @FXML
    private TextField tripDestinationBox;
    @FXML
    private TextField tripBudgetBox;
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
    @FXML
    private Button deleteActivityButton;
    //<editor-fold desc="Trip overview">
    @FXML
    private Label tripDatesLabel;
    @FXML
    private Label peopleInvolvedLabel;
    @FXML
    private Label destinationLabel;
    @FXML
    private Label totalExpensesLabel;
    @FXML
    private Label dailyExpensesLabel;
    @FXML
    private Label dailyAverageLabel;
    @FXML
    private Label budgetLabel;
    //</editor-fold>
    @FXML
    private Label budgetStatusLabel;
    @FXML
    private Label budgetInfoLabel;
    @FXML
    private ListView<Itinerary.ItineraryItem> summaryItinerary;
    @FXML
    private ListView<CategoryExpenseSummary> categoryBreakdownList;
    @FXML
    private PieChart expenseChart;
    //<editor-fold desc="Expense planner">
    @FXML
    private ChoiceBox<String> expenseTypeSelector;
    @FXML
    private TextField expenseAmountBox;
    //</editor-fold>

    //<editor-fold desc="Upcoming trips">
    @FXML
    private TextField expenseNameBox;

    //</editor-fold>
    @FXML
    private Spinner<Integer> expenseDayBox;
    @FXML
    private ListView<PlannedExpense> expenseList;
    //</editor-fold>
    @FXML
    private Button deleteExpenseButton;
    @FXML
    private Label selectedExpenseLabel;
    @FXML
    private ListView<Trip> upcomingTripsList;
    //<editor-fold desc="Notification area">
    @FXML
    private HBox readOnlyNotification;
    @FXML
    private HBox unsavedChangesNotification;
    private boolean launchedInReadOnly = false;

    public static TripPlanner getInstance() {
        return instance;
    }

    /**
     * TODO: Ensure 'Trip Overview' doesn't access unset variables for newly created Trips
     * TODO: Implement 'Set reminders' (incl. integration with Google Calendar)
     * TODO: Finish implementing 'Trip Overview' (add missing data, add polish, add bottom margin to itinerary label etc.)
     * TODO: Implement plan document generation (also add menu for that)
     */

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
            refreshItinerary();


        } else {
            expenseDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE, -1);
            itineraryDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE, -1);
        }

        expenseDayBox.setValueFactory(expenseDayValueFactory);
        activityDayBox.setValueFactory(itineraryDayValueFactory);

        tripStartBox.setValue(currentPlan.getTripStartDate().toLocalDate());
        tripEndBox.setValue(currentPlan.getTripEndDate().toLocalDate());

        tripPartySizeBox.getValueFactory().setValue((int) currentPlan.getPartySize());

        selectedExpenseLabel.setText("Nothing selected");
        selectedItineraryEntryLabel.setText("Nothing selected");

        refreshViews();
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
        itineraryListView.setCellFactory(new ItineraryCellFactory(true));
        summaryItinerary.setCellFactory(new ItineraryCellFactory(false));
        expenseList.setCellFactory(new ExpenseLinkerCellFactory(true));
        upcomingTripsList.setCellFactory(new UpcomingTripsCellFactory());
        categoryBreakdownList.setCellFactory(new CostListCellFactory());
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
                selectedExpenseLabel.setText(newValue.getDescription() + " - " + TripManager.Formatting.formatMoney(newValue.getAmount(), "€", false) + (newValue.getDay() != -1 ? " (Day #" + newValue.getDay() + ")" : ""));
            }
        });

        itineraryListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedItineraryEntryLabel.setText(newValue.getDescription() + (newValue.getDay() != -1 ? " (Day #" + newValue.getDay() + ")" : ""));
            }
        });

        categoryBreakdownList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                Optional<PieChart.Data> slice = expenseChart.getData().stream().filter(data -> data.getName().equals(oldValue.getCategory().getDisplayName())).findFirst();
                slice.ifPresent(data -> data.getNode().setStyle("-fx-border-color: transparent; -fx-border-width: 0px"));
            }
            if (newValue != null) {
                Optional<PieChart.Data> slice = expenseChart.getData().stream().filter(data -> data.getName().equals(newValue.getCategory().getDisplayName())).findFirst();
                slice.ifPresent(data -> data.getNode().setStyle("-fx-border-color: black; -fx-border-width: 2px"));
            }
        });

        refreshItinerary();
    }

    public void launchRefreshTask() {
        scheduler.scheduleAtFixedRate(() -> {
            Optional<PlannedExpense> selectedExpense = Optional.ofNullable(expenseList.getSelectionModel().getSelectedItem());
            Optional<Itinerary.ItineraryItem> selectedItineraryItem = Optional.ofNullable(itineraryListView.getSelectionModel().getSelectedItem());
            deleteExpenseButton.setDisable(selectedExpense.isEmpty());
            deleteActivityButton.setDisable(selectedItineraryItem.isEmpty());

            if (currentPlan != null) {
                if (launchedInReadOnly) {
                    readOnlyNotification.setVisible(true);
                    readOnlyNotification.setManaged(true);
                } else {
                    readOnlyNotification.setVisible(false);
                    readOnlyNotification.setManaged(false);

                    if (currentPlan.hasBeenModified() && !launchedInReadOnly) {
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

    private void enableReadOnly(boolean readOnly) {
        this.launchedInReadOnly = readOnly;
        if (launchedInReadOnly) {
            unsavedChangesNotification.setVisible(false);
            unsavedChangesNotification.setManaged(false);
        }
    }

    private CompletableFuture<Void> loadTrip(Trip t) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        refreshWindowTitle("KrisApps Trip Planner - loading " + t.getTripName());

        currentPlan = t;
        t.resetModifiedFlag();

        readOnlyNotification.setVisible(false);
        readOnlyNotification.setManaged(false);

        future.complete(null);
        return future;
    }

    public void refreshViews() {
        refreshItinerary();
        refreshTripOverview();
        refreshExpensePlanner();
        refreshUpcomingTrips();
    }

    public void refreshItinerary() {
        if (currentPlan == null) return;

        itineraryListView.getItems().clear();
        for (Itinerary.ItineraryItem item : currentPlan.getItinerary().getItems().values().stream().sorted(Comparator.comparingInt((i) -> {
            if (i.getDay() == -1 || i.getDay() == 0) return Integer.MAX_VALUE;
            else return i.getDay();
        })).toList()) {
            itineraryListView.getItems().add(item);
        }
    }

    public void refreshExpensePlanner() {
        // Reinitialize the spinners to apply limit changes for current trip plan (e.g. limit day picker to trip duration)
        setupSpinners();
        refreshExpenseList();
    }

    public void refreshExpenseList() {
        expenseList.getItems().clear();
        if (currentPlan != null) {
            for (PlannedExpense exp : currentPlan.getExpenseData().getPlannedExpenses().values()) {
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

        destinationLabel.setText(currentPlan.getTripDestination());

        categoryBreakdownList.getItems().clear();
        ArrayList<CategoryExpenseSummary> categorySummaries = new ArrayList<>();

        double totalExpenses = 0.0;

        // Group all expenses into summary objects
        for (PlannedExpense e : currentPlan.getExpenseData().getPlannedExpenses().values()) {

            // Get the existing summary object or create a new one for the expense category if missing.
            CategoryExpenseSummary summary = categorySummaries.stream().filter(sum -> sum.getCategory().equals(e.getCategory())).findFirst().orElse(new CategoryExpenseSummary(e.getCategory()));

            summary.addExpense(e);
            if (categorySummaries.stream().noneMatch(sum -> sum.getCategory().equals(e.getCategory()))) {
                categorySummaries.add(summary);
            }
            totalExpenses += e.getAmount();
        }

        ArrayList<DayExpenses> dayExpenseSummaries = new ArrayList<>();

        for (PlannedExpense e : currentPlan.getExpenseData().getPlannedExpenses().values()) {
            if (e.getDay() == -1) continue;
            DayExpenses expenses = dayExpenseSummaries.stream().filter(dayExpenses -> dayExpenses.getDayIndex() == e.getDay()).findFirst().orElse(new DayExpenses(e.getDay()));

            expenses.addExpense(e);
            if (dayExpenseSummaries.stream().noneMatch(dayExpenses -> dayExpenses.getDayIndex() == e.getDay())) {
                dayExpenseSummaries.add(expenses);
            }
        }

        double minExpenses = Double.MAX_VALUE;
        double maxExpenses = 0.0d;
        double dailyAverage = 0.0d;
        for (DayExpenses expenseSummary : dayExpenseSummaries) {
            if (expenseSummary.getTotalExpenses() < minExpenses) {
                minExpenses = expenseSummary.getTotalExpenses();
            }
            if (expenseSummary.getTotalExpenses() > maxExpenses) {
                maxExpenses = expenseSummary.getTotalExpenses();
            }
            dailyAverage += expenseSummary.getTotalExpenses();
        }

        dailyAverage = dailyAverage / dayExpenseSummaries.size();

        if (currentPlan.getExpenseData().getPlannedExpenses().isEmpty()) {
            minExpenses = 0.0d;
            maxExpenses = 0.0d;
            dailyAverage = 0.0d;
        }
        totalExpensesLabel.setText(totalExpenses + "€");
        dailyExpensesLabel.setText(minExpenses + "€" + " - " + maxExpenses + "€");
        dailyAverageLabel.setText(TripManager.Formatting.decimalFormatter.format(dailyAverage) + "€");

        // Sort by total amount, descending
        categorySummaries.sort(Comparator.comparingDouble(CategoryExpenseSummary::getTotalAmount).reversed());
        categoryBreakdownList.getItems().addAll(categorySummaries);

        ObservableList<Itinerary.ItineraryItem> itineraryItems = summaryItinerary.getItems();
        itineraryItems.clear();
        itineraryItems.addAll(currentPlan.getItinerary().getItems().values().stream().sorted(Comparator.comparingInt((i) -> {
            if (i.getDay() == -1 || i.getDay() == 0) return Integer.MAX_VALUE;
            else return i.getDay();
        })).toList());
        summaryItinerary.setItems(itineraryItems);

        expenseChart.getData().clear();
        for (CategoryExpenseSummary sum : categorySummaries) {
            expenseChart.getData().add(new PieChart.Data(sum.getCategory().getDisplayName(), sum.getTotalAmount()));
        }

        budgetLabel.setText(currentPlan.getExpenseData().getBudget() + "€");
        if (currentPlan.getExpenseData().getTotalExpenses() <= currentPlan.getExpenseData().getBudget()) {
            budgetInfoLabel.setVisible(false);
            budgetInfoLabel.setManaged(false);
            budgetStatusLabel.setText("Within budget!");
        } else {
            budgetInfoLabel.setVisible(true);
            budgetInfoLabel.setManaged(true);
            budgetStatusLabel.setText("Over budget!");
            budgetInfoLabel.setText("+" + Math.abs(currentPlan.getExpenseData().getBudget() - currentPlan.getExpenseData().getTotalExpenses()) + "€");
        }
    }

    public void refreshUpcomingTrips() {
        upcomingTripsList.getItems().clear();
        for (Trip t : trips.getTrips().stream().sorted(Comparator.comparingLong(t -> Duration.between(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()), t.getTripStartDate()).toHours())).toList()) {
            upcomingTripsList.getItems().add(t);
        }
    }

    public void promptEditTripDetails() {
        if (currentPlan == null) {
            return;
        }
        EditTripDetailsDialog editDialog = new EditTripDetailsDialog(currentPlan);
        editDialog.showAndWait();
        refreshViews();
    }

    public void showTripSetup() {
        if (currentPlan != null) {

            Optional<ButtonType> response = PopupManager.showPredefinedPopup(PopupManager.PopupType.PLANNING_IN_PROGRESS);
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

    /**
     * Loads a trip from the file and opens it in the Planner.
     *
     * @param tripToOpen     The trip to load
     * @param openInReadOnly Whether to open the Planner in read-only mode.
     */
    public void openExistingTrip(Trip tripToOpen, boolean openInReadOnly) {
        if (currentPlan != null) {
            Optional<ButtonType> response = PopupManager.showPredefinedPopup(PopupManager.PopupType.PLANNING_IN_PROGRESS);
            if (response.isPresent()) {
                if (response.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                    TripManager.log("Discarded current trip plan.");
                    resetPlanner();
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        LoadingDialog dlg = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_SPINNER);
        dlg.setPrimaryLabel("Opening plan");
        dlg.setSecondaryLabel("Please wait while the trip is opened in the Planner...");
        dlg.show("Loading", () -> {
            Platform.runLater(() -> {
                loadTrip(tripToOpen).join();
                enableReadOnly(openInReadOnly);
                changeProgramState(ProgramState.PLAN_TRIP);
                updateUIForCurrentPlan();
                refreshWindowTitle("KrisApps Trip Planner - planning " + tripToOpen.getTripName());
            });
        });
    }

    /***
     * Reloads the active trip from the file, resetting all changes.
     */
    public void discardChanges(boolean silent) {
        if (currentPlan == null) {
            return;
        }
        if (silent) {
            loadTrip(currentPlan).join();
            enableReadOnly(true);
            changeProgramState(ProgramState.PLAN_TRIP);
            updateUIForCurrentPlan();
            refreshItinerary();
            refreshExpensePlanner();
            refreshWindowTitle("KrisApps Trip Planner - planning " + currentPlan.getTripName());
            TripManager.log("Discarded changes to open trip.");
        } else {
            Optional<ButtonType> response = PopupManager.showConfirmation(
                    "Discard all trip changes",
                    "Are you sure you wish to discard all pending changes? This action cannot be undone.",
                    new ButtonType("Yes, discard", ButtonBar.ButtonData.APPLY),
                    new ButtonType("No, keep changes", ButtonBar.ButtonData.CANCEL_CLOSE)
            );
            if (response.isPresent()) {
                if (response.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                    discardChanges(true);
                }
            }
        }
    }

    /**
     * Creates a new trip and opens it in the Planner.
     */
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

    public void promptClosePlanner() {
        if (currentPlan == null) {
            return;
        }
        closePlanner(false);
    }

    public void promptDiscardChanges() {
        discardChanges(false);
    }

    /**
     * Discards all changes, closes planner and opens the trip list.
     */
    public void closePlanner(boolean silent) {
        if (silent) {
            resetPlanner();
            changeProgramState(ProgramState.SHOW_DASHBOARD);
        } else {
            Optional<ButtonType> response = PopupManager.showConfirmation(
                    "Close planner",
                    "Are you sure you wish to close the planner? This will discard all pending changes and reset the planner.",
                    new ButtonType("Yes, close planner", ButtonBar.ButtonData.APPLY),
                    new ButtonType("No, cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
            );
            if (response.isPresent()) {
                if (response.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                    resetPlanner();
                    changeProgramState(ProgramState.SHOW_DASHBOARD);
                }
            }
        }
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
                enableReadOnly(false);
            }
        }
    }

    public void saveChanges() {
        if (launchedInReadOnly) {
            return;
        }
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
        expense.setDay(expenseDayBox.getValue() == 0 ? -1 : expenseDayBox.getValue());
        trips.addExpense(currentPlan, expense);

        expenseAmountBox.setText("");
        expenseNameBox.setText("");
        expenseTypeSelector.setValue(ExpenseCategory.UNCATEGORIZED.name());
        expenseDayBox.getValueFactory().setValue(-1);
        refreshExpensePlanner();
    }

    public void deleteSelectedExpense() {
        Optional<PlannedExpense> selectedExpense = Optional.ofNullable(expenseList.getSelectionModel().getSelectedItem());
        selectedExpense.ifPresent(expense -> currentPlan.getExpenseData().removeExpense(expense.getId()));
        refreshExpenseList();
    }

    public void addItineraryEntry() {
        if (activityDescriptionBox.getText().isEmpty()) {
            return;
        }
        String activityDesc = activityDescriptionBox.getText();
        int activityDay = activityDayBox.getValue() != null ? activityDayBox.getValue() : -1;
        currentPlan.getItinerary().addItem(new Itinerary.ItineraryItem(activityDesc, activityDay));

        activityDescriptionBox.setText("");
        activityDayBox.getValueFactory().setValue(-1);
        refreshItinerary();
    }

    public void deleteSelectedItineraryEntry() {
        Optional<Itinerary.ItineraryItem> selectedEntry = Optional.ofNullable(itineraryListView.getSelectionModel().getSelectedItem());
        Optional<ButtonType> btn = PopupManager.showConfirmation("Delete itinerary item", "Are you sure you wish to delete this itinerary item?\nAll its related expenses will remain intact.", new ButtonType("Yes, delete", ButtonBar.ButtonData.APPLY), new ButtonType("No, cancel", ButtonBar.ButtonData.CANCEL_CLOSE));
        if (btn.isPresent()) {
            if (btn.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                selectedEntry.ifPresent(itineraryItem -> currentPlan.getItinerary().removeItem(itineraryItem.getId()));
                refreshItinerary();
            }
        }
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

    private enum ProgramState {
        CREATE_NEW_TRIP,
        PLAN_TRIP,
        SHOW_DASHBOARD,
        LOADING_TRIP
    }

}