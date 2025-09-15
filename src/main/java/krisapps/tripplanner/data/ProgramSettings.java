package krisapps.tripplanner.data;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ProgramSettings {

    private boolean modified = false;

    public static class CalendarSettings {
        private boolean integrationEnabled = false;
        private boolean reminderEnabled = false;
        private TimeUnit reminderUnit;
        private int reminderValue;
        private String calendarEventID = null;
        private boolean modified = false;

        public CalendarSettings() {
            this.integrationEnabled = false;
            this.reminderEnabled = false;
            this.reminderUnit = TimeUnit.MINUTES;
            this.reminderValue = -1;
            this.calendarEventID = null;
            this.modified = false;
        }

        public CalendarSettings(boolean integrationEnabled, boolean reminderEnabled, TimeUnit reminderUnit, int reminderValue, String calendarEventID, boolean modified) {
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

    private final HashMap<UUID, CalendarSettings> calendarSettings;
    private char currencySymbol;

    public ProgramSettings() {
        this.modified = false;
        this.calendarSettings = new HashMap<>();
        this.currencySymbol = '€';
    }

    public HashMap<UUID, CalendarSettings> getCalendarSettings() {
        return calendarSettings;
    }

    public void setCalendarSettings(UUID trip, CalendarSettings settings) {
        if (settings == null) {
            calendarSettings.remove(trip);
            this.modified = true;
            return;
        }
        if (calendarSettings.containsKey(trip)) {
            calendarSettings.replace(trip, settings);
        } else {
            calendarSettings.put(trip, settings);
        }
        this.modified = true;
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
        this.modified = calendarSettings.values().stream().anyMatch(CalendarSettings::haveBeenModified) || this.modified;
        return modified;
    }
}
