package krisapps.tripplanner.data;

import krisapps.tripplanner.data.trip.Trip;

import java.util.ArrayList;
import java.util.HashMap;

public class Data {

    private HashMap<String, Object> values;
    private ArrayList<Trip> trips;

    public Data() {
        this.values = new HashMap<>();
        this.trips = new ArrayList<>();
    }

    public HashMap<String, Object> getSavedValues() {
        return this.values;
    }
    public ArrayList<Trip> getTrips() {
        return this.trips;
    }
}
