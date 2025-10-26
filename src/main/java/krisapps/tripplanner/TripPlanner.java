package krisapps.tripplanner;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventReminder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import krisapps.tripplanner.data.*;
import krisapps.tripplanner.data.listview.cost_list.CategoryExpenseSummary;
import krisapps.tripplanner.data.listview.cost_list.CostListCellFactory;
import krisapps.tripplanner.data.listview.expense_linker.ExpenseLinkerCellFactory;
import krisapps.tripplanner.data.listview.itinerary.ItineraryCellFactory;
import krisapps.tripplanner.data.listview.upcoming_trips.UpcomingTripsCellFactory;
import krisapps.tripplanner.data.prompts.EditTripDetailsDialog;
import krisapps.tripplanner.data.prompts.LoadingDialog;
import krisapps.tripplanner.data.prompts.ProgramSettingsDialog;
import krisapps.tripplanner.data.trip.ExpenseCategory;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;
import krisapps.tripplanner.misc.*;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

public class TripPlanner {

    //<editor-fold desc="Globals">
    public static final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(2);
    private static Trip currentPlan = null;
    private static TripSettings currentPlanSettings;
    private static ProgramSettings currentProgramSettings;
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
    private boolean launchedInReadOnly = false;
    //</editor-fold>

    //<editor-fold desc="Miscellaneous UI">
    @FXML
    private VBox root;

    @FXML
    private MenuItem debugAction;
    //</editor-fold>

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
    @FXML
    private ListView<Itinerary.ItineraryItem> itineraryListView;
    @FXML
    private Label selectedItineraryEntryLabel;
    @FXML
    private Button deleteActivityButton;
    //</editor-fold>
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
    //</editor-fold>
    //<editor-fold desc="Expense planner">
    @FXML
    private ChoiceBox<String> expenseTypeSelector;
    @FXML
    private TextField expenseAmountBox;
    @FXML
    private Spinner<Integer> expenseDayBox;
    @FXML
    private ListView<PlannedExpense> expenseList;
    @FXML
    private TextField expenseNameBox;
    @FXML
    private Button deleteExpenseButton;
    @FXML
    private Label selectedExpenseLabel;
    //</editor-fold>
    //<editor-fold desc="Upcoming trips">
    @FXML
    private ListView<Trip> upcomingTripsList;
    //</editor-fold>
    //<editor-fold desc="Reminders">
    @FXML
    private ToggleButton calendarIntegrationToggle;

    @FXML
    private VBox calendarSettingsPanel;

    @FXML
    private VBox calendarSettingsContent;

    @FXML
    private CheckBox reminderToggle;

    @FXML
    private HBox reminderOptions;

    @FXML
    private TextField reminderOffsetBox;
    @FXML
    private ChoiceBox<TimeUnit> reminderOffsetUnitSelector;


    @FXML
    private ChoiceBox<String> countdownFormatSelector;

    @FXML
    private ToggleButton countdownToggle;
    //</editor-fold>
    //<editor-fold desc="Notification area">
    @FXML
    private VBox notificationArea;
    @FXML
    private HBox readOnlyNotification;
    @FXML
    private HBox unsavedChangesNotification;
    @FXML
    private HBox returnToPlannerNotification;
    //</editor-fold>
    //<editor-fold desc="Error area">
    @FXML
    private HBox errorPanel;
    @FXML
    private Label errorText;
    //</editor-fold>


    public static TripPlanner getInstance() {
        return instance;
    }

