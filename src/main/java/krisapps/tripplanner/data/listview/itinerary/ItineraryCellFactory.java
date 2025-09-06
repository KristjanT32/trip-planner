package krisapps.tripplanner.data.listview.itinerary;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import krisapps.tripplanner.data.trip.Itinerary;

public class ItineraryCellFactory implements Callback<ListView<Itinerary.ItineraryItem>, ListCell<Itinerary.ItineraryItem>> {

    private final boolean allowEditCells;
    public ItineraryCellFactory(boolean allowEditCells) {
        this.allowEditCells = allowEditCells;
    }

    @Override
    public ListCell<Itinerary.ItineraryItem> call(ListView<Itinerary.ItineraryItem> param) {
        return new ItineraryItemCell(allowEditCells);
    }
}
