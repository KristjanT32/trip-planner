package krisapps.tripplanner.data.listview.itinerary;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.TripPlanner;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.dialogs.AddOrEditItineraryEntryDialog;
import krisapps.tripplanner.data.dialogs.ItineraryEntryDetailsDialog;
import krisapps.tripplanner.data.dialogs.LinkExpensesDialog;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.Trip;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class ItineraryItemCell extends ListCell<Itinerary.ItineraryItem> {

    @FXML
    private VBox rootPane;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label dayLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label expenseSummaryLabel;

    @FXML
    private Button linkExpensesButton;

    @FXML
    private Button editActivityButton;

    @FXML
    private Button viewDetailsButton;

    final TripManager util = TripManager.getInstance();
    private final boolean editable;
    private final Consumer<Itinerary.ItineraryItem> onItemUpdated;
    private final Trip parent;

    public ItineraryItemCell(boolean editable, Consumer<Itinerary.ItineraryItem> onItemUpdated, Trip trip) {
        this.editable = editable;
        this.onItemUpdated = onItemUpdated;
        this.parent = trip;
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("listview/itinerary_item_cell.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        descriptionLabel.setStyle("-fx-text-fill: black");
        dayLabel.setStyle("-fx-text-fill: black");
        expenseSummaryLabel.setStyle("-fx-text-fill: black");
        timeLabel.setStyle("-fx-text-fill: black");

        viewDetailsButton.setVisible(!editable);
        viewDetailsButton.setManaged(!editable);

        if (!editable) {
            editActivityButton.setVisible(false);
            linkExpensesButton.setVisible(false);
        }
    }

    @Override
    protected void updateItem(Itinerary.ItineraryItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            descriptionLabel.setText(item.getDescription());
            dayLabel.setText(item.getDay() == -1 ? "No day assigned" : "Planned for Day " + item.getDay());

            String startSection = (item.getStartTime() != null ? new SimpleDateFormat("HH:mm").format(item.getStartTime()) : "...");
            String endSection = (item.getEndTime() != null ? new SimpleDateFormat("HH:mm").format(item.getEndTime()) : "...");
            if (startSection.equals("...") && endSection.equals("...")) {
                timeLabel.setText("");
            } else {
                timeLabel.setText(startSection + (!startSection.isBlank() && !endSection.isBlank() ? " - " : "") + endSection);
            }

            setText(null);
            setGraphic(rootPane);

            viewDetailsButton.setVisible(isSelected() && !editable);

            linkExpensesButton.setVisible(isSelected() && editable);
            editActivityButton.setVisible(isSelected() && editable);

            linkExpensesButton.setOnAction((_event) -> {
                LinkExpensesDialog dlg = new LinkExpensesDialog(item.copy(), parent);
                Optional<Itinerary.ItineraryItem> updated = dlg.showAndWait();
                updated.ifPresent(onItemUpdated);
                this.updateItem(item, false);
            });

            editActivityButton.setOnAction((event -> {
                AddOrEditItineraryEntryDialog editDialog = new AddOrEditItineraryEntryDialog(item, (int) parent.getTripDuration().toDays(), true);
                Optional<Itinerary.ItineraryItem> updated = editDialog.showAndWait();
                updated.ifPresent(onItemUpdated);

                this.updateItem(item, false);
            }));

            viewDetailsButton.setOnAction((event -> {
                ItineraryEntryDetailsDialog detailsDialog = new ItineraryEntryDetailsDialog(TripPlanner.getInstance().getOpenPlan(), item);
                detailsDialog.showAndWait();
            }));

            double totalExpenses = 0.0d;
            for (UUID expenseID : item.getLinkedExpenses()) {
                totalExpenses += parent.getExpenseData().getPlannedExpenses().get(expenseID).getAmount();
            }

            expenseSummaryLabel.setText(TripManager.Formatting.formatMoney(totalExpenses, TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));
        }
    }
}
