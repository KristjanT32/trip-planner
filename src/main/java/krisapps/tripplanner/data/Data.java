package krisapps.tripplanner.data;

import krisapps.tripplanner.data.trip.Trip;

import java.util.ArrayList;
import java.util.HashMap;

public class Data {

    private HashMap<String, Object> values;
    private ArrayList<Trip> trips;
    private ProgramSettings settings;

    public Data() {
        this.values = new HashMap<>();
        this.trips = new ArrayList<>();
        this.settings = new ProgramSettings();
    }

    public HashMap<String, Object> getSavedValues() {
        return this.values;
    }

    public void setTrips(ArrayList<Trip> trips) {
        this.trips = trips;
    }

    public ArrayList<Trip> getTrips() {
        return this.trips;
    }

    public ProgramSettings getSettings() {
        return settings;
    }

    public void setSettings(ProgramSettings settings) {
        this.settings = settings;
    }
}
