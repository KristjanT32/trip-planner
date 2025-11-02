package krisapps.tripplanner.data.listview.upcoming_trips;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import krisapps.tripplanner.data.trip.Trip;

public class UpcomingTripsCellFactory implements Callback<ListView<Trip>, ListCell<Trip>> {

    final Runnable onDeleteCallback;

    public UpcomingTripsCellFactory(Runnable onDeleteCallback) {
        this.onDeleteCallback = onDeleteCallback;
    }

    @Override
    public ListCell<Trip> call(ListView<Trip> param) {
        return new UpcomingTripCell(onDeleteCallback);
    }
}
