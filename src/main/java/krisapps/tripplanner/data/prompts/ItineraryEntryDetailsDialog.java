package krisapps.tripplanner.data.prompts;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.TripPlanner;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.listview.expense_linker.ExpenseLinkerCellFactory;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class ItineraryEntryDetailsDialog extends Dialog<Void> {

    @FXML
    private VBox rootPane;

    @FXML
    private Label descLabel;

    @FXML
    private Label dayLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label expenseTotalLabel;

    @FXML
    private ListView<PlannedExpense> expenseList;

    private final Trip trip;
    private final Itinerary.ItineraryItem item;
    private static final TripPlanner trips = TripPlanner.getInstance();

    public ItineraryEntryDetailsDialog(Trip plan, Itinerary.ItineraryItem item) {
        this.trip = plan;
        this.item = item;

        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/itinerary_entry_details.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(item.getDescription());

        getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));

        descLabel.setText(item.getDescription());
        dayLabel.setText(item.getDay() != -1 ? "Planned for Day #" + item.getDay() : "No day assigned");
        expenseTotalLabel.setText("Total expenses: " + TripManager.Formatting.formatMoney(item.getExpenseTotal(trip), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));

        String startSection = (item.getStartTime() != null ? new SimpleDateFormat("HH:mm").format(item.getStartTime()) : "...");
        String endSection = (item.getEndTime() != null ? new SimpleDateFormat("HH:mm").format(item.getEndTime()) : "...");
        if (startSection.equals("...") && endSection.equals("...")) {
            timeLabel.setText("");
        } else {
            timeLabel.setText(startSection + (!startSection.isBlank() && !endSection.isBlank() ? " - " : "") + endSection);
        }
        timeLabel.setVisible(!timeLabel.getText().isEmpty());
        timeLabel.setManaged(timeLabel.isVisible());

        expenseList.setCellFactory(new ExpenseLinkerCellFactory(false));

        ObservableList<PlannedExpense> linkedExpenses = expenseList.getItems();
        linkedExpenses.clear();

        // Map the associated expenses for this item to the actual expense object, then add all to the list.
        linkedExpenses.setAll(
                item.getLinkedExpenses().stream()
                        .map(uuid -> trips.getOpenPlan().getExpenseData().getPlannedExpenses().get(uuid))
                        .collect(Collectors.toList())
        );
    }
}
