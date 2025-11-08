package krisapps.tripplanner.data.dialogs;

import com.google.common.collect.ImmutableList;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.TripPlanner;
import krisapps.tripplanner.data.ProgramSettings;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.listview.expense_linker.ExpenseLinkerCellFactory;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

public class LinkExpensesDialog extends Dialog<Itinerary.ItineraryItem> {

    @FXML
    private VBox rootPane;

    @FXML
    private Label activityNameLabel;

    @FXML
    private Label expenseTotalLabel;

    @FXML
    private ListView<PlannedExpense> expenseList;

    @FXML
    private ListView<PlannedExpense> linkedExpenseList;

    @FXML
    private Button addSelectedButton;

    @FXML
    private Button addAllButton;

    @FXML
    private HBox dailyBudgetPanel;

    @FXML
    private Label budgetPanelLabel;

    @FXML
    private Label budgetPanelExpenseLabel;

    @FXML
    private Label budgetPanelBudgetLabel;

    private Itinerary.ItineraryItem item;
    private double totalExpenses = 0.0d;

    private boolean initialized = false;

    final TripPlanner trips = TripPlanner.getInstance();
    final ImmutableList<UUID> initialData;

    public LinkExpensesDialog(Itinerary.ItineraryItem item, Trip parent) {
        this.initialData = ImmutableList.copyOf(item.getLinkedExpenses());

        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/link_expenses.fxml"));
            loader.setController(this);
            rootPane = loader.load();
            initialized = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);