    /**
     * TODO: Implement 'Set reminders' (incl. integration with Google Calendar)
     * TODO: Test Calendar Event creation, ensure no weird bugs are present
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
        TripManager.log("Initializing Calendar Service");
        GoogleCalendarIntegration.initialize();
        currentProgramSettings = TripManager.getInstance().getSettings();
    }

    /**
     * Ensures all UI elements reflect the data of the currently loaded trip plan.
     * <br><b>Runs on the FX Application Thread.</b>
     */
    public void updateUIForCurrentPlan() {
        Platform.runLater(() -> {
            if (currentPlan.getTripStartDate() != null) {
                tripStartBox.setValue(currentPlan.getTripStartDate().toLocalDate());
            }
            if (currentPlan.getTripEndDate() != null) {
                tripEndBox.setValue(currentPlan.getTripEndDate().toLocalDate());
            }

            selectedExpenseLabel.setText("Nothing selected");
            selectedItineraryEntryLabel.setText("Nothing selected");

            // Initialize calendar integration panel
            TripSettings calSettings = trips.getSettingsForTrip(currentPlan.getUniqueID());
            calendarIntegrationToggle.setSelected(calSettings.isCalendarIntegrationEnabled());
            reminderToggle.setSelected(calSettings.isReminderEnabled());
            reminderOffsetBox.setText(String.valueOf(calSettings.getReminderValue() != -1 ? calSettings.getReminderValue() : ""));
            reminderOffsetUnitSelector.getSelectionModel().select(calSettings.getReminderUnit());
            countdownToggle.setSelected(currentPlanSettings.isCountdownEnabled());
            countdownFormatSelector.setDisable(!currentPlanSettings.isCountdownEnabled());
            countdownFormatSelector.getSelectionModel().select(currentPlanSettings.getCountdownFormat().getPreview());
        });
        refreshViews();
        refreshSpinners();
    }

    public void initUI() {
        setupSpinners();
        setupListViews();
        setupDropdowns();
        registerListeners();
        initReminderPanel();


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
        debugAction.setOnAction((e) -> {
            if (currentPlan == null) return;
            // GoogleCalendarIntegration.createCalendarEventForTrip(currentPlan, null, "_test32");
            LoadingDialog dlg = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_PROGRESSBAR);
            dlg.setPrimaryLabel("Please wait");
            dlg.setSecondaryLabel("Querying the Calendar API...");
            dlg.show("Hold on!", () -> {
                for (Event ev : GoogleCalendarIntegration.getTripPlannerCalendarEvents()) {
                    TripManager.log(ev.getDescription());
                }
            });
        });

        expenseAmountBox.setTextFormatter(new TextFormatter<>(numbersOnlyFormatter));
        tripBudgetBox.setTextFormatter(new TextFormatter<>(numbersOnlyFormatter));

