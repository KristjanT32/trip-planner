package krisapps.tripplanner.data;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ProgramSettings {

    private boolean modified = false;

    public static class TripSettings {
        private boolean calendarIntegrationEnabled = false;
        private boolean reminderEnabled = false;
        private TimeUnit reminderUnit;
        private int reminderValue;
        private String calendarEventID = "";
        private boolean modified = false;

        private boolean countdownEnabled = false;
        private CountdownFormat countdownFormat = CountdownFormat.DEFAULT;

        public TripSettings() {
            this.calendarIntegrationEnabled = false;
            this.reminderEnabled = false;
            this.reminderUnit = TimeUnit.MINUTES;
            this.reminderValue = -1;
            this.calendarEventID = "";
            this.modified = false;
        }

        public TripSettings(boolean calendarIntegrationEnabled, boolean reminderEnabled, TimeUnit reminderUnit, int reminderValue, String calendarEventID, boolean modified) {
            this.calendarIntegrationEnabled = calendarIntegrationEnabled;
            this.reminderEnabled = reminderEnabled;
            this.reminderUnit = reminderUnit;
            this.reminderValue = reminderValue;
            this.calendarEventID = calendarEventID;
            this.modified = modified;
        }

        public boolean isCalendarIntegrationEnabled() {
            return calendarIntegrationEnabled;
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

        public void setCalendarIntegrationEnabled(boolean calendarIntegrationEnabled) {
            this.calendarIntegrationEnabled = calendarIntegrationEnabled;
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

        public void setReminder(int value, TimeUnit unit) {
            this.reminderEnabled = true;
            this.reminderValue = value;
            this.reminderUnit = unit;
            this.modified = true;
        }

        public void setCountdownEnabled(boolean countdownEnabled) {
            this.countdownEnabled = countdownEnabled;
            this.modified = true;
        }

        public void setCountdownFormat(CountdownFormat countdownFormat) {
            this.countdownFormat = countdownFormat;
            this.modified = true;
        }

        public boolean isCountdownEnabled() {
            return countdownEnabled;
        }

        public CountdownFormat getCountdownFormat() {
            return countdownFormat;
        }

        public boolean hasReminder() {
            return this.reminderValue != -1 && this.reminderValue != 0;
        }

        public void resetModifiedFlag() {
            this.modified = false;
        }

        public boolean haveBeenModified() {
            return modified;
        }

        public String getCalendarEventID() {
            return calendarEventID;
        }

        public void setCalendarEventID(String calendarEventID) {
            this.calendarEventID = calendarEventID;
            this.modified = true;
        }

        public boolean calendarEventsCreated() {
            return !this.calendarEventID.isBlank();
        }
    }

    private final HashMap<UUID, TripSettings> tripSettings;
    private String currencySymbol;
    private boolean currencySymbolPrefix = false;
    private Path documentGeneratorOutputFolder;

    public ProgramSettings() {
        this.modified = false;
        this.tripSettings = new HashMap<>();
        this.currencySymbol = "€";
    }

    public HashMap<UUID, TripSettings> getTripSettings() {
        return tripSettings;
    }

    /**
     * Sets the supplied trip's settings to <code>settings</code>.
     *
     * @param trip     The trip whose settings to update
     * @param settings The new settings object
     * @return True, if the settings were updated, false if they were reset or added
     */
    public boolean setTripSettings(UUID trip, TripSettings settings) {
        if (settings == null) {
            tripSettings.remove(trip);
            this.modified = true;
            return false;
        }
        if (tripSettings.containsKey(trip)) {
            tripSettings.replace(trip, settings);
            this.modified = true;
            return true;
        } else {
            tripSettings.put(trip, settings);
            this.modified = false;
            return false;
        }
    }

    public TripSettings getSettingsForTrip(UUID trip) {
        return getTripSettings().getOrDefault(trip, new TripSettings());
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
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

    public boolean currencySymbolPrefixed() {
        return currencySymbolPrefix;
    }

    public void setCurrencySymbolPrefixed(boolean prefixed) {
        this.currencySymbolPrefix = prefixed;
        this.modified = true;
    }

    public Path getDocumentGeneratorOutputFolder() {
        return documentGeneratorOutputFolder;
    }

    public void setDocumentGeneratorOutputFolder(Path documentGeneratorOutputFolder) {
        this.documentGeneratorOutputFolder = documentGeneratorOutputFolder;
        this.modified = true;
    }
}