        getDialogPane().getButtonTypes().addAll(
                new ButtonType("Apply", ButtonBar.ButtonData.APPLY),
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        this.item = item;

        expenseList.setCellFactory(new ExpenseLinkerCellFactory(false));
        linkedExpenseList.setCellFactory(new ExpenseLinkerCellFactory(false));

        expenseList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                linkedExpenseList.getSelectionModel().clearSelection();
            }
        });

        linkedExpenseList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                expenseList.getSelectionModel().clearSelection();
            }
        });

        setResultConverter((response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                if (item.hasBeenModified()) {
                    return item;
                } else {
                    return null;
                }
            } else {
                TripManager.log("Rolling back all changes to linked expenses");
                item.setLinkedExpenses(initialData);
                return null;
            }
        }));

        refreshUI();

        // Trickery to be able to close the dialog manually
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Node b = getDialogPane().lookupButton(ButtonType.CANCEL);
        b.setVisible(false);
        b.setManaged(false);

        setTitle(trips.isReadOnlyEnabled() ? "Link expenses (preview; read-only mode active)" : "Link expenses");
        activityNameLabel.setText(item.getDescription() + (item.getDay() != -1 ? " (Day #" + item.getDay() + ")" : ""));
    }

    private void recalculateTotal() {
        totalExpenses = item.getExpenseTotal(trips.getOpenPlan());
    }

    private void refreshUI() {

        // Populate expense list
        ObservableList<PlannedExpense> expenses = expenseList.getItems();
        expenses.clear();

        for (PlannedExpense exp : trips.getOpenPlan().getExpenseData().getPlannedExpenses().values()) {
            if (item.getLinkedExpenses().contains(exp.getId())) continue;
            if (TripManager.getInstance().isExpenseLinked(trips.getOpenPlan(), exp.getId())) continue;
            expenses.add(exp);
        }

        // Populate linked expense list
        ObservableList<PlannedExpense> linkedExpenses = linkedExpenseList.getItems();
        linkedExpenses.clear();

        // Map the associated expenses for this item to the actual expense object, then add all to the list.
        linkedExpenses.setAll(
                item.getLinkedExpenses().stream()
                        .map(uuid -> trips.getOpenPlan().getExpenseData().getPlannedExpenses().get(uuid))
                        .collect(Collectors.toList())
        );

        recalculateTotal();
        refreshDailyBudgetPanel();
        expenseTotalLabel.setText(TripManager.Formatting.formatMoney(totalExpenses, TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));
    }

    public void refreshDailyBudgetPanel() {
        Trip currentPlan = trips.getOpenPlan();
        ProgramSettings currentProgramSettings = TripManager.getInstance().getSettings();
        dailyBudgetPanel.setVisible(trips.getOpenPlan().getExpenseData().getBudgetData().hasDailyBudgetLimit() && item.getDay() > 0);
        dailyBudgetPanel.setManaged(trips.getOpenPlan().getExpenseData().getBudgetData().hasDailyBudgetLimit() && item.getDay() > 0);

        double expensesPerSelectedDay = totalExpenses;
        boolean exceededDailyBudget = totalExpenses > currentPlan.getExpenseData().getBudgetData().getDailyBudget();
        addSelectedButton.setDisable(exceededDailyBudget && currentPlan.getExpenseData().getBudgetData().shouldEnforceDailyBudget());
        addAllButton.setDisable(exceededDailyBudget && currentPlan.getExpenseData().getBudgetData().shouldEnforceDailyBudget());

        if (exceededDailyBudget) {
            budgetPanelBudgetLabel.setText(TripManager.Formatting.formatMoney(currentPlan.getExpenseData().getBudgetData().getDailyBudget(), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed())
                    + " (+" + TripManager.Formatting.formatMoney(expensesPerSelectedDay - currentPlan.getExpenseData().getBudgetData().getDailyBudget(), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed()) + ")"
            );
        } else {
            budgetPanelBudgetLabel.setText(TripManager.Formatting.formatMoney(currentPlan.getExpenseData().getBudgetData().getDailyBudget(), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed()));
        }
        budgetPanelExpenseLabel.setText(
                TripManager.Formatting.formatMoney(expensesPerSelectedDay, currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed())
        );

        if (exceededDailyBudget) {
            dailyBudgetPanel.getStyleClass().add("exceeded");
        } else {
            dailyBudgetPanel.getStyleClass().removeAll("exceeded");
        }
        budgetPanelLabel.setText(exceededDailyBudget ? "Daily budget exceeded: " : "Daily budget limit: ");
    }

    public void linkSelectedExpense() {
        SelectionModel<PlannedExpense> expense = expenseList.getSelectionModel();
        if (expense.getSelectedItem() != null) {
            item.linkExpense(expense.getSelectedItem());
            TripManager.log("Linked expense #" + expense.getSelectedItem().getId() + " -> " + item.getDescription());
            refreshUI();
        }
    }

    public void unlinkSelectedExpense() {
        SelectionModel<PlannedExpense> expense = linkedExpenseList.getSelectionModel();
        if (expense.getSelectedItem() != null) {
            item.unlinkExpense(expense.getSelectedItem().getId());
            TripManager.log("Unlinked expense #" + expense.getSelectedItem().getId() + " from " + item.getDescription());
            refreshUI();
        }
    }

    public void linkAll() {
        for (PlannedExpense exp : trips.getOpenPlan().getExpenseData().getPlannedExpenses().values()) {
            if (item.getLinkedExpenses().contains(exp.getId())) continue;
            if (TripManager.getInstance().isExpenseLinked(trips.getOpenPlan(), exp.getId())) continue;
            item.linkExpense(exp);
        }
        refreshUI();
    }

    public void unlinkAll() {
        item.unlinkAllExpenses();
        refreshUI();
    }

    public void linkMatching() {
        for (PlannedExpense exp : trips.getOpenPlan().getExpenseData().getPlannedExpenses().values()) {
            if (item.getLinkedExpenses().contains(exp.getId())) continue;
            if (TripManager.getInstance().isExpenseLinked(trips.getOpenPlan(), exp.getId())) continue;
            if (exp.getDay() == item.getDay()) {
                item.linkExpense(exp);
            }
        }
        refreshUI();
    }
}