        setNotificationVisible(PlannerNotification.ALL, false);
        displayError(PlannerError.ANY, false);
    }

    /**
     * Initializes everything on the reminder panel.
     * <br><b>Runs on the FX Application Thread.</b>
     */
    public void initReminderPanel() {
        Platform.runLater(() -> {
            calendarIntegrationToggle.selectedProperty().addListener((observable, oldVal, newVal) -> {
                calendarIntegrationToggle.setText(newVal ? "Enabled" : "Disabled");
                calendarSettingsContent.setDisable(!newVal);

                if (launchedInReadOnly) return;
                currentPlanSettings.setCalendarIntegrationEnabled(newVal);
            });
            reminderToggle.selectedProperty().addListener((observable, oldVal, newVal) -> {
                reminderOptions.setDisable(!newVal);

                if (launchedInReadOnly) return;
                currentPlanSettings.setReminderEnabled(newVal);
            });

            reminderOffsetBox.setTextFormatter(new TextFormatter<>(numbersOnlyFormatter));
            reminderOffsetUnitSelector.getItems().clear();
            reminderOffsetUnitSelector.getItems().addAll(TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS);

            countdownFormatSelector.getItems().clear();
            for (CountdownFormat format : CountdownFormat.values()) {
                countdownFormatSelector.getItems().add(format.getPreview());
            }

            countdownFormatSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) -> {
                if (currentPlan == null) return;
                if (launchedInReadOnly) return;
                currentPlanSettings.setCountdownFormat(CountdownFormat.of(newVal));
            });

            countdownToggle.selectedProperty().addListener((observable, oldVal, newVal) -> {
                countdownToggle.setText(newVal ? "Enabled" : "Disabled");
                countdownFormatSelector.setDisable(!newVal);
                if (currentPlan == null) return;
                if (launchedInReadOnly) return;
                currentPlanSettings.setCountdownEnabled(newVal);
            });

            // Register listeners
            reminderOffsetBox.textProperty().addListener((observable, oldVal, newVal) -> {
                if (launchedInReadOnly) return;
                if (newVal != null) {
                    if (Integer.parseInt(newVal) > 0) {
                        currentPlanSettings.setReminderValue(Integer.parseInt(newVal));
                    }
                }
            });
            reminderOffsetUnitSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) -> {
                if (launchedInReadOnly) return;
                if (newVal != null) {
                    currentPlanSettings.setReminderUnit(newVal);
                }
            });

            // Init UI to reflect current plan, unless current plan has no settings
            if (currentPlanSettings == null) return;
            calendarIntegrationToggle.setSelected(currentPlanSettings.isCalendarIntegrationEnabled());
            reminderToggle.setSelected(currentPlanSettings.isReminderEnabled());
            reminderOffsetBox.setText(String.valueOf(currentPlanSettings.getReminderValue()));
            reminderOffsetUnitSelector.getSelectionModel().select(currentPlanSettings.getReminderUnit());
            countdownToggle.setSelected(currentPlanSettings.isCountdownEnabled());
            countdownFormatSelector.setDisable(!currentPlanSettings.isCountdownEnabled());
            countdownFormatSelector.getSelectionModel().select(CountdownFormat.DEFAULT.getPreview());
        });
    }

    public void setupSpinners() {
        if (tripPartySizeBox.getValueFactory() == null) {
            SpinnerValueFactory<Integer> partySizeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Short.MAX_VALUE, 1);
            tripPartySizeBox.setValueFactory(partySizeValueFactory);
        }
    }

    /**
     * Populates the dropdowns in the UI.
     * <br><b>Runs on the FX Application Thread.</b>
     */
    public void setupDropdowns() {
        Platform.runLater(() -> {
            ObservableList<String> items = expenseTypeSelector.getItems();
            items.clear();
            items.addAll(Arrays.stream(ExpenseCategory.values()).map(ExpenseCategory::name).toList());
            expenseTypeSelector.getSelectionModel().select(ExpenseCategory.UNCATEGORIZED.name());
        });
    }

    public void setupListViews() {
        itineraryListView.setCellFactory(new ItineraryCellFactory(true));
        summaryItinerary.setCellFactory(new ItineraryCellFactory(false));
        expenseList.setCellFactory(new ExpenseLinkerCellFactory(true));
        upcomingTripsList.setCellFactory(new UpcomingTripsCellFactory());
        categoryBreakdownList.setCellFactory(new CostListCellFactory());
    }

    /**
     * Refreshes the various spinners to reflect current data, or initializes them if not initialized already.
     * <br><b>Runs on the FX Application Thread.</b>
     */
    public void refreshSpinners() {
        Platform.runLater(() -> {
            if (!currentPlan.tripDatesSupplied()) return;
            SpinnerValueFactory<Integer> expenseDayValueFactory = expenseDayBox.getValueFactory();
            SpinnerValueFactory<Integer> itineraryDayValueFactory = activityDayBox.getValueFactory();

            if (expenseDayValueFactory == null) {
                if (currentPlan == null) {
                    expenseDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE, -1);
                } else {
                    expenseDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, (int) currentPlan.getTripDuration().toDays(), -1);
                }
                expenseDayBox.setValueFactory(expenseDayValueFactory);
            } else {
                ((SpinnerValueFactory.IntegerSpinnerValueFactory) expenseDayBox.getValueFactory()).setMax(currentPlan == null ? Integer.MAX_VALUE : (int) currentPlan.getTripDuration().toDays());
            }

            if (itineraryDayValueFactory == null) {
                if (currentPlan == null) {
                    itineraryDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE, -1);
                } else {
                    itineraryDayValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, (int) currentPlan.getTripDuration().toDays(), -1);
                }
                activityDayBox.setValueFactory(itineraryDayValueFactory);
            } else {
                ((SpinnerValueFactory.IntegerSpinnerValueFactory) activityDayBox.getValueFactory()).setMax(currentPlan == null ? Integer.MAX_VALUE : (int) currentPlan.getTripDuration().toDays());
            }

            if (tripPartySizeBox.getValue() != currentPlan.getPartySize()) {
                tripPartySizeBox.getValueFactory().setValue((int) currentPlan.getPartySize());
            }
        });
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
            if (currentPlan.tripDatesSupplied()) {
                refreshSpinners();
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
            if (currentPlan.tripDatesSupplied()) {
                refreshSpinners();
            }
        });
        tripPartySizeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (currentPlan == null) return;
            if (launchedInReadOnly) return;
            if (Objects.equals(newValue, oldValue)) {
                return;
            }
            currentPlan.setPartySize(newValue.shortValue());
        });

        expenseList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedExpenseLabel.setText(newValue.getDescription() + " - " + TripManager.Formatting.formatMoney(newValue.getAmount(), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed()) + (newValue.getDay() != -1 ? " (Day #" + newValue.getDay() + ")" : ""));
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
                if (!tripWizard.isVisible()) {
                    setNotificationVisible(PlannerNotification.RETURN_TO_PLANNER, true);
                } else {
                    setNotificationVisible(PlannerNotification.RETURN_TO_PLANNER, false);
                }

                if (launchedInReadOnly) {
                    setNotificationVisible(PlannerNotification.READ_ONLY_MODE, true);
                } else {
                    setNotificationVisible(PlannerNotification.READ_ONLY_MODE, false);

                    setNotificationVisible(PlannerNotification.UNSAVED_CHANGES, (currentPlan.hasBeenModified() || currentPlanSettings.haveBeenModified()) && !launchedInReadOnly);
                }

                if (!currentPlan.tripDatesSupplied()) {
                    tripWizard.getTabs().get(4).setDisable(true);
                    return;
                } else {
                    if (currentPlan.getTripStartDate().isAfter(currentPlan.getTripEndDate())) {
                        displayError(PlannerError.TRIP_START_AFTER_END, true);
                    } else {
                        displayError(PlannerError.TRIP_START_AFTER_END, false);
                    }
                }
                tripWizard.getTabs().get(4).setDisable(false);
            } else {
                setNotificationVisible(PlannerNotification.ALL, false);
                displayError(PlannerError.ANY, false);
            }
        }, 0L, 100L, TimeUnit.MILLISECONDS);
    }

    public void launchCountdownRefresh() {
        scheduler.scheduleAtFixedRate(() -> {
            if (upcomingTripsPanel.isVisible() && !upcomingTripsList.getItems().isEmpty()) {
                upcomingTripsList.refresh();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Changes which part of the program is rendered based on ProgramState
     * <br><b>Runs on the Application FX Thread.</b>
     *
     * @param changeTo The new state to render.
     */
    private void changeProgramState(ProgramState changeTo) {
        Platform.runLater(() -> {
            tripWizard.setVisible(false);
            upcomingTripsPanel.setVisible(false);
            tripSetupPanel.setVisible(false);

            switch (changeTo) {
                case CREATE_NEW_TRIP -> tripSetupPanel.setVisible(true);
                case PLAN_TRIP -> {
                    tripWizard.setVisible(true);
                    if (launchedInReadOnly) {
                        setNotificationVisible(PlannerNotification.READ_ONLY_MODE, true);
                    } else {
                        setNotificationVisible(PlannerNotification.READ_ONLY_MODE, false);
                    }
                }
                case SHOW_DASHBOARD -> upcomingTripsPanel.setVisible(true);
            }
        });
    }

    /**
     * Shows or hides the supplied notification at the bottom of the window.
     * <br><b>Runs on the FX Application Thread.</b>
     *
     * @param notification The notification to show/hide
     * @param visible      Whether to show or hide the notification.
     */
    private void setNotificationVisible(PlannerNotification notification, boolean visible) {
        Platform.runLater(() -> {
            switch (notification) {
                case UNSAVED_CHANGES -> handleNotificationVisibilityChange(visible, unsavedChangesNotification);
                case READ_ONLY_MODE -> handleNotificationVisibilityChange(visible, readOnlyNotification);
                case RETURN_TO_PLANNER -> handleNotificationVisibilityChange(visible, returnToPlannerNotification);
                case ALL -> {
                    for (PlannerNotification notif : PlannerNotification.values()) {
                        if (notif == PlannerNotification.ALL) continue;
                        setNotificationVisible(notif, visible);
                    }
                }
            }
            notificationArea.setManaged(!(!readOnlyNotification.isVisible() && !returnToPlannerNotification.isVisible() && !unsavedChangesNotification.isVisible() && !errorPanel.isVisible()));
        });
    }

    /**
     * Displays an error at the bottom of the program window.
     * <br><b>Runs on the FX Application Thread.</b>
     *
     * @param error The error message to display. You may pass an empty string to clear the error message (hiding it)
     */
    private void displayError(PlannerError error, boolean display) {
        Platform.runLater(() -> {
            if (error == PlannerError.ANY) {
                errorPanel.setVisible(display);
                errorPanel.setManaged(display);
            } else {
                // If display is false, check if the currently shown error is of type 'error', and if so, clear the error text.
                // If display is true, replace the current error with the one supplied.
                if (!display) {
                    if (errorText.getText().equals(error.getMessage())) {
                        errorText.setText("");
                        errorPanel.setVisible(false);
                        errorPanel.setManaged(false);
                    }
                } else {
                    errorText.setText(error.getMessage());
                    errorPanel.setVisible(true);
                    errorPanel.setManaged(true);
                }
            }
        });
    }

    private void handleNotificationVisibilityChange(boolean visible, HBox notificationContainer) {
        if (notificationContainer.isVisible() == visible) return;
        if (!visible) {
            if (notificationContainer.getUserData() == "animating") return;
            notificationContainer.setUserData("animating");
            AnimationUtils.animateHBoxHorizontalScale(notificationContainer, 1, 0, javafx.util.Duration.millis(200), () -> {
                notificationContainer.setVisible(false);
                notificationContainer.setManaged(false);
                notificationContainer.setUserData(null);
            });
        } else {
            notificationContainer.setVisible(true);
            notificationContainer.setManaged(true);
            AnimationUtils.animateHBoxHorizontalScale(notificationContainer, 0, 1, javafx.util.Duration.millis(200), () -> {
            });
        }
    }

    private void enableReadOnly(boolean readOnly) {
        this.launchedInReadOnly = readOnly;
        if (launchedInReadOnly) {
            setNotificationVisible(PlannerNotification.UNSAVED_CHANGES, false);
        }
    }

    private CompletableFuture<Void> loadTrip(Trip t) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        refreshWindowTitle("KrisApps Trip Planner - loading " + t.getTripName());

        // Reload settings to ensure all trip settings are current
        currentProgramSettings = TripManager.getInstance().getSettings();

        currentPlan = t;

        currentPlanSettings = trips.getSettingsForTrip(t.getUniqueID());

        currentPlan.resetModifiedFlag();
        currentPlanSettings.resetModifiedFlag();

        setNotificationVisible(PlannerNotification.READ_ONLY_MODE, false);

        future.complete(null);
        return future;
    }

    public void reloadData() {
        if (currentPlan == null) return;
        Optional<ButtonType> response = PopupManager.showConfirmation("Reload active plan data",
                "Are you sure you wish to reload the data for the currently open trip plan from the disk? This will discard all changes and apply all the saved data.\nThis might take a bit to finish.",
                new ButtonType("Yes, reload", ButtonBar.ButtonData.APPLY),
                new ButtonType("No, cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        if (response.isPresent()) {
            if (response.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                TripManager.log("Reloading data for open trip plan");
                long start = System.currentTimeMillis();

                LoadingDialog dlg = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_SPINNER);
                dlg.setPrimaryLabel("Reloading plan data");
                dlg.setSecondaryLabel("Please wait while the data is reloaded");
                dlg.show("Reloading", () -> {
                    loadTrip(currentPlan).join();
                    enableReadOnly(launchedInReadOnly);
                    changeProgramState(ProgramState.PLAN_TRIP);
                    updateUIForCurrentPlan();

                    // Create some much-needed artificial tension
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                    }
                    refreshWindowTitle("KrisApps Trip Planner - planning " + currentPlan.getTripName());
                });
                TripManager.log("Data reloaded in " + (System.currentTimeMillis() - start - 200) + " ms");
            }
        }
    }

    public void refreshViews() {
        refreshItinerary();
        refreshTripOverview();
        refreshExpensePlanner();
        refreshUpcomingTrips();
    }

    public int getTimeInMinutes(Date date) {
        if (date == null) return -1;
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return ldt.getHour() * 60 + ldt.getMinute();
    }

    /**
     * Repopulates the itinerary.
     * <br><b>Runs on the FX Application Thread.</b>
     */
    public void refreshItinerary() {
        if (currentPlan == null) return;

        Platform.runLater(() -> {
            itineraryListView.getItems().clear();
            for (Itinerary.ItineraryItem item : currentPlan.getItinerary().getItems().values().stream().sorted(Comparator.comparingInt((i) -> {
                if (i.getDay() == -1 || i.getDay() == 0) return Integer.MAX_VALUE;
                else return i.getDay();
            })).sorted(Comparator.comparingLong((i) -> getTimeInMinutes(i.getStartTime()))).toList()) {
                itineraryListView.getItems().add(item);
            }
        });
    }

    public void refreshExpensePlanner() {
        // Reinitialize the spinners to apply limit changes for current trip plan (e.g. limit day picker to trip duration)
        setupSpinners();
        refreshExpenseList();
    }

    /**
     * Repopulates the expense list.
     * <br><b>Runs on the FX Application Thread.</b>
     */
    public void refreshExpenseList() {
        Platform.runLater(() -> {
            expenseList.getItems().clear();
            if (currentPlan != null) {
                for (PlannedExpense exp : currentPlan.getExpenseData().getPlannedExpenses().values()) {
                    expenseList.getItems().add(exp);
                }
            }
        });
    }

    /**
     * Refreshes the Trip Overview UI to reflect current data.
     * <br><b>Runs on the FX Application Thread.</b>
     */
    public void refreshTripOverview() {
        Platform.runLater(() -> {
            if (currentPlan == null) return;
            if (currentPlan.tripDatesSupplied()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                long tripDuration = Duration.between(currentPlan.getTripStartDate(), currentPlan.getTripEndDate().plusDays(1)).toDays();
                tripDatesLabel.setText(
                        formatter.format(currentPlan.getTripStartDate().toLocalDate()) + " - " + formatter.format(currentPlan.getTripEndDate().toLocalDate())
                                + " (duration: " + tripDuration + (tripDuration == 1 ? " day" : " days") + ")"
                );
            }
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
            totalExpensesLabel.setText(TripManager.Formatting.formatMoney(Math.floor(totalExpenses), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed()));
            dailyExpensesLabel.setText(TripManager.Formatting.formatMoney(Math.floor(minExpenses), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed()) + " - " + TripManager.Formatting.formatMoney(maxExpenses, currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed()));
            dailyAverageLabel.setText(TripManager.Formatting.decimalFormatter.format(dailyAverage) + currentProgramSettings.getCurrencySymbol());

            // Sort by total amount, descending
            categorySummaries.sort(Comparator.comparingDouble(CategoryExpenseSummary::getTotalAmount).reversed());
            categoryBreakdownList.getItems().addAll(categorySummaries);

            ObservableList<Itinerary.ItineraryItem> itineraryItems = summaryItinerary.getItems();
            itineraryItems.clear();
            itineraryItems.addAll(currentPlan.getItinerary().getItems().values().stream().sorted(Comparator.comparingInt((i) -> {
                if (i.getDay() == -1 || i.getDay() == 0) return Integer.MAX_VALUE;
                else return i.getDay();
            })).sorted(Comparator.comparingLong((i) -> getTimeInMinutes(i.getStartTime()))).toList());
            summaryItinerary.setItems(itineraryItems);

            expenseChart.getData().clear();
            for (CategoryExpenseSummary sum : categorySummaries) {
                expenseChart.getData().add(new PieChart.Data(sum.getCategory().getDisplayName(), sum.getTotalAmount()));
            }

            budgetLabel.setText(TripManager.Formatting.formatMoney(currentPlan.getExpenseData().getBudget(), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed()));
            if (currentPlan.getExpenseData().getTotalExpenses() <= currentPlan.getExpenseData().getBudget()) {
                budgetInfoLabel.setVisible(false);
                budgetInfoLabel.setManaged(false);
                budgetStatusLabel.setText("Within budget!");
            } else {
                budgetInfoLabel.setVisible(true);
                budgetInfoLabel.setManaged(true);
                budgetStatusLabel.setText("Over budget!");
                budgetInfoLabel.setText("+" + TripManager.Formatting.formatMoney(Math.floor(Math.abs(currentPlan.getExpenseData().getBudget() - currentPlan.getExpenseData().getTotalExpenses())), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed()));
            }
        });
    }

    /**
     * Repopulates the upcoming trips list.
     * <br><b>Runs on the FX Application Thread.</b>
     */
    public void refreshUpcomingTrips() {
        Platform.runLater(() -> {
            upcomingTripsList.getItems().clear();
            for (Trip t : trips.getTrips().stream().sorted(Comparator.comparingLong(t -> Duration.between(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()), t.getTripStartDate()).toHours())).toList()) {
                upcomingTripsList.getItems().add(t);
            }
        });
    }

    public void promptEditTripDetails() {
        if (currentPlan == null) {
            return;
        }
        EditTripDetailsDialog editDialog = new EditTripDetailsDialog(currentPlan);
        editDialog.showAndWait();
        refreshViews();
    }

    public void promptShowSettings() {
        ProgramSettingsDialog settingsDialog = new ProgramSettingsDialog(currentProgramSettings);
        Optional<ProgramSettings> changed = settingsDialog.showAndWait();
        changed.ifPresent(trips::updateProgramSettings);
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

    public void returnToPlanner() {
        if (currentPlan != null) {
            tripSetupPanel.setVisible(false);
            upcomingTripsPanel.setVisible(false);
            tripWizard.setVisible(true);
        } else {
            PopupManager.showPredefinedPopup(PopupManager.PopupType.NO_OPEN_PLAN);
        }
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
            loadTrip(tripToOpen).join();
            enableReadOnly(openInReadOnly);
            updateUIForCurrentPlan();
            changeProgramState(ProgramState.PLAN_TRIP);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            refreshWindowTitle("KrisApps Trip Planner - planning " + tripToOpen.getTripName());
        });
    }

    /**
     * Reloads the active trip from the file, resetting all changes.
     *
     * @param silent If true, the changes are discarded without confirmation.
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
     * This is a callback method for the 'Start planning!' button on the new trip wizard.
     * Creates a new trip using data from the new trip wizard and opens it in the Planner.
     */
    public void createNewTrip() {
        if (tripNameBox.getText().isEmpty() | tripDestinationBox.getText().isEmpty()) {
            PopupManager.showPredefinedPopup(PopupManager.PopupType.NEW_TRIP_WIZARD_EMPTY);
            return;
        }
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

    /**
     * Checks if the current trip needs to have its calendar events generated.
     */
    public void createCalendarEvents(boolean removeExisting) {
        if (!currentPlanSettings.isCalendarIntegrationEnabled()) {
            PopupManager.showPredefinedPopup(PopupManager.PopupType.CALENDAR_INTEGRATION_DISABLED);
            return;
        }
        if (removeExisting) {
            if (currentPlanSettings.calendarEventsCreated()) {
                boolean success = GoogleCalendarIntegration.deleteCalendarEventsForTrip(currentPlan);
                if (success) {
                    currentPlanSettings.setCalendarEventID(null);
                    trips.updateTripSettings(currentPlan, currentPlanSettings);
                    TripManager.log("Deleted old calendar events for '" + currentPlan.getTripName() + "'");
                }
            }
        }
        if (!currentPlanSettings.calendarEventsCreated()) {
            TripManager.log("Starting calendar event generation...");
            LoadingDialog dlg = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_PROGRESSBAR);
            dlg.setPrimaryLabel("Creating calendar events, hold on...");
            dlg.setSecondaryLabel("This will only take a moment.");
            dlg.show("Creating calendar events for trip", () -> {
                EventReminder reminder = new EventReminder();
                if (!currentPlanSettings.isReminderEnabled() || !currentPlanSettings.hasReminder()) {
                    reminder = null;
                } else {
                    TimeUnit unit = currentPlanSettings.getReminderUnit();
                    reminder.setMinutes((int) unit.toMinutes(currentPlanSettings.getReminderValue()));
                }
                String id = GoogleCalendarIntegration.createCalendarEventForTrip(currentPlan, reminder, "");
                if (id == null) {
                    PopupManager.showPredefinedPopup(PopupManager.PopupType.CALENDAR_EVENTS_NOT_CREATED);
                } else {
                    TripManager.log(String.format("Event created - ID: %s, updating settings", id));
                    dlg.setPrimaryLabel("Events created!");
                    dlg.setSecondaryLabel("Finishing up...");
                    currentPlanSettings.setCalendarEventID(id);
                    trips.updateTripSettings(currentPlan, currentPlanSettings);
                    TripManager.log("Done.");
                }
            });
        }
    }

    public void deleteCalendarEvents() {
        if (currentPlan == null) return;
        if (!currentPlanSettings.calendarEventsCreated()) {
            PopupManager.showPredefinedPopup(PopupManager.PopupType.NO_EVENTS);
        } else {
            LoadingDialog dlg = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_SPINNER);
            dlg.setTitle("Deleting events");
            dlg.setPrimaryLabel("Talking to Google Calendar");
            dlg.setSecondaryLabel("This will only take a moment.");
            dlg.show("Delete events", () -> {
                boolean success = GoogleCalendarIntegration.deleteCalendarEventsForTrip(currentPlan);
                if (success) {
                    Platform.runLater(() -> {
                        PopupManager.showPredefinedPopup(PopupManager.PopupType.EVENTS_DELETED);
                    });
                } else {
                    Platform.runLater(() -> {
                        PopupManager.showPredefinedPopup(PopupManager.PopupType.EVENTS_NOT_DELETED);
                    });
                }
            });
        }
    }

    public void regenerateCalendarEvents() {
        if (currentPlan == null) return;
        if (!currentPlanSettings.isCalendarIntegrationEnabled()) {
            PopupManager.showPredefinedPopup(PopupManager.PopupType.CALENDAR_INTEGRATION_DISABLED);
            return;
        }
        Optional<ButtonType> response = PopupManager.showConfirmation(
                "Regenerate calendar events for '" + currentPlan.getTripName() + "'?",
                "Are you sure you wish to regenerate the calendar events for this trip?\nThis will delete the existing events from the calendar and replace them with new ones.",
                new ButtonType("Yes, regenerate", ButtonBar.ButtonData.APPLY),
                new ButtonType("No, cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        if (response.isPresent()) {
            if (response.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                createCalendarEvents(true);
            }
        }
    }

    public void generatePlanDocument() {
        if (currentPlan == null) return;
        DocumentGenerator.generateTripPlan(currentPlan, trips.getSettings().getDocumentGeneratorOutputFolder());
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
                    if (currentPlanSettings.isCalendarIntegrationEnabled() && !currentPlanSettings.calendarEventsCreated()) {
                        Optional<ButtonType> r = PopupManager.showConfirmation("Create events?",
                                "Would you like the Planner to create calendar events for your trip? You can always regenerate them later.\nThis will only take a moment.",
                                new ButtonType("Sure, create events", ButtonBar.ButtonData.YES),
                                new ButtonType("Not now, thanks", ButtonBar.ButtonData.CANCEL_CLOSE)
                        );
                        if (r.isPresent()) {
                            if (r.get().getButtonData() == ButtonBar.ButtonData.YES) {
                                createCalendarEvents(false);
                            }
                        }
                    }
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
            currentPlanSettings.resetModifiedFlag();
            trips.updateTrip(currentPlan);
            trips.updateTripSettings(currentPlan, currentPlanSettings);
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

    /**
     * Sets the window title.
     * <br><b>Runs on the FX Application Thread.</b>
     *
     * @param title The title to set.
     */
    public void refreshWindowTitle(String title) {
        if (PlannerApplication.window != null) {
            Platform.runLater(() -> {
                PlannerApplication.window.setTitle(title);
            });
        } else {
            Platform.runLater(() -> {
                if (PlannerApplication.window != null) {
                    PlannerApplication.window.setTitle(title);
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
    }

}