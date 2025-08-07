package krisapps.tripplanner.data.listview.itinerary;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import krisapps.tripplanner.Application;
import krisapps.tripplanner.data.trip.Itinerary;

import java.io.IOException;

public class ItineraryItemCell extends ListCell<Itinerary.ItineraryItem> {

    @FXML
    private VBox rootPane;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label dayLabel;

    @FXML
    private Label expenseSummaryLabel;

    @FXML
    private Button linkExpensesButton;

    public ItineraryItemCell() {
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("listview/itinerary_item_cell.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        descriptionLabel.setStyle("-fx-text-fill: black");
        dayLabel.setStyle("-fx-text-fill: black");
        expenseSummaryLabel.setStyle("-fx-text-fill: black");
    }

    @Override
    protected void updateItem(Itinerary.ItineraryItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            descriptionLabel.setText(item.getItemDescription());
            dayLabel.setText(item.getAssociatedDay() == -1 ? "" : "Planned for Day " + item.getAssociatedDay());
            expenseSummaryLabel.setText("Not implemented yet");
            setText(null);
            setGraphic(rootPane);
        }
    }
}
