package krisapps.tripplanner.data;

import krisapps.tripplanner.data.trip.Trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Data {

    private HashMap<String, Object> values;
    private ArrayList<Trip> trips;
    private ProgramSettings settings;
    private final HashMap<UUID, TripSettings> tripSettings;

    public Data() {
        this.values = new HashMap<>();
        this.trips = new ArrayList<>();
        this.settings = new ProgramSettings();
        tripSettings = new HashMap<>();
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

    public TripSettings getTripSettings(UUID tripID) {
        return tripSettings.getOrDefault(tripID, new TripSettings());
    }

    /**
     * Sets the supplied trip's settings to <code>settings</code>.
     * If <code>settings</code> is null, the settings are removed.
     *
     * @param tripID   The trip whose settings to update
     * @param settings The new settings object
     * @return True, if the settings were updated, false if they were reset or added
     */
    public boolean setTripSettings(UUID tripID, TripSettings settings) {
        if (settings == null) {
            this.tripSettings.remove(tripID);
            return false;
        }
        if (this.tripSettings.containsKey(tripID)) {
            this.tripSettings.replace(tripID, settings);
            return true;
        } else {
            this.tripSettings.put(tripID, settings);
            return false;
        }
    }
}
