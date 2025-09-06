package krisapps.tripplanner.data.prompts;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.Application;
import krisapps.tripplanner.TripPlanner;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.listview.expense_linker.ExpenseLinkerCellFactory;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.PlannedExpense;

import java.io.IOException;
import java.util.stream.Collectors;

public class LinkExpensesDialog extends Dialog<Void> {

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
    private Button closeButton;

    private Itinerary.ItineraryItem item;
    private double totalExpenses = 0.0d;

    private boolean initialized = false;

    final TripPlanner trips = TripPlanner.getInstance();


    public LinkExpensesDialog(Itinerary.ItineraryItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("dialogs/link_expenses.fxml"));
            loader.setController(this);
            rootPane = loader.load();
            initialized = true;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);

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

        refreshUI();

        // Trickery to be able to close the dialog manually
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Node b = getDialogPane().lookupButton(ButtonType.CANCEL);
        b.setVisible(false);
        b.setManaged(false);

        setTitle(trips.isReadOnlyEnabled() ? "Link expenses (preview; read-only mode active)" : "Link expenses");
        activityNameLabel.setText(item.getDescription());
        closeButton.setOnAction(e -> {
            close();
        });
    }

    private void recalculateTotal() {
        totalExpenses = 0.0d;
        for (PlannedExpense exp: item.getLinkedExpenses().stream().map(exp -> trips.getOpenPlan().getExpenseData().getPlannedExpenses().get(exp)).toList()) {
            totalExpenses += exp.getAmount();
        }
    }

    private void refreshUI() {
        ObservableList<PlannedExpense> expenses = expenseList.getItems();
        expenses.clear();

        for (PlannedExpense exp: trips.getOpenPlan().getExpenseData().getPlannedExpenses().values()) {
            if (item.getLinkedExpenses().contains(exp.getId())) continue;
            if (TripManager.getInstance().isExpenseLinked(trips.getOpenPlan(), exp.getId())) continue;
            expenses.add(exp);
        }

        ObservableList<PlannedExpense> linkedExpenses = linkedExpenseList.getItems();
        linkedExpenses.clear();

        // Map the associated expenses for this item to the actual expense object, then add all to the list.
        linkedExpenses.setAll(
                item.getLinkedExpenses().stream()
                        .map(uuid -> trips.getOpenPlan().getExpenseData().getPlannedExpenses().get(uuid))
                        .collect(Collectors.toList())
        );

        // TODO: Implement settings for the currency symbol
        recalculateTotal();
        expenseTotalLabel.setText(TripManager.Formatting.formatMoney(totalExpenses, "€", false));
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
}
