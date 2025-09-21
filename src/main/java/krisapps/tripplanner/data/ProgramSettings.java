package krisapps.tripplanner.data;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ProgramSettings {

    private boolean modified = false;

    public static class TripSettings {
        private boolean integrationEnabled = false;
        private boolean reminderEnabled = false;
        private TimeUnit reminderUnit;
        private int reminderValue;
        private String calendarEventID = null;
        private boolean modified = false;

        public TripSettings() {
            this.integrationEnabled = false;
            this.reminderEnabled = false;
            this.reminderUnit = TimeUnit.MINUTES;
            this.reminderValue = -1;
            this.calendarEventID = null;
            this.modified = false;
        }

        public TripSettings(boolean integrationEnabled, boolean reminderEnabled, TimeUnit reminderUnit, int reminderValue, String calendarEventID, boolean modified) {
            this.integrationEnabled = integrationEnabled;
            this.reminderEnabled = reminderEnabled;
            this.reminderUnit = reminderUnit;
            this.reminderValue = reminderValue;
            this.calendarEventID = calendarEventID;
            this.modified = modified;
        }

        public boolean isIntegrationEnabled() {
            return integrationEnabled;
        }

        public boolean isReminderEnabled() {
            return reminderEnabled;
        }

        public TimeUnit getReminderUnit() {
            return reminderUnit;
        }

        public int getReminderValue() {
            return reminderValue;
        }

        public void setIntegrationEnabled(boolean integrationEnabled) {
            this.integrationEnabled = integrationEnabled;
            this.modified = true;
        }

        public void setReminderEnabled(boolean reminderEnabled) {
            this.reminderEnabled = reminderEnabled;
            this.modified = true;
        }

        public void setReminderUnit(TimeUnit reminderUnit) {
            this.reminderUnit = reminderUnit;
            this.modified = true;
        }

        public void setReminderValue(int reminderValue) {
            this.reminderValue = reminderValue;
            this.modified = true;
        }

        public void resetModifiedFlag() {
            this.modified = false;
        }

        public boolean haveBeenModified() {
            return modified;
        }
    }

    private final HashMap<UUID, TripSettings> tripSettings;
    private char currencySymbol;

    public ProgramSettings() {
        this.modified = false;
        this.tripSettings = new HashMap<>();
        this.currencySymbol = '€';
    }

    public HashMap<UUID, TripSettings> getTripSettings() {
        return tripSettings;
    }

    public void setCalendarSettings(UUID trip, TripSettings settings) {
        if (settings == null) {
            tripSettings.remove(trip);
            this.modified = true;
            return;
        }
        if (tripSettings.containsKey(trip)) {
            tripSettings.replace(trip, settings);
        } else {
            tripSettings.put(trip, settings);
        }
        this.modified = true;
    }

    public TripSettings getTripSettings(UUID trip) {
        return getTripSettings().getOrDefault(trip, new TripSettings());
    }

    public char getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(char currencySymbol) {
        this.currencySymbol = currencySymbol;
        this.modified = true;
    }

    public void resetModifiedFlag() {
        this.modified = false;
    }

    public boolean haveBeenModified() {
        this.modified = tripSettings.values().stream().anyMatch(TripSettings::haveBeenModified) || this.modified;
        return modified;
    }
}
