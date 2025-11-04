package krisapps.tripplanner.data.listview.itinerary;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.Trip;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ItineraryCellFactory implements Callback<ListView<Itinerary.ItineraryItem>, ListCell<Itinerary.ItineraryItem>> {

    private final boolean allowEditCells;

    @Nullable
    private final Consumer<Itinerary.ItineraryItem> onItemUpdated;
    private final Trip trip;

    public ItineraryCellFactory(Trip trip, boolean allowEditCells, @Nullable Consumer<Itinerary.ItineraryItem> onItemUpdated) {
        this.allowEditCells = allowEditCells;
        this.onItemUpdated = onItemUpdated;
        this.trip = trip;
    }

    @Override
    public ListCell<Itinerary.ItineraryItem> call(ListView<Itinerary.ItineraryItem> param) {
        return new ItineraryItemCell(allowEditCells, onItemUpdated, trip);
    }
}
