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
        initModality(Modality.WINDOW_MODAL);

        this.item = item;

        expenseList.setCellFactory(new ExpenseLinkerCellFactory());
        linkedExpenseList.setCellFactory(new ExpenseLinkerCellFactory());

        ObservableList<PlannedExpense> expenses = expenseList.getItems();
        expenses.clear();
        expenses.setAll(trips.getOpenPlan().getExpenseData().getPlannedExpenses().values());

        ObservableList<PlannedExpense> linkedExpenses = linkedExpenseList.getItems();
        linkedExpenses.clear();

        // Map the associated expenses for this item to the actual expense object, then add all to the list.
        linkedExpenses.setAll(
                item.getAssociatedExpenses().stream()
                        .map(uuid -> trips.getOpenPlan().getExpenseData().getPlannedExpenses().get(uuid))
                        .collect(Collectors.toList())
        );


        // Trickery to be able to close the dialog manually
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Node b = getDialogPane().lookupButton(ButtonType.CANCEL);
        b.setVisible(false);
        b.setManaged(false);

        setTitle("Link expenses");
        activityNameLabel.setText(item.getItemDescription());
        closeButton.setOnAction(e -> {
            close();
        });
    }

    public void linkSelectedExpense() {
        System.out.println("Linking selected expense");
    }

    public void unlinkSelectedExpense() {
        System.out.println("Unlinking selected expense");
    }
}
